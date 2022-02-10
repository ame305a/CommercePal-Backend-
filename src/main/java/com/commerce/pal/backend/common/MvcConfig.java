package com.commerce.pal.backend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Value(value = "${org.java.user.data.directory}")
    private String vehicleImageDir;

    @Value(value = "${org.java.user.data.directory}")
    private String userImageDIr;

    @Value(value = "${org.java.user.data.directory}")
    private String documentsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("file:" + userImageDIr + "/");
        registry
                .addResourceHandler("/document/**")
                .addResourceLocations("file:" + documentsDir + "/");
        registry
                .addResourceHandler("/vehicle/images/**")
                .addResourceLocations("file:" + vehicleImageDir + "/");
    }

}