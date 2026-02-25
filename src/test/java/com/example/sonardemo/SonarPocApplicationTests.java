package com.example.sonardemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "server.port=0")
class SonarPocApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainMethod_ShouldRunWithoutExceptions() {
		SonarPocApplication.main(new String[]{});
	}
}