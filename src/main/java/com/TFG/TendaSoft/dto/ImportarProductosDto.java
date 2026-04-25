package com.TFG.TendaSoft.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImportarProductosDto {
    private List<String> codigosBarras;
}