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
    public DatosNegocio guardarConfiguracion(String nombreEmpresa, String cif,
                                             String direccion, String mensajeTicket,
                                             Boolean verifactuActivado,
                                             MultipartFile logo,
                                             MultipartFile certificado) { // <-- Recibimos ambos archivos

        // 1. Recuperamos la configuración actual o creamos una nueva
        DatosNegocio config = obtenerConfiguracion();
        config.setId(1); // Forzamos que siempre sea la fila 1
        config.setNombreEmpresa(nombreEmpresa);
        config.setCif(cif);
        config.setDireccion(direccion);
        config.setMensajeTicket(mensajeTicket);
        config.setVerifactuActivado(verifactuActivado);

        // 2. Si viene un logo nuevo, lo procesamos usando el método auxiliar
        if (logo != null && !logo.isEmpty()) {
            String rutaLogo = guardarArchivoFisico(logo, "logo_");
            config.setRutaLogo(rutaLogo);
        }

        // 3. Si viene un certificado nuevo, lo procesamos
        if (certificado != null && !certificado.isEmpty()) {
            String rutaCert = guardarArchivoFisico(certificado, "cert_");
            config.setRutaCertificado(rutaCert);
        }

        // 4. Guardamos en Base de Datos
        return datosNegocioRepository.save(config);
    }

    // --- MÉTODO AUXILIAR PARA NO REPETIR CÓDIGO ---
    private String guardarArchivoFisico(MultipartFile archivo, String prefijo) {
        try {
            // Comprobamos la carpeta uploads
            File directorio = new File("uploads");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            // Generamos un nombre seguro para el archivo
            String nombreOriginalLimpio = archivo.getOriginalFilename().replaceAll("\\s+", "_");
            String nombreArchivo = prefijo + System.currentTimeMillis() + "_" + nombreOriginalLimpio;

            Path rutaCompleta = Paths.get("uploads" + File.separator + nombreArchivo);

            // Guardamos el binario
            Files.write(rutaCompleta, archivo.getBytes());

            // Devolvemos el nombre generado para guardarlo en la base de datos
            return nombreArchivo;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar el archivo " + archivo.getOriginalFilename() + " en el servidor.", e);
        }
    }
}