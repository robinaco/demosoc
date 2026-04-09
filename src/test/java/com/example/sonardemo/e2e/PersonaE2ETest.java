package com.example.sonardemo.e2e;

import com.example.dto.PersonaDTO;
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
        PersonaDTO nuevaPersona = new PersonaDTO();
        nuevaPersona.setNombre("Laura");
        nuevaPersona.setEmail("laura@email.com");
        nuevaPersona.setEdad(32);

        ResponseEntity<PersonaDTO> postResponse = restTemplate.postForEntity("/api/personas", nuevaPersona, PersonaDTO.class);

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getBody().getId()).isNotNull();
        assertThat(postResponse.getBody().getNombre()).isEqualTo("Laura");

        Long id = postResponse.getBody().getId();

        // ===========================================
        // 2. CONSULTAR persona por ID
        // ===========================================
        ResponseEntity<PersonaDTO> getResponse = restTemplate.getForEntity("/api/personas/" + id, PersonaDTO.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getNombre()).isEqualTo("Laura");
        assertThat(getResponse.getBody().getEmail()).isEqualTo("laura@email.com");

        // ===========================================
        // 3. ACTUALIZAR persona
        // ===========================================
        PersonaDTO personaActualizada = new PersonaDTO();
        personaActualizada.setId(id);
        personaActualizada.setNombre("Laura Updated");
        personaActualizada.setEmail("laura.nuevo@email.com");
        personaActualizada.setEdad(33);

        HttpEntity<PersonaDTO> requestUpdate = new HttpEntity<>(personaActualizada);

        ResponseEntity<PersonaDTO> putResponse = restTemplate.exchange("/api/personas/" + id, HttpMethod.PUT, requestUpdate, PersonaDTO.class);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().getNombre()).isEqualTo("Laura Updated");

        // ===========================================
        // 4. ELIMINAR persona
        // ===========================================
        restTemplate.delete("/api/personas/" + id);

        ResponseEntity<PersonaDTO> getAfterDelete = restTemplate.getForEntity("/api/personas/" + id, PersonaDTO.class);
        assertThat(getAfterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void flujoCritico_ListarTodasLasPersonas() {
        // Crear algunas personas
        PersonaDTO persona1 = new PersonaDTO();
        persona1.setNombre("Uno");
        persona1.setEmail("uno@email.com");
        persona1.setEdad(20);

        PersonaDTO persona2 = new PersonaDTO();
        persona2.setNombre("Dos");
        persona2.setEmail("dos@email.com");
        persona2.setEdad(30);

        restTemplate.postForEntity("/api/personas", persona1, PersonaDTO.class);
        restTemplate.postForEntity("/api/personas", persona2, PersonaDTO.class);

        // Listar todas
        ResponseEntity<PersonaDTO[]> getResponse = restTemplate.getForEntity("/api/personas", PersonaDTO[].class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
        assertThat(getResponse.getBody().length).isGreaterThanOrEqualTo(2);
    }
}