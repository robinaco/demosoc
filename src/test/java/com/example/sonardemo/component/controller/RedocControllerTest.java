package com.example.sonardemo.component.controller;

import com.example.sonardemo.controller.PersonaController;
import com.example.sonardemo.controller.RedocController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@WebMvcTest(PersonaController.class)
class RedocControllerTest {

    @Autowired
    private RedocController controller;

    @Test
    void shouldReturnHtml() throws Exception {
        String result = controller.getRedocDocumentation();
        assertNotNull(result);
    }
}