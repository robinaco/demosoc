package com.example.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PersonaDTO {
    private Long id;
    private String nombre;
    private String email;
    private Integer edad;
    
}