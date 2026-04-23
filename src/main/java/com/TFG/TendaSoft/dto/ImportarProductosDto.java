package com.TFG.TendaSoft.dto;

import lombok.Data;
import java.util.List;

@Data // Genera automáticamente Getters, Setters, toString, etc.
public class ImportarProductosDto {
    private List<String> codigosBarras;
}