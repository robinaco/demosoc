package com.example.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDTOTest {

    @Test
    void testGettersAndSetters() {
        PersonaDTO dto = new PersonaDTO();

        dto.setId(1L);
        dto.setNombre("Robinson");
        dto.setEmail("robinson@test.com");
        dto.setEdad(30);

        assertEquals(1L, dto.getId());
        assertEquals("Robinson", dto.getNombre());
        assertEquals("robinson@test.com", dto.getEmail());
        assertEquals(30, dto.getEdad());
    }

    @Test
    void testNoArgsConstructor() {
        PersonaDTO dto = new PersonaDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getNombre());
        assertNull(dto.getEmail());
        assertNull(dto.getEdad());
    }

    @Test
    void testEqualsAndHashCode() {
        PersonaDTO dto1 = new PersonaDTO();
        dto1.setId(1L);
        dto1.setNombre("Robinson");
        dto1.setEmail("robinson@test.com");
        dto1.setEdad(30);

        PersonaDTO dto2 = new PersonaDTO();
        dto2.setId(1L);
        dto2.setNombre("Robinson");
        dto2.setEmail("robinson@test.com");
        dto2.setEdad(30);

        PersonaDTO dto3 = new PersonaDTO();
        dto3.setId(2L);
        dto3.setNombre("Otro");
        dto3.setEmail("otro@test.com");
        dto3.setEdad(25);

        // Reflexividad
        assertEquals(dto1, dto1);

        // Simetría
        assertEquals(dto1, dto2);
        assertEquals(dto2, dto1);

        // Consistencia del hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());

        // Diferentes objetos
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        // Comparación con null
        assertNotEquals(dto1, null);

        // Comparación con otra clase
        assertNotEquals(dto1, "texto");
    }

    @Test
    void testToString() {
        PersonaDTO dto = new PersonaDTO();
        dto.setId(1L);
        dto.setNombre("Robinson");
        dto.setEmail("robinson@test.com");
        dto.setEdad(30);

        String toString = dto.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Robinson"));
        assertTrue(toString.contains("robinson@test.com"));
        assertTrue(toString.contains("30"));
    }
}