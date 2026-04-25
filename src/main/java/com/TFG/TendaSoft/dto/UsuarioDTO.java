package com.TFG.TendaSoft.dto;

import lombok.Builder;
import lombok.Data;

// DTO de respuesta para el usuario — nunca expone el hash de la contraseña al frontend.
@Data
@Builder
public class UsuarioDTO {
    private Integer idUsuario;
    private String nombreReal;
    private String nombreUsuario;
    private String rol;
    private Boolean activo;
}