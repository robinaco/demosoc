package com.example.sonardemo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersonaTest {

    @Test
    void testGettersAndSetters() {
        Persona persona = new Persona();

        persona.setId(1L);
        persona.setNombre("Carlos");
        persona.setEmail("carlos@email.com");
        persona.setEdad(28);

        assertEquals(1L, persona.getId());
        assertEquals("Carlos", persona.getNombre());
        assertEquals("carlos@email.com", persona.getEmail());
        assertEquals(28, persona.getEdad());
    }

    @Test
    void testConstructorConParametros() {
        Persona persona = new Persona(1L, "Ana", "ana@email.com", 25);

        assertEquals(1L, persona.getId());
        assertEquals("Ana", persona.getNombre());
        assertEquals("ana@email.com", persona.getEmail());
        assertEquals(25, persona.getEdad());
    }

    @Test
    void testConstructorVacio() {
        Persona persona = new Persona();
        assertNotNull(persona);
    }



    @Test
    void testEqualsAndHashCode() {
        Persona persona1 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona2 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona3 = new Persona(2L, "Pedro", "pedro@email.com", 25);

        assertEquals(persona1, persona2);  // Mismos datos, deben ser iguales
        assertNotEquals(persona1, persona3); // Diferentes, no deben ser iguales
        assertEquals(persona1.hashCode(), persona2.hashCode());
        assertNotEquals(persona1.hashCode(), persona3.hashCode());
    }

    @Test
    void testToString() {
        Persona persona = new Persona(1L, "Juan", "juan@email.com", 30);
        String toString = persona.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("juan@email.com"));
    }

    @Test
    void testCanEqual() {
        Persona persona = new Persona();
        assertTrue(persona.canEqual(new Persona()));
        assertFalse(persona.canEqual(new Object()));
    }

    // Prueba para asegurar que todos los métodos de JPA están presentes
    @Test
    void testJpaMethods() throws NoSuchMethodException {
        Persona persona = new Persona();

        // Verificar que tiene los métodos que JPA necesita
        assertNotNull(Persona.class.getMethod("getId"));
        assertNotNull(Persona.class.getMethod("getNombre"));
        assertNotNull(Persona.class.getMethod("getEmail"));
        assertNotNull(Persona.class.getMethod("getEdad"));

        assertNotNull(Persona.class.getMethod("setId", Long.class));
        assertNotNull(Persona.class.getMethod("setNombre", String.class));
        assertNotNull(Persona.class.getMethod("setEmail", String.class));
        assertNotNull(Persona.class.getMethod("setEdad", Integer.class));
    }

    @Test
    void testEquals_SameObject() {
        Persona persona = new Persona(1L, "Juan", "juan@email.com", 30);
        assertEquals(persona, persona);  // Mismo objeto
    }

    @Test
    void testEquals_Null() {
        Persona persona = new Persona(1L, "Juan", "juan@email.com", 30);
        assertNotEquals(null, persona);
    }

    @Test
    void testEquals_DifferentClass() {
        Persona persona = new Persona(1L, "Juan", "juan@email.com", 30);
        String texto = "test";
        assertNotEquals(persona, texto);
    }

    @Test
    void testEquals_DifferentId() {
        Persona persona1 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona2 = new Persona(2L, "Juan", "juan@email.com", 30);
        assertNotEquals(persona1, persona2);
    }

    @Test
    void testEquals_DifferentNombre() {
        Persona persona1 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona2 = new Persona(1L, "Pedro", "juan@email.com", 30);
        assertNotEquals(persona1, persona2);
    }

    @Test
    void testEquals_DifferentEmail() {
        Persona persona1 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona2 = new Persona(1L, "Juan", "pedro@email.com", 30);
        assertNotEquals(persona1, persona2);
    }

    @Test
    void testEquals_DifferentEdad() {
        Persona persona1 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona2 = new Persona(1L, "Juan", "juan@email.com", 31);
        assertNotEquals(persona1, persona2);
    }

    @Test
    void testHashCode_Consistency() {
        Persona persona = new Persona(1L, "Juan", "juan@email.com", 30);
        int hashCode1 = persona.hashCode();
        int hashCode2 = persona.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCode_DifferentObjects() {
        Persona persona1 = new Persona(1L, "Juan", "juan@email.com", 30);
        Persona persona2 = new Persona(1L, "Juan", "juan@email.com", 30);
        assertEquals(persona1.hashCode(), persona2.hashCode());
    }
}