package com.ridealongug.backend;

import com.ridealongug.backend.models.jpahelpers.repository.JetRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(repositoryBaseClass = JetRepositoryImpl.class)
@SpringBootApplication
public class RideAlongUgBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RideAlongUgBackendApplication.class, args);
    }
}
