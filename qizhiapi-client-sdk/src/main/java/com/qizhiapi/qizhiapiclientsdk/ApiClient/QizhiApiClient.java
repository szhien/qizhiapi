package com.qizhiapi.qizhiapiclientsdk.ApiClient;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.qizhiapi.qizhiapiclientsdk.entity.User;

import java.util.HashMap;
import static com.qizhiapi.qizhiapiclientsdk.utils.SignUtils.getSign;

/**
 * @author zhien
 * Created on 2024/6/18.
 * @Description Http客户端工具类
 */
public class QizhiApiClient {
    private final String gatewayHost;
    private final String accessKey;
    private final String secretKey;
    public QizhiApiClient(String gatewayHost,String accessKey, String secretKey) {
        this.gatewayHost = gatewayHost;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }


    //获取请求头
    private HashMap getHeaders(String body){
        HashMap<String, String> headers = new HashMap<>();
        headers.put("accessKey", accessKey);
        System.out.println("accessKey:"+accessKey);
        //一定不能直接加到请求头发送给后端
        //headers.put("secretKey", secretKey);
        headers.put("nonce", RandomUtil.randomNumbers(4));
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()/ 1000));
        headers.put("body", body);
        headers.put("sign", getSign(body,secretKey));
        return headers;
    }

    public String getNameByGet(String name){
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result1= HttpUtil.get(gatewayHost + "/api/name/", paramMap);
        System.out.println(result1);
        return result1;
    }

    public String getNameByPost(String name){
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result2= HttpUtil.post(gatewayHost + "/api/name/", paramMap);
        System.out.println(result2);
        return result2;
    }
    public String getUsernameByPost(User user){
        String json = JSONUtil.toJsonStr(user);
        System.out.println("json:"+json);
        HttpResponse httpResponse = HttpRequest.post(gatewayHost + "/api/name/user")
                .addHeaders(getHeaders(json))
                .body(json)
                .execute();
        System.out.println(httpResponse.getStatus());
        System.out.println(httpResponse.body());
        return httpResponse.body();
    }
}
