package com.example.sonardemo.service;

import com.example.sonardemo.entity.Persona;
import com.example.sonardemo.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    /**
     * Guarda una persona en la base de datos
     * @param persona la persona a guardar
     * @return la persona guardada con su ID generado
     * @throws IllegalArgumentException si la edad es negativa
     */
    public Persona guardar(Persona persona) {
        if (persona.getEdad() < 0) {
            throw new IllegalArgumentException("La edad no puede ser negativa");
        }
        return personaRepository.save(persona);
    }

    /**
     * Busca una persona por su ID
     * @param id el ID de la persona
     * @return Optional con la persona si existe, Optional vacío si no
     */
    public Optional<Persona> obtenerPorId(Long id) {
        return personaRepository.findById(id);
    }

    /**
     * Lista todas las personas
     * @return lista de todas las personas
     */
    public List<Persona> listar() {
        return personaRepository.findAll();
    }

    /**
     * Elimina una persona por su ID
     * @param id el ID de la persona a eliminar
     */
    public void eliminar(Long id) {
        personaRepository.deleteById(id);
    }

    /**
     * Actualiza una persona existente
     * @param id el ID de la persona a actualizar
     * @param persona los nuevos datos
     * @return la persona actualizada
     * @throws RuntimeException si no existe la persona
     */
    public Persona actualizar(Long id, Persona persona) {
        return personaRepository.findById(id)
                .map(personaExistente -> {
                    personaExistente.setNombre(persona.getNombre());
                    personaExistente.setEmail(persona.getEmail());
                    personaExistente.setEdad(persona.getEdad());
                    return personaRepository.save(personaExistente);
                })
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
    }
}