package com.example.sonardemo.integration;

import com.example.dto.PersonaDTO;
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
        PersonaDTO persona1 = new PersonaDTO();
        persona1.setNombre("Carlos");
        persona1.setEmail("carlos@email.com");
        persona1.setEdad(30);

        mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persona1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.edad").value(30));

        // Crear segunda persona
        PersonaDTO persona2 = new PersonaDTO();
        persona2.setNombre("Ana");
        persona2.setEmail("ana@email.com");
        persona2.setEdad(25);

        mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persona2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.edad").value(25));

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
        PersonaDTO persona = new PersonaDTO();
        persona.setNombre("Pedro");
        persona.setEmail("pedro@email.com");
        persona.setEdad(40);

        String response = mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(persona)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PersonaDTO personaCreada = objectMapper.readValue(response, PersonaDTO.class);
        Long id = personaCreada.getId();

        // Obtener por ID
        mockMvc.perform(get("/api/personas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro"))
                .andExpect(jsonPath("$.email").value("pedro@email.com"))
                .andExpect(jsonPath("$.edad").value(40));
    }

    @Test
    void testObtenerPersonaPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/personas/999"))
                .andExpect(status().isNotFound());
    }
}
