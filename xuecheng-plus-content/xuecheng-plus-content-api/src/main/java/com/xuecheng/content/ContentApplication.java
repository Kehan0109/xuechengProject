package com.xuecheng.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Ke_han
 * @creat 2023-03-22 17:14
 * @description 内容管理服务的启动类
 */
@SpringBootApplication(scanBasePackages = "com.xuecheng")
@EnableSwagger2Doc  //生成Swagger接口文档
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }

}
