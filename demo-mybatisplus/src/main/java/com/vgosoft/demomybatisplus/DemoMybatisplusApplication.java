package com.vgosoft.demomybatisplus;

import com.vgosoft.core.util.SpringContextHolder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@MapperScan("com.vgosoft.demomybatisplus.dao")
@SpringBootApplication(scanBasePackages = "com.vgosoft")
public class DemoMybatisplusApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(DemoMybatisplusApplication.class, args);
        SpringContextHolder springContextHolder = new SpringContextHolder();
        springContextHolder.setApplicationContext(applicationContext);
    }

}
