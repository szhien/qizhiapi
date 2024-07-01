package com.zhien.qizhiapi;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.stereotype.Service;

// 排除数据源自动配置，避免与Dubbo冲突
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@EnableDubbo
@Service
public class ZhienGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhienGatewayApplication.class, args);
    }
//    @DubboReference
//    private DemoService demoService;
//    public static void main(String[] args) {
//        ConfigurableApplicationContext context = SpringApplication.run(ZhienGatewayApplication.class, args);
//        ZhienGatewayApplication application = context.getBean(ZhienGatewayApplication.class);
//        String result = application.doSayHello("world");
//        String result2 = application.doSayHello2("world");
//        System.out.println("result: " + result);
//        System.out.println("result: " + result2);
//    }
//    public String doSayHello(String name) {
//        return demoService.sayHello(name);
//    }
//
//    public String doSayHello2(String name) {
//        return demoService.sayHello2(name);
//    }

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("path_route", r -> r.path("/get")
//                        .uri("http://httpbin.org"))
//                .route("host_route", r -> r.host("*.myhost.org")
//                        .uri("http://httpbin.org"))
//                .route("rewrite_route", r -> r.host("*.rewrite.org")
//                        .filters(f -> f.rewritePath("/foo/(?<segment>.*)", "/${segment}"))
//                        .uri("http://httpbin.org"))
//                .build();
//    }
}
