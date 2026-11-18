package gut.dev.veloxplay;

import gut.dev.veloxplay.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
public class VeloxplayApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeloxplayApplication.class, args);
    }
}
