package com.cloudogu.smeagol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * Main entry point for the whole application.
 */
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class Smeagol {

    private static final Logger LOG = LoggerFactory.getLogger(Smeagol.class);

    private final Stage stage;

    /**
     * Creates a new Smeagol application in the requested stage.
     *
     * @param stageName name of stage
     */
    public Smeagol(@Value("${stage}") String stageName) {
        stage = Stage.fromString(stageName);
        if (stage == Stage.DEVELOPMENT) {
            LOG.warn("smeagol is running in development stage, never use this stage for production deployments");
        }
    }

    @Bean
    public Stage stage() {
        return stage;
    }

    public static void main(String[] args) {
        // disable dev tools restart, because we are using spring loaded
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(Smeagol.class, args);
    }
}
