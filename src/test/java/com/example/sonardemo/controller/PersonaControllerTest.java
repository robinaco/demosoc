package com.example.sonardemo.controller;

import com.example.sonardemo.entity.Persona;
import com.example.sonardemo.service.PersonaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonaController.class)
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonaService personaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Persona crearPersona(Long id, String nombre, String email, int edad) {
        Persona p = new Persona();
        p.setId(id);
        p.setNombre(nombre);
        p.setEmail(email);
        p.setEdad(edad);
        return p;
    }

    @Test
    void crearPersonaDeberiaRetornar201() throws Exception {
        Persona personaRequest = crearPersona(null, "Carlos López", "carlos@email.com", 40);
        Persona personaResponse = crearPersona(1L, "Carlos López", "carlos@email.com", 40);

        when(personaService.guardar(any(Persona.class))).thenReturn(personaResponse);

        mockMvc.perform(post("/api/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos López"));
    }

    @Test
    void listarPersonasDeberiaRetornarLista() throws Exception {
        List<Persona> personas = Arrays.asList(
                crearPersona(1L, "Ana López", "ana@email.com", 35),
                crearPersona(2L, "Pedro Gómez", "pedro@email.com", 28)
        );

        when(personaService.listar()).thenReturn(personas);

        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ana López"))
                .andExpect(jsonPath("$[1].nombre").value("Pedro Gómez"));
    }

    @Test
    void obtenerPersonaPorIdCuandoExisteRetorna200() throws Exception {
        Persona persona = crearPersona(1L, "Juan Pérez", "juan@email.com", 30);
        when(personaService.obtenerPorId(1L)).thenReturn(Optional.of(persona));

        mockMvc.perform(get("/api/personas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    void obtenerPersonaPorIdCuandoNoExisteRetorna404() throws Exception {
        when(personaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/personas/99"))
                .andExpect(status().isNotFound());
    }
}