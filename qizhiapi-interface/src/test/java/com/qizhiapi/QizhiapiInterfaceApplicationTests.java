package com.qizhiapi;

import com.qizhiapi.qizhiapiclientsdk.ApiClient.QizhiApiClient;
import com.qizhiapi.qizhiapiclientsdk.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class QizhiapiInterfaceApplicationTests {

    @Autowired
    private QizhiApiClient apiClient;

    @Test
    void contextLoads() {
//        String result1 = apiClient.getName("zhien");
//        System.out.println(result1);
//        String result2 = apiClient.postName("zhien");
//        System.out.println(
//                "postName:" + result2
//        );
        User user = new User();
        user.setUsername("zhien");
        String result3 = apiClient.getUsernameByPost(user);
        System.out.println(result3);
    }

    @Test
    void test(){
        System.out.println("test");

    }
}
