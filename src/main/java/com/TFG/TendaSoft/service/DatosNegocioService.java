package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.repository.DatosNegocioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Transactional
    public DatosNegocio guardarConfiguracionConLogo(String nombreEmpresa, String cif,
                                                    String direccion, String mensajeTicket,
                                                    Boolean verifactuActivado, MultipartFile logo) {

        // 1. Recuperamos la configuración actual o creamos una nueva forzando el ID a 1
        DatosNegocio config = obtenerConfiguracion();
        config.setId(1);
        config.setNombreEmpresa(nombreEmpresa);
        config.setCif(cif);
        config.setDireccion(direccion);
        config.setMensajeTicket(mensajeTicket);
        config.setVerifactuActivado(verifactuActivado);

        // 2. Si viene un logo nuevo, lo procesamos
        if (logo != null && !logo.isEmpty()) {
            try {
                // Comprobamos la carpeta uploads
                File directorio = new File("uploads");
                if (!directorio.exists()) {
                    directorio.mkdirs();
                }

                // Generamos un nombre seguro para el logo (ej: logo_tienda.png)
                // Usamos currentTimeMillis para que si subes otro logo distinto mañana, no se pisen en la caché del navegador
                String nombreOriginalLimpio = logo.getOriginalFilename().replaceAll("\\s+", "_");
                String nombreArchivo = "logo_" + System.currentTimeMillis() + "_" + nombreOriginalLimpio;

                Path rutaCompleta = Paths.get("uploads" + File.separator + nombreArchivo);

                // Guardamos el binario
                Files.write(rutaCompleta, logo.getBytes());

                // Guardamos SOLO la ruta lógica
                config.setRutaLogo(nombreArchivo);

            } catch (IOException e) {
                throw new RuntimeException("No se pudo guardar el archivo de logo en el servidor.", e);
            }
        }

        // 3. Guardamos en Base de Datos
        return datosNegocioRepository.save(config);
    }
}