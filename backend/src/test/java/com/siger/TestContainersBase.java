package com.siger;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Clase base abstracta que levanta un contenedor MySQL 8 compartido entre
 * todas las clases de test que la extiendan, y registra sus credenciales JDBC
 * en las propiedades del datasource de Spring mediante {@code @DynamicPropertySource}.
 *
 * <p>Las subclases solo necesitan anotarse con {@code @DataJpaTest} o
 * {@code @SpringBootTest}; las anotaciones {@code @Testcontainers} y
 * {@code @ActiveProfiles("test")} se heredan desde aqui.
 *
 * <p>El contenedor es {@code static} y {@code @Container}, de modo que se
 * inicia una sola vez por clase de test (y, con reuse, se reutiliza entre
 * ejecuciones). Las credenciales son las del propio contenedor efimero.
 */
@Testcontainers
@ActiveProfiles("test")
public abstract class TestContainersBase {

    /** Contenedor MySQL 8.0 real, unico para toda la suite de tests. */
    @Container
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("siger_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    /**
     * Inyecta la URL, usuario y contraseña del contenedor en las propiedades
     * del datasource, pisando cualquier valor de application-test.properties.
     */
    @DynamicPropertySource
    static void registrarDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}
