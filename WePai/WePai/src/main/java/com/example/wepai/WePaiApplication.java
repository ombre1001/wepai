package com.example.wepai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.wepai.mapper")
public class WePaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WePaiApplication.class, args);
    }

}
