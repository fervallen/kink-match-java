package kinkmatch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KinkMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(KinkMatchApplication.class, args);
    }
}
