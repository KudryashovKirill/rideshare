package com.example.rideshare_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class RideshareRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideshareRestApplication.class, args);
    }

}
