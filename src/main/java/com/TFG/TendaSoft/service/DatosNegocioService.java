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
        return datosNegocioRepository.findTopByOrderByIdAsc().orElseGet(() -> {
            DatosNegocio defaults = new DatosNegocio();
            defaults.setNombreEmpresa("Mi Tienda");
            defaults.setCif("00000000T");
            defaults.setDireccion("Dirección por defecto");
            defaults.setVerifactuActivado(false);
            return datosNegocioRepository.save(defaults);
        });
    }

    @Transactional
    public DatosNegocio guardarConfiguracion(String nombre, String cif, String direccion,
                                             String mensajeTicket, Boolean verifactuActivado,
                                             String certificadoPassword,
                                             MultipartFile logo, MultipartFile certificado) throws IOException {

        DatosNegocio negocio = datosNegocioRepository.findTopByOrderByIdAsc().orElse(new DatosNegocio());
        negocio.setNombreEmpresa(nombre);
        negocio.setCif(cif);
        negocio.setDireccion(direccion);
        negocio.setMensajeTicket(mensajeTicket);
        negocio.setVerifactuActivado(verifactuActivado);

        if (certificadoPassword != null && !certificadoPassword.isEmpty()) {
            negocio.setCertificadoPassword(certificadoPassword);
        }

        String uploadDir = "uploads/";
        String certDir   = "config/certs/";
        new File(uploadDir).mkdirs();
        new File(certDir).mkdirs();

        if (logo != null && !logo.isEmpty()) {
            String nombreLogo = "logo_" + System.currentTimeMillis() + "_" + logo.getOriginalFilename();
            Files.copy(logo.getInputStream(), Paths.get(uploadDir + nombreLogo), StandardCopyOption.REPLACE_EXISTING);
            negocio.setRutaLogo(nombreLogo);
        }

        if (certificado != null && !certificado.isEmpty()) {
            String nombreCert = "cert_" + System.currentTimeMillis() + ".p12";
            Path rutaCert = Paths.get(certDir + nombreCert);
            Files.copy(certificado.getInputStream(), rutaCert, StandardCopyOption.REPLACE_EXISTING);
            negocio.setRutaCertificado(rutaCert.toAbsolutePath().toString());
        }

        return datosNegocioRepository.save(negocio);
    }
}