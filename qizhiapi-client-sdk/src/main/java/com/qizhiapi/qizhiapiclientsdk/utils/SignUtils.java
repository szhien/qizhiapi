package com.qizhiapi.qizhiapiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @author zhien
 * Created on 2024/6/18.
 * @Description
 */
public class SignUtils {
    //生成签名
    public static String getSign(String body, String secretKey){
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String content = body + "." + secretKey;
        return md5.digestHex(content);
    }
}
