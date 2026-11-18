package br.com.fiap.medlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class MedlinkApplication {

    public static void main(String[] args) {
        // O servidor do Render roda em UTC; as consultas são no horário de Brasília.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(MedlinkApplication.class, args);
    }
}
