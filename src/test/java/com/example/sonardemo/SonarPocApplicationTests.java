package com.example.sonardemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SonarPocApplicationTests {

	@Test
	void contextLoads() {
		// Verifica que el contexto carga
	}

	@Test
	void mainMethod_ShouldRunWithoutExceptions() {
		// Configurar propiedades para puerto aleatorio
		String[] args = new String[]{
				"--server.port=0"  // Puerto aleatorio
		};

		assertDoesNotThrow(() -> {
			ConfigurableApplicationContext context = SpringApplication.run(SonarPocApplication.class, args);
			context.close();
		});
	}
}