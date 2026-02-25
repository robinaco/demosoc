package com.example.sonardemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SonarPocApplicationTests {

	@Test
	void contextLoads() {
		// Verifica que la aplicación arranca
	}

	@Test
	void mainMethod_ShouldRunWithoutExceptions() {
		SonarPocApplication.main(new String[]{});
	}
}