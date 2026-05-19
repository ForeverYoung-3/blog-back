package com.blog.back;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.back.mapper")
public class BlogBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogBackApplication.class, args);
        System.out.println("启动成功");
    }
}
