package com.apa.clipfarmer;

import com.apa.clipfarmer.service.ClipFarmerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application to autoconfigure the Java application.
 * This application will fetch clips from Twitch and add them to a YouTube channel.
 *
 * @author alexpages
 */
@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ClipFarmerApplication implements CommandLineRunner {

    private final ClipFarmerService clipFarmerService;

    /**
     * The main entry point for the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        String[] hardcodedArgs = {"streamerName=jasontheween"};
        SpringApplication.run(ClipFarmerApplication.class, hardcodedArgs); //TODO change for args
    }

    /**
     * Runs the ClipFarmer batch process upon application startup.
     *
     * @param args command-line arguments (streamer name) passed to the application
     */
    @Override
    public void run(String... args) {
        log.info("Starting ClipFarmerService batch process...");
        clipFarmerService.execute(args);
    }
}
