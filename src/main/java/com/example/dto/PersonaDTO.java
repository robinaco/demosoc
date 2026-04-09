package com.example.dto;

import lombok.Data;

@Data
public class PersonaDTO {
    private Long id;
    private String nombre;
    private String email;
    private Integer edad;


    public PersonaDTO() {

    }
}