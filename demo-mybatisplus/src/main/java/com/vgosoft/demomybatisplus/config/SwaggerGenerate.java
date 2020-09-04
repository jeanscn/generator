package com.vgosoft.demomybatisplus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerGenerate {
    @Bean
    public Docket createSwaggerdemomybatisplusRestApi() {
        String groupName = "Swaggerdemomybatisplus";
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(swaggerdemomybatisplusApiInfo())
                .select().apis(RequestHandlerSelectors.basePackage("com.vgosoft.demomybatisplus.controller"))
                .paths(PathSelectors.any())
                .build()
                .groupName(groupName);
    }

    private ApiInfo swaggerdemomybatisplusApiInfo(){
        return new ApiInfoBuilder()
                .title("vgosoft-swaggerdemomybatisplusApiInfo包接口swagger")
                .description("vgosoft-swaggerdemomybatisplusApiInfo包接口swagger测试及文档")
                .contact(new Contact("vgosoft","http://www.vgosoft.com",""))
                .version("1.0")
                .build();
    }
}
