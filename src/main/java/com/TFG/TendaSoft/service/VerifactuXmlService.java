package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Venta;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VerifactuXmlService {

    // Plantilla base del XML ORDENADA según el esquema xs:sequence de la AEAT
    // Plantilla base del XML ORDENADA según el esquema xs:sequence de la AEAT
    private static final String PLANTILLA_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <sum1:RegFactuSistemaFacturacion xmlns:sum1="https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd" xmlns:sum="https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd">
                <sum1:Cabecera>
                    <sum:ObligadoEmision>
                        <sum:NombreRazon>%s</sum:NombreRazon>
                        <sum:NIF>%s</sum:NIF>
                    </sum:ObligadoEmision>
                </sum1:Cabecera>
                <sum1:RegistroFactura>
                    <sum:RegistroAlta>
                        <sum:IDVersion>1.0</sum:IDVersion>
                        <sum:IDFactura>
                            <sum:IDEmisorFactura>%s</sum:IDEmisorFactura>
                            <sum:NumSerieFactura>%s</sum:NumSerieFactura>
                            <sum:FechaExpedicionFactura>%s</sum:FechaExpedicionFactura>
                        </sum:IDFactura>
                        <sum:NombreRazonEmisor>%s</sum:NombreRazonEmisor>
                        <sum:TipoFactura>%s</sum:TipoFactura>
                        <sum:DescripcionOperacion>Venta en TPV</sum:DescripcionOperacion>
                        <sum:Desglose>
            %s
                        </sum:Desglose>
                        <sum:CuotaTotal>%.2f</sum:CuotaTotal>
                        <sum:ImporteTotal>%.2f</sum:ImporteTotal>
                        <sum:Encadenamiento>
                            <sum:PrimerRegistro>S</sum:PrimerRegistro>
                        </sum:Encadenamiento>
                        <sum:SistemaInformatico>
                            <sum:NombreRazon>TendaSoft TFG</sum:NombreRazon>
                            <sum:NIF>%s</sum:NIF>
                            <sum:NombreSistemaInformatico>TendaSoft POS</sum:NombreSistemaInformatico>
                            <sum:IdSistemaInformatico>01</sum:IdSistemaInformatico>
                            <sum:Version>1.0</sum:Version>
                            <sum:NumeroInstalacion>01</sum:NumeroInstalacion>
                            <sum:TipoUsoPosibleSoloVerifactu>S</sum:TipoUsoPosibleSoloVerifactu>
                            <sum:TipoUsoPosibleMultiOT>N</sum:TipoUsoPosibleMultiOT>
                            <sum:IndicadorMultiplesOT>N</sum:IndicadorMultiplesOT>
                        </sum:SistemaInformatico>
                        <sum:FechaHoraHusoGenRegistro>%s</sum:FechaHoraHusoGenRegistro>
                        <sum:TipoHuella>01</sum:TipoHuella>
                        <sum:Huella>%s</sum:Huella>
                    </sum:RegistroAlta>
                </sum1:RegistroFactura>
            </sum1:RegFactuSistemaFacturacion>
            """;


    public String generarXmlAltaFactura(Venta venta, List<LineaVenta> lineas, DatosNegocio negocio) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatoHoraIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                .withZone(ZoneId.systemDefault());

        String fechaExpedicion = venta.getFecha().format(formatoFecha);
        String fechaHoraRegistro = venta.getFecha().atZone(ZoneId.systemDefault()).format(formatoHoraIso);

        String bloqueDesglose = generarBloqueDesglose(lineas);

        return String.format(Locale.US, PLANTILLA_XML,
                negocio.getNombreEmpresa(), // 1. Cabecera Nombre
                negocio.getCif(),           // 2. Cabecera NIF
                negocio.getCif(),           // 3. IDFactura NIF Emisor
                venta.getNumeroFactura(),   // 4. IDFactura Numero
                fechaExpedicion,            // 5. IDFactura Fecha
                negocio.getNombreEmpresa(), // 6. NombreRazonEmisor
                venta.getTipoFactura(),     // 7. TipoFactura
                bloqueDesglose,             // 8. Desglose
                venta.getCuotaIvaTotal(),   // 9. CuotaTotal (Obligatorio)
                venta.getTotal(),           // 10. ImporteTotal
                negocio.getCif(),           // 11. SistemaInformatico NIF
                fechaHoraRegistro,          // 12. FechaHora Registro
                venta.getHashVerifactu()    // 13. Huella actual
        );
    }

    private String generarBloqueDesglose(List<LineaVenta> lineas) {
        StringBuilder desgloseXml = new StringBuilder();
        Map<BigDecimal, BigDecimal[]> agrupacionIva = new HashMap<>();

        for (LineaVenta linea : lineas) {
            BigDecimal porc = linea.getPorcentajeIva();
            agrupacionIva.putIfAbsent(porc, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal cantidad = new BigDecimal(linea.getCantidad());
            BigDecimal baseLinea = linea.getPrecioUnitario().multiply(cantidad);
            agrupacionIva.get(porc)[0] = agrupacionIva.get(porc)[0].add(baseLinea);
            agrupacionIva.get(porc)[1] = agrupacionIva.get(porc)[1].add(linea.getImporteIva());
        }

        // Estructura y orden exacto según el XSD: Clave, Calificacion, TipoImpositivo, Base, Cuota
        for (Map.Entry<BigDecimal, BigDecimal[]> entry : agrupacionIva.entrySet()) {
            desgloseXml.append(String.format(Locale.US, """
                            <sum:DetalleDesglose>
                                <sum:ClaveRegimen>01</sum:ClaveRegimen>
                                <sum:CalificacionOperacion>S1</sum:CalificacionOperacion>
                                <sum:TipoImpositivo>%.2f</sum:TipoImpositivo>
                                <sum:BaseImponibleOimporteNoSujeto>%.2f</sum:BaseImponibleOimporteNoSujeto>
                                <sum:CuotaRepercutida>%.2f</sum:CuotaRepercutida>
                            </sum:DetalleDesglose>""",
                    entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        return desgloseXml.toString();
    }

    public boolean validarXmlContraEsquema(String xml) {
        try {
            ClassPathResource xsdResource = new ClassPathResource("schemas/SuministroLR.xsd");
            if (!xsdResource.exists()) {
                System.out.println("ATENCIÓN: No se ha encontrado el archivo XSD. Saltando validación.");
                return true;
            }
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdResource.getFile());
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
            System.out.println("El XML es VÁLIDO según el esquema de Hacienda.");
            return true;
        } catch (SAXException e) {
            System.err.println("ERROR DE ESTRUCTURA XML: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("ERROR DE LECTURA.");
            return false;
        }
    }
}