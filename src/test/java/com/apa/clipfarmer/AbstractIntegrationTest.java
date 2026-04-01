package com.apa.clipfarmer;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.apa.clipfarmer.service.ClipFarmerService;

/**
 * Base class for integration tests. Starts a MySQL Testcontainer with the application schema
 * and wires its coordinates into the Spring datasource before context startup.
 *
 * <p>The {@link ClipFarmerService} is mocked to prevent the {@code CommandLineRunner}
 * from triggering the full batch on context load.
 *
 * @author alexpages
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("clipfarmer")
            .withUsername("clipfarmer")
            .withPassword("clipfarmer")
            .withInitScript("tables.sql");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    // Prevents CommandLineRunner from executing the full batch during context startup
    @MockBean
    ClipFarmerService clipFarmerService;
}
