package br.com.sistemacopias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SistemaRegistrosCopiasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaRegistrosCopiasApplication.class, args);
    }
}
