package com.example.sonardemo.component.controller;

import com.example.sonardemo.controller.RedocController;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedocControllerTest {

    @Test
    void shouldReturnHtml() throws Exception {
        RedocController controller = new RedocController();

        ByteArrayResource resource = new ByteArrayResource("test".getBytes());

        Field field = RedocController.class.getDeclaredField("redocHtml");
        field.setAccessible(true);
        field.set(controller, resource);

        String result = controller.getRedocDocumentation();

        assertNotNull(result);
    }
}