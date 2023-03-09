package com.tcl.huim.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.tcl.huim"})
public class ServiceCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class, args);
    }
}
