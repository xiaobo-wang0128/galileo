package org.armada.galileo.docker_center;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.armada"})
@EnableScheduling
public class DockerCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerCenterApplication.class, args);
    }

}
