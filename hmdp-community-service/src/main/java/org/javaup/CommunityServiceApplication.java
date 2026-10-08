package org.javaup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = "org.javaup.api")
@MapperScan("org.javaup.mapper")
@SpringBootApplication
public class CommunityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityServiceApplication.class, args);
    }
}
