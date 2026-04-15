package com.example.sonardemo.controller;

import com.example.dto.PersonaDTO;
import com.example.sonardemo.entity.Persona;
import com.example.sonardemo.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    private PersonaDTO toDTO(Persona persona) {
        PersonaDTO dto = new PersonaDTO();
        dto.setId(persona.getId());
        dto.setNombre(persona.getNombre());
        dto.setEmail(persona.getEmail());
        dto.setEdad(persona.getEdad());
        return dto;
    }

    // Convertir DTO -> Entity
    private Persona toEntity(PersonaDTO dto) {
        Persona persona = new Persona();
        persona.setId(dto.getId());
        persona.setNombre(dto.getNombre());
        persona.setEmail(dto.getEmail());
        persona.setEdad(dto.getEdad());
        return persona;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK, we have a health check from Git Lab CI/CD pipeline");
    }


    @GetMapping
    public List<Persona> listar() {
        return personaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> obtenerPorId(@PathVariable Long id) {
        Optional<Persona> persona = personaService.obtenerPorId(id);
        return persona.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PersonaDTO> crear(@RequestBody PersonaDTO personaDTO) {
        Persona persona = toEntity(personaDTO);
        Persona nuevaPersona = personaService.guardar(persona);
        return new ResponseEntity<>(toDTO(nuevaPersona), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonaDTO> actualizar(@PathVariable Long id, @RequestBody PersonaDTO personaDTO) {
        try {
            Persona persona = toEntity(personaDTO);
            Persona personaActualizada = personaService.actualizar(id, persona);
            return ResponseEntity.ok(toDTO(personaActualizada));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }


}
