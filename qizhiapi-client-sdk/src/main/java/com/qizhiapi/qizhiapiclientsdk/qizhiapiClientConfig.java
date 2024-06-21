package com.qizhiapi.qizhiapiclientsdk;

import com.qizhiapi.qizhiapiclientsdk.ApiClient.QizhiApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhien
 * Created on 2024/6/18.
 * @Description 定义Client的配置信息:我们不是启动一个web项目，而是构建一个已经生成的Client
 */
@Configuration
@ConfigurationProperties("qizhiapi.client")
@Data
@ComponentScan
public class qizhiapiClientConfig {
    private String gatewayHost;
    private String accessKey;
    private String secretKey;

    @Bean
    public QizhiApiClient qizhiapiClient(){
        return new QizhiApiClient(gatewayHost,accessKey,secretKey);
    }

}
