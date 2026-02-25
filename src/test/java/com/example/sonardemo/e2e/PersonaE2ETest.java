package com.example.sonardemo.e2e;

import com.example.sonardemo.entity.Persona;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PersonaE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void flujoCritico_CrearActualizarEliminarPersona() {
        // ===========================================
        // 1. CREAR persona
        // ===========================================
        Persona nuevaPersona = new Persona(null, "Laura", "laura@email.com", 32);

        ResponseEntity<Persona> postResponse = restTemplate.postForEntity("/api/personas", nuevaPersona, Persona.class);

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody().getId()).isNotNull();
        assertThat(postResponse.getBody().getNombre()).isEqualTo("Laura");

        Long id = postResponse.getBody().getId();

        // ===========================================
        // 2. CONSULTAR persona por ID
        // ===========================================
        ResponseEntity<Persona> getResponse = restTemplate.getForEntity("/api/personas/" + id, Persona.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getNombre()).isEqualTo("Laura");
        assertThat(getResponse.getBody().getEmail()).isEqualTo("laura@email.com");

        // ===========================================
        // 3. ACTUALIZAR persona
        // ===========================================
        Persona personaActualizada = new Persona(id, "Laura Updated", "laura.nuevo@email.com", 33);
        HttpEntity<Persona> requestUpdate = new HttpEntity<>(personaActualizada);

        ResponseEntity<Persona> putResponse = restTemplate.exchange("/api/personas/" + id, HttpMethod.PUT, requestUpdate, Persona.class);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().getNombre()).isEqualTo("Laura Updated");

        // ===========================================
        // 4. ELIMINAR persona
        // ===========================================
        restTemplate.delete("/api/personas/" + id);

        ResponseEntity<Persona> getAfterDelete = restTemplate.getForEntity("/api/personas/" + id, Persona.class);
        assertThat(getAfterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void flujoCritico_ListarTodasLasPersonas() {
        // Crear algunas personas
        restTemplate.postForEntity("/api/personas", new Persona(null, "Uno", "uno@email.com", 20), Persona.class);
        restTemplate.postForEntity("/api/personas", new Persona(null, "Dos", "dos@email.com", 30), Persona.class);

        // Listar todas
        ResponseEntity<Persona[]> getResponse = restTemplate.getForEntity("/api/personas", Persona[].class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
        assertThat(getResponse.getBody().length).isGreaterThanOrEqualTo(2);
    }
}