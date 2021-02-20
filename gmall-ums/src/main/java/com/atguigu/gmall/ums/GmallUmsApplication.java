package com.atguigu.gmall.ums;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.atguigu.gmall.ums.mapper")
@EnableSwagger2
public class GmallUmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUmsApplication.class, args);
    }

}
