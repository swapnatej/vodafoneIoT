package com.vodafoneapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication(scanBasePackageClasses= {VodafoneIoTServer.class})
public class VodafoneIoTServer {
	
    public static void main(String[] args) {
        SpringApplication.run(VodafoneIoTServer.class, args);
    }

}
