package com.example.sonardemo.integration;

import com.example.sonardemo.entity.Persona;
import com.example.sonardemo.repository.PersonaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PersonaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        personaRepository.deleteAll();
    }

    @Test
    void testCrearYListarPersonas() throws Exception {
        Persona persona1 = new Persona(null, "Carlos", "carlos@email.com", 30);

        mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persona1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Carlos"));

        // Crear segunda persona
        Persona persona2 = new Persona(null, "Ana", "ana@email.com", 25);

        mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persona2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana"));

        // Listar todas
        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Carlos"))
                .andExpect(jsonPath("$[1].nombre").value("Ana"));
    }

    @Test
    void testObtenerPersonaPorId() throws Exception {
        // Crear persona
        Persona persona = new Persona(null, "Pedro", "pedro@email.com", 40);

        String response = mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persona)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Persona personaCreada = objectMapper.readValue(response, Persona.class);
        Long id = personaCreada.getId();

        // Obtener por ID
        mockMvc.perform(get("/api/personas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro"))
                .andExpect(jsonPath("$.email").value("pedro@email.com"));
    }

    @Test
    void testObtenerPersonaPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/personas/999"))
                .andExpect(status().isNotFound());
    }
}