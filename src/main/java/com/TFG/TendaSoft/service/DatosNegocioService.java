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
import java.nio.file.StandardCopyOption;

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
    public DatosNegocio guardarConfiguracion(String nombre, String cif, String dir, String msg,
                                             Boolean vf, String pass, MultipartFile logo,
                                             MultipartFile cert) throws IOException {

        DatosNegocio n = datosNegocioRepository.findTopByOrderByIdAsc().orElse(new DatosNegocio());

        n.setNombreEmpresa(nombre);
        n.setCif(cif);
        n.setDireccion(dir);
        n.setMensajeTicket(msg);
        n.setVerifactuActivado(vf);
        if (pass != null && !pass.isEmpty()) n.setCertificadoPassword(pass);

        // Directorios de almacenamiento
        String uploadDir = "uploads/";
        String certDir = "config/certs/";
        new File(uploadDir).mkdirs();
        new File(certDir).mkdirs();

        if (logo != null && !logo.isEmpty()) {
            String fileName = "logo_" + System.currentTimeMillis() + "_" + logo.getOriginalFilename();
            Files.copy(logo.getInputStream(), Paths.get(uploadDir + fileName), StandardCopyOption.REPLACE_EXISTING);
            n.setRutaLogo(fileName);
        }

        if (cert != null && !cert.isEmpty()) {
            String certName = "cert_" + System.currentTimeMillis() + ".p12";
            Path path = Paths.get(certDir + certName);
            Files.copy(cert.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            n.setRutaCertificado(path.toAbsolutePath().toString());
        }

        return datosNegocioRepository.save(n);
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