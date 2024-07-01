package com.zhien.qizhiapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhien.qizhiapi.exception.BusinessException;
import com.zhien.qizhiapi.utils.RedissonLockUtil;
import com.zhien.qizhiapicommon.exception.ErrorCode;
import com.zhien.qizhiapicommon.model.dto.RequestParamsField;
import com.zhien.qizhiapicommon.model.emums.InterfaceStatusEnum;
import com.zhien.qizhiapicommon.model.entity.InterfaceInfo;
import com.zhien.qizhiapicommon.model.vo.UserVO;
import com.zhien.qizhiapicommon.service.InnerInterfaceInfoService;
import com.zhien.qizhiapicommon.service.InnerUserInterfaceInvokeService;
import com.zhien.qizhiapicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bouncycastle.util.Strings;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.qizhiapi.qizhiapiclientsdk.utils.SignUtils.getSign;
import static com.zhien.qizhiapi.CacheBodyGatewayFilter.CACHE_REQUEST_BODY_OBJECT_KEY;
import static com.zhien.qizhiapi.utils.NetUtils.getIp;

/**
 * 全局过滤
 */


@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    /**
     * 请求白名单
     */
    private final static List<String> WHITE_HOST_LIST = Arrays.asList("127.0.0.1", "101.43.61.87");
    /**
     * 五分钟过期时间
     */
    private static final long FIVE_MINUTES = 5L * 60;
    @Resource
    private RedissonLockUtil redissonLockUtil;
    @DubboReference
    private InnerUserService innerUserService;
    @DubboReference
    private InnerUserInterfaceInvokeService interfaceInvokeService;
    @DubboReference
    private InnerInterfaceInfoService interfaceInfoService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求唯一id：" + request.getId());
        log.info("请求方法：" + request.getMethod());
        log.info("请求路径：" + request.getPath());
        log.info("网关本地地址：" + request.getLocalAddress());
        log.info("请求远程地址：" + request.getRemoteAddress());
        log.info("接口请求IP：" + getIp(request));
        log.info("url:" + request.getURI());
        return verifyParameters(exchange, chain);
    }

    /**
     * 验证参数
     *
     * @param exchange 交换
     * @param chain    链条
     * @return {@link Mono}<{@link Void}>
     */
    private Mono<Void> verifyParameters(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 请求白名单
        // if (!WHITE_HOST_LIST.contains(getIp(request))) {
        //     throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        // }

        HttpHeaders headers = request.getHeaders();
        String body = headers.getFirst("body");
        String accessKey = headers.getFirst("accessKey");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        // 请求头中参数必须完整
        if (StringUtils.isAnyBlank(body, sign, accessKey, timestamp)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }
        // 防重发XHR
        long currentTime = System.currentTimeMillis() / 1000;
        assert timestamp != null;
        if (currentTime - Long.parseLong(timestamp) >= FIVE_MINUTES) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "会话已过期,请重试！");
        }
        try {
            UserVO user = innerUserService.getInvokeUserByAccessKey(accessKey);
            if (user == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请正确配置接口凭证");
            }
            // 校验accessKey
            if (!user.getAccessKey().equals(accessKey)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "请先获取请求密钥");
            }
//            if (user.getStatus().equals(BAN.getValue())) {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号已封禁");
//            }
            // 校验签名
            if (!getSign(body, user.getSecretKey()).equals(sign)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "非法请求");
            }
            if (user.getBalance() <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足，请先充值。");
            }
            String method = Objects.requireNonNull(request.getMethod()).toString();
            String uri = request.getURI().toString().trim();

            if (StringUtils.isAnyBlank(uri, method)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            InterfaceInfo interfaceInfo = interfaceInfoService.getInterfaceInfo(uri, method);

            if (interfaceInfo == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
            }
            if (interfaceInfo.getStatus() == InterfaceStatusEnum.AUDITING.getValue()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口审核中");
            }
            if (interfaceInfo.getStatus() == InterfaceStatusEnum.OFFLINE.getValue()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口未开启");
            }
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            String requestParams = interfaceInfo.getRequestParams();
            List<RequestParamsField> list = new Gson().fromJson(requestParams, new TypeToken<List<RequestParamsField>>() {
            }.getType());
            if ("POST".equals(method)) {
                Object cacheBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
                String requestBody = getPostRequestBody((Flux<DataBuffer>) cacheBody);
                log.info("POST请求参数：" + requestBody);
                Map<String, Object> requestBodyMap = new Gson().fromJson(requestBody, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                if (StringUtils.isNotBlank(requestParams)) {
                    for (RequestParamsField requestParamsField : list) {
                        if ("是".equals(requestParamsField.getRequired())) {
                            if (StringUtils.isBlank((CharSequence) requestBodyMap.get(requestParamsField.getFieldName())) || !requestBodyMap.containsKey(requestParamsField.getFieldName())) {
                                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求参数有误，" + requestParamsField.getFieldName() + "为必选项，详细参数请参考API文档：https://doc.qimuu.icu/");
                            }
                        }
                    }
                }
            } else if ("GET".equals(method)) {
                log.info("GET请求参数：" + request.getQueryParams());
                // 校验请求参数
                if (StringUtils.isNotBlank(requestParams)) {
                    for (RequestParamsField requestParamsField : list) {
                        if ("是".equals(requestParamsField.getRequired())) {
                            if (StringUtils.isBlank(queryParams.getFirst(requestParamsField.getFieldName())) || !queryParams.containsKey(requestParamsField.getFieldName())) {
                                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "请求参数有误，" + requestParamsField.getFieldName() + "为必选项，详细参数请参考API文档：https://doc.qimuu.icu/");
                            }
                        }
                    }
                }
            }
            return handleResponse(exchange, chain, user, interfaceInfo);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, e.getMessage());
        }
    }

    /**
     * 获取post请求正文
     *
     * @param body 身体
     * @return {@link String}
     */
    private String getPostRequestBody(Flux<DataBuffer> body) {
        AtomicReference<String> getBody = new AtomicReference<>();
        body.subscribe(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            getBody.set(Strings.fromUTF8ByteArray(bytes));
        });
        return getBody.get();
    }

    /**
     * 处理响应
     *
     * @param exchange 交换
     * @param chain    链条
     * @return {@link Mono}<{@link Void}>
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, UserVO user, InterfaceInfo interfaceInfo) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        // 缓存数据的工厂
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        // 拿到响应码
        HttpStatus statusCode = originalResponse.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            // 装饰，增强能力
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                // 等调用完转发的接口后才会执行
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        // 往返回值里写数据
                        return super.writeWith(
                                fluxBody.map(dataBuffer -> {
                                    // 扣除积分
                                    redissonLockUtil.redissonDistributedLocks(("gateway_" + user.getUserAccount()).intern(), () -> {
                                        boolean invoke = interfaceInvokeService.invoke(interfaceInfo.getId(), user.getId(), interfaceInfo.getReduceScore());
                                        if (!invoke) {
                                            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
                                        }
                                    }, "接口调用失败");
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    // 释放掉内存
                                    DataBufferUtils.release(dataBuffer);
                                    String data = new String(content, StandardCharsets.UTF_8);
                                    // 打印日志
                                    log.info("响应结果：" + data);
                                    return bufferFactory.wrap(content);
                                }));
                    } else {
                        // 8. 调用失败，返回一个规范的错误码
                        log.error("<--- {} 响应code异常", getStatusCode());
                    }
                    return super.writeWith(body);
                }
            };
            // 设置 response 对象为装饰过的
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }
        // 降级处理返回数据
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
/*
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //  1. 记录请求日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("request uri: {}", request.getURI());
        log.info("request method: {}", request.getMethod());
        log.info("request header: {}", request.getHeaders());
        log.info("request query: {}", request.getQueryParams());
        String source = request.getRemoteAddress().getHostString();
        log.info("request remoteAddress host: {}",source);
        log.info("request remoteAddress: {}", request.getRemoteAddress());
        //  2. 访问控制 - 黑白名单
        ServerHttpResponse response = exchange.getResponse();
        if (!IP_WHITE_LIST.contains(source)){
            log.info("{} is not in white list", source);
            return handleNoAuth(response);
        }
        //  3. 用户鉴权（判断accessKey、secretKey是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String body = headers.getFirst("body");
        String sign = headers.getFirst("sign");
        System.out.println("accessKey:"+accessKey);
        //TODO 实际是从数据库中查是否分配给了用户
        //校验上面的参数
        if (!accessKey.equals("zhien")){
            log.error("accessKey is error");
            return handleNoAuth(response);
        }
        if (Long.parseLong(nonce) > 10000){
            log.error("nonce is error");
            return handleNoAuth(response);
        }
        //时间和当前时间相比不能超过 5分钟
        Long currentTime = System.currentTimeMillis()/1000;
        final Long FIVE_MINUTE = 5L * 60;
        if ((currentTime - Long.parseLong(timestamp)) > FIVE_MINUTE){
            log.error("timestamp is error");
            return handleNoAuth(response);
        }
        //TODO 实际情况是从数据库中查出 secretKey
//        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getSign(body, "abcdefgh");
        if (!sign.equals(serverSign) || sign == null){
            log.error("serverSign is error");
            return handleNoAuth(response);
        }
        // 4. 请求的模拟接口是否存在，以及请求方法是否匹配
//        InterfaceInfo interfaceInfo = null;
//        try {
//            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
//        } catch (Exception e) {
//            log.error("getInterfaceInfo error", e);
//        }
//        if (interfaceInfo == null) {
//            return handleNoAuth(response);
//        }
        // todo 是否还有调用次数
        // 5. 请求转发，调用模拟接口 + 响应日志
        //        Mono<Void> filter = chain.filter(exchange);
        //        return filter;
//        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
        return chain.filter(exchange);
    }

    */
/**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     *//*

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7.TODO 调用成功，接口调用次数 + 1 invokeCount
                                        try {
//                                            innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }


    @Override
    public int getOrder() {
        return -1;
    }


    public Mono<Void> handleNoAuth(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response){
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}*/
