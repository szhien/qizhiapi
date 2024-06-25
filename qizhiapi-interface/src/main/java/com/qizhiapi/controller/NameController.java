package com.qizhiapi.controller;

import com.qizhiapi.qizhiapiclientsdk.entity.User;
import com.qizhiapi.qizhiapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhien
 * Created on 2024/6/18.
 * @Description 获取名字
 */
@RestController
@RequestMapping("/name")
public class NameController
{
    @GetMapping("/get")
    public String getNameByGet(@RequestParam String name,HttpServletRequest request){
//        System.out.println("请求头:" + request.getHeader("zhien"));
        return "使用GET 你的名字是："+name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestBody String name){
        return "使用POST 你的名字是："+name;
    }
    @PostMapping("/user")
    public String getUsernameByPost(@RequestBody User user, HttpServletRequest request){
        String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String body = request.getHeader("body");
        String sign = request.getHeader("sign");
        System.out.println("accessKey:"+accessKey);
        //TODO 实际是从数据库中查是否分配给了用户
        //校验上面的参数
        if (!accessKey.equals("zhien")){
            throw new RuntimeException("无权限");
        }
        if (Long.parseLong(nonce) > 10000){
            throw new RuntimeException("无权限");
        }
        //时间和当前时间相比不能超过 5分钟
        Long currentTime = System.currentTimeMillis()/1000;
        final Long FIVE_MINUTE = Long.valueOf(5*60);
        if ((currentTime - Long.parseLong(timestamp)) > FIVE_MINUTE){
            throw new RuntimeException("timestamp is error");
        }
        //TODO 实际情况是从数据库中查出 secretKey
        String serverSign = SignUtils.getSign(body, "abcdefgh");
        if (!sign.equals(serverSign)){
            throw new RuntimeException("用户签名无权限");
        }
        return "使用POST 你的用户名是："+user.getUsername();
    }
}
