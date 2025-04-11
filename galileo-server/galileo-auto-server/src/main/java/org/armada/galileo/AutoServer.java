package org.armada.galileo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.armada"})
@EnableScheduling
public class AutoServer {

    public static void main(String[] args) {
        SpringApplication.run(AutoServer.class, args);
    }

}
