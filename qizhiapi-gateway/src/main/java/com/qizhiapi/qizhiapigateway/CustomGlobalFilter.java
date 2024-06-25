package com.qizhiapi.qizhiapigateway;

import com.qizhiapi.qizhiapiclientsdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤
 */

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
        //  4. 请求的模拟接口是否存在？
        //TODO 实际情况是从数据库中查出 secretKey
        String serverSign = SignUtils.getSign(body, "abcdefgh");
        if (!sign.equals(serverSign)){
            log.error("serverSign is error");
            return handleNoAuth(response);
        }
        //  5. 请求转发，调用模拟接口,请求调用成功后释放过滤器链，或进入下一步的过滤
        Mono<Void> filter = chain.filter(exchange);
        log.info("响应的状态码："+response.getStatusCode());

        //  6. 响应日志
        log.info("响应日志："+response.getStatusCode());

        //  7. 调用成功，接口调用次数 +1 invokeCount
        if (response.getStatusCode() == HttpStatus.OK){

        }else {
            //  8. 调用失败，返回一个规范的错误码
            return handleInvokeError(response);
        }



        log.info("custom global filter");
        return filter;
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
}