package com.TFG.TendaSoft.repository;

import com.TFG.TendaSoft.config.EncriptadoConfig;
import com.TFG.TendaSoft.model.DatosNegocio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(EncriptadoConfig.class)
@TestPropertySource(properties = {
        "app.encriptado.clave=ClaveTFG2026",
        "app.encriptado.salt=746bbef00edea081"
})
class DatosNegocioRepositoryTest {

    @Autowired
    private DatosNegocioRepository datosNegocioRepository;

    // El converter cifra al guardar y descifra al leer — el test verifica el ciclo completo
    @Test
    void guardarYRecuperar_debeDescifrarPasswordCorrecto() {
        DatosNegocio negocio = new DatosNegocio();
        negocio.setNombreEmpresa("Test S.L.");
        negocio.setCif("A39200019");
        negocio.setDireccion("Calle Test 1");
        negocio.setVerifactuActivado(true);
        negocio.setCertificadoPassword("MiPasswordSecreto123");

        DatosNegocio guardado = datosNegocioRepository.save(negocio);
        datosNegocioRepository.flush();

        DatosNegocio recuperado = datosNegocioRepository.findById(guardado.getId()).orElseThrow();

        // El password debe llegar descifrado — igual al original
        assertEquals("MiPasswordSecreto123", recuperado.getCertificadoPassword());
    }

    @Test
    void guardarConPasswordNulo_noDebeLanzarExcepcion() {
        DatosNegocio negocio = new DatosNegocio();
        negocio.setNombreEmpresa("Test S.L.");
        negocio.setCif("A39200019");
        negocio.setDireccion("Calle Test 1");
        negocio.setVerifactuActivado(false);
        negocio.setCertificadoPassword(null);

        assertDoesNotThrow(() -> {
            DatosNegocio guardado = datosNegocioRepository.save(negocio);
            assertNull(guardado.getCertificadoPassword());
        });
    }

    @Test
    void findTopByOrderByIdAsc_debeDevolverPrimeroInsertado() {
        DatosNegocio primero = new DatosNegocio();
        primero.setNombreEmpresa("Primero S.L.");
        primero.setCif("A00000001");
        primero.setDireccion("Dir 1");
        primero.setVerifactuActivado(false);
        datosNegocioRepository.save(primero);

        DatosNegocio segundo = new DatosNegocio();
        segundo.setNombreEmpresa("Segundo S.L.");
        segundo.setCif("A00000002");
        segundo.setDireccion("Dir 2");
        segundo.setVerifactuActivado(false);
        datosNegocioRepository.save(segundo);

        DatosNegocio resultado = datosNegocioRepository.findTopByOrderByIdAsc().orElseThrow();
        assertEquals("Primero S.L.", resultado.getNombreEmpresa());
    }
}