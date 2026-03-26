package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.repository.DatosNegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatosNegocioService {

    private final DatosNegocioRepository datosNegocioRepository;

    public DatosNegocio obtenerConfiguracion() {
        // Buscamos la primera configuración que haya. Si no hay ninguna, devolvemos una vacía por defecto.
        return datosNegocioRepository.findAll().stream().findFirst().orElseGet(() -> {
            DatosNegocio defaultDatos = new DatosNegocio();
            defaultDatos.setNombreEmpresa("Mi Tienda");
            defaultDatos.setCif("00000000T");
            defaultDatos.setDireccion("Dirección por defecto");
            defaultDatos.setVerifactuActivado(false);
            return datosNegocioRepository.save(defaultDatos);
        });
    }

    public DatosNegocio actualizarConfiguracion(DatosNegocio datosActualizados) {
        DatosNegocio datosActuales = obtenerConfiguracion();

        // Actualizamos los campos
        datosActuales.setNombreEmpresa(datosActualizados.getNombreEmpresa());
        datosActuales.setCif(datosActualizados.getCif());
        datosActuales.setDireccion(datosActualizados.getDireccion());
        datosActuales.setMensajeTicket(datosActualizados.getMensajeTicket());
        datosActuales.setVerifactuActivado(datosActualizados.getVerifactuActivado());

        return datosNegocioRepository.save(datosActuales);
    }
}