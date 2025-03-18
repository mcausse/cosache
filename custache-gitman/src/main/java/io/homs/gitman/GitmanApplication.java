package io.homs.gitman;

import io.homs.custache.spring.CustacheConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CustacheConfiguration.class)
@SpringBootApplication
public class GitmanApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitmanApplication.class, args);
    }

}
