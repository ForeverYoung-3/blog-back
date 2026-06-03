package com.blog.back;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.blog.back.mapper")
@EnableScheduling
public class BlogBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogBackApplication.class, args);
        System.out.println("启动成功");
    }
}
