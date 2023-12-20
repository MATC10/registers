package com.example.repasoexamen2023javafx;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class Persona {
    private int id;
    private String nombre;
    private int edad;
    @JsonFormat(pattern = "yyyy/MM/dd") // en caso de que la fecha tenga este formato para el json
    private LocalDate fechaNacimiento;
    private String lenguaje;
    private boolean carrera;
}
