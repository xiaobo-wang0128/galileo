package org.armada.galileo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.context.annotation.ImportResource;

import java.net.URL;
import java.net.URLClassLoader;

@SpringBootApplication(scanBasePackages = {"org.armada"})
@ImportResource
public class StartApplication {

    public static void main(String[] args) {

        // System.setProperty("server.servlet.context-path", "/data22");

        SpringApplication.run(StartApplication.class, args);
    }

}