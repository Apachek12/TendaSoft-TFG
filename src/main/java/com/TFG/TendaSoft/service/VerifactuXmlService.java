package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.utils.VerifactuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class VerifactuXmlService {

    // Datos del fabricante del software
    private static final String NIF_FABRICANTE    = "A39200019";
    private static final String NOMBRE_FABRICANTE = "CERTIFICADO ENTIDAD PRUEBAS";
    private static final String ID_SISTEMA        = "01";
    private static final String VERSION_SISTEMA   = "1.0";
    private static final String NUM_INSTALACION   = "01";

    public String generarXmlAltaFactura(Venta venta, List<LineaVenta> lineas, DatosNegocio negocio) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatoIso   = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        String fechaExpedicion = venta.getFecha().format(formatoFecha);
        String fechaHoraHuso   = venta.getFecha().atZone(ZoneId.systemDefault()).format(formatoIso);

        // Cálculo huella SHA-256 antes de generar el XML
        String huella = VerifactuUtils.generarHashVerifactu(
                negocio.getCif(), venta.getNumeroFactura(), venta.getFecha(),
                venta.getTipoFactura(), venta.getCuotaIvaTotal(), venta.getTotal(),
                venta.getHashAnterior(), fechaHoraHuso
        );
        venta.setHashVerifactu(huella);

        String desglose       = generarBloqueDesglose(lineas);
        String encadenamiento = generarBloqueEncadenamiento(venta, formatoFecha);
        String cuotaTotal     = String.format(Locale.US, "%.2f", venta.getCuotaIvaTotal());
        String importeTotal   = String.format(Locale.US, "%.2f", venta.getTotal());

        // Estructura oficial RegFactuSistemaFacturacion
        return "<sum:RegFactuSistemaFacturacion" +
                " xmlns:sum=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd\"" +
                " xmlns:sum1=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd\"" +
                " xmlns:xd=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                "  <sum:Cabecera>\n" +
                "    <sum1:ObligadoEmision>\n" +
                "      <sum1:NombreRazon>" + negocio.getNombreEmpresa() + "</sum1:NombreRazon>\n" +
                "      <sum1:NIF>" + negocio.getCif() + "</sum1:NIF>\n" +
                "    </sum1:ObligadoEmision>\n" +
                "  </sum:Cabecera>\n" +
                "  <sum:RegistroFactura>\n" +
                "    <sum1:RegistroAlta>\n" +
                "      <sum1:IDVersion>1.0</sum1:IDVersion>\n" +
                "      <sum1:IDFactura>\n" +
                "        <sum1:IDEmisorFactura>" + negocio.getCif() + "</sum1:IDEmisorFactura>\n" +
                "        <sum1:NumSerieFactura>" + venta.getNumeroFactura() + "</sum1:NumSerieFactura>\n" +
                "        <sum1:FechaExpedicionFactura>" + fechaExpedicion + "</sum1:FechaExpedicionFactura>\n" +
                "      </sum1:IDFactura>\n" +
                "      <sum1:NombreRazonEmisor>" + negocio.getNombreEmpresa() + "</sum1:NombreRazonEmisor>\n" +
                "      <sum1:TipoFactura>" + venta.getTipoFactura() + "</sum1:TipoFactura>\n" +
                "      <sum1:DescripcionOperacion>Venta</sum1:DescripcionOperacion>\n" +
                "      <sum1:Desglose>\n" +
                desglose +
                "      </sum1:Desglose>\n" +
                "      <sum1:CuotaTotal>" + cuotaTotal + "</sum1:CuotaTotal>\n" +
                "      <sum1:ImporteTotal>" + importeTotal + "</sum1:ImporteTotal>\n" +
                encadenamiento +
                "      <sum1:SistemaInformatico>\n" +
                "        <sum1:NombreRazon>" + NOMBRE_FABRICANTE + "</sum1:NombreRazon>\n" +
                "        <sum1:NIF>" + NIF_FABRICANTE + "</sum1:NIF>\n" +
                "        <sum1:NombreSistemaInformatico>TendaSoft POS</sum1:NombreSistemaInformatico>\n" +
                "        <sum1:IdSistemaInformatico>" + ID_SISTEMA + "</sum1:IdSistemaInformatico>\n" +
                "        <sum1:Version>" + VERSION_SISTEMA + "</sum1:Version>\n" +
                "        <sum1:NumeroInstalacion>" + NUM_INSTALACION + "</sum1:NumeroInstalacion>\n" +
                "        <sum1:TipoUsoPosibleSoloVerifactu>S</sum1:TipoUsoPosibleSoloVerifactu>\n" +
                "        <sum1:TipoUsoPosibleMultiOT>N</sum1:TipoUsoPosibleMultiOT>\n" +
                "        <sum1:IndicadorMultiplesOT>N</sum1:IndicadorMultiplesOT>\n" +
                "      </sum1:SistemaInformatico>\n" +
                "      <sum1:FechaHoraHusoGenRegistro>" + fechaHoraHuso + "</sum1:FechaHoraHusoGenRegistro>\n" +
                "      <sum1:TipoHuella>01</sum1:TipoHuella>\n" +
                "      <sum1:Huella>" + huella + "</sum1:Huella>\n" +
                "    </sum1:RegistroAlta>\n" +
                "  </sum:RegistroFactura>\n" +
                "</sum:RegFactuSistemaFacturacion>";
    }

    // Validar el XML generado contra el esquema XSD oficial de la AEAT
    public boolean validarXmlContraEsquema(String xml) {
        try (InputStream xsdStream = getClass().getResourceAsStream("/schemas/SuministroLR.xsd")) {
            if (xsdStream == null) {
                log.warn("XSD no encontrado en resources/schemas/ — validación omitida");
                return true;
            }
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdStream));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml)));
            log.info("XML validado correctamente contra esquema XSD de la AEAT");
            return true;
        } catch (SAXException e) {
            log.error("XML no cumple el esquema XSD: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("Error al leer el archivo XSD: {}", e.getMessage());
            return false;
        }
    }

    // Primera factura: PrimerRegistro=S. Siguientes: encadenan con datos del registro anterior.
    private String generarBloqueEncadenamiento(Venta venta, DateTimeFormatter formatoFecha) {
        String hash = venta.getHashAnterior();

        if (hash == null || hash.equals("INICIO-SISTEMA")) {
            return "      <sum1:Encadenamiento>\n" +
                    "        <sum1:PrimerRegistro>S</sum1:PrimerRegistro>\n" +
                    "      </sum1:Encadenamiento>\n";
        }

        String fechaAnterior = venta.getFechaAnterior() != null
                ? venta.getFechaAnterior().format(formatoFecha)
                : "";

        return "      <sum1:Encadenamiento>\n" +
                "        <sum1:RegistroAnterior>\n" +
                "          <sum1:IDEmisorFactura>" + venta.getCifEmisorAnterior() + "</sum1:IDEmisorFactura>\n" +
                "          <sum1:NumSerieFactura>" + venta.getNumeroFacturaAnterior() + "</sum1:NumSerieFactura>\n" +
                "          <sum1:FechaExpedicionFactura>" + fechaAnterior + "</sum1:FechaExpedicionFactura>\n" +
                "          <sum1:Huella>" + hash + "</sum1:Huella>\n" +
                "        </sum1:RegistroAnterior>\n" +
                "      </sum1:Encadenamiento>\n";
    }

    // Agrupa las líneas por tipo de IVA para generar un DetalleDesglose por cada tramo
    private String generarBloqueDesglose(List<LineaVenta> lineas) {
        Map<BigDecimal, BigDecimal[]> agrupacion = new HashMap<>();

        for (LineaVenta linea : lineas) {
            BigDecimal porc = linea.getPorcentajeIva();
            agrupacion.putIfAbsent(porc, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal base = linea.getPrecioUnitario().multiply(new BigDecimal(linea.getCantidad()));
            agrupacion.get(porc)[0] = agrupacion.get(porc)[0].add(base);
            agrupacion.get(porc)[1] = agrupacion.get(porc)[1].add(linea.getImporteIva());
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<BigDecimal, BigDecimal[]> entry : agrupacion.entrySet()) {
            sb.append("        <sum1:DetalleDesglose>\n")
                    .append("          <sum1:ClaveRegimen>01</sum1:ClaveRegimen>\n")
                    .append("          <sum1:CalificacionOperacion>S1</sum1:CalificacionOperacion>\n")
                    .append("          <sum1:TipoImpositivo>").append(String.format(Locale.US, "%.2f", entry.getKey())).append("</sum1:TipoImpositivo>\n")
                    .append("          <sum1:BaseImponibleOimporteNoSujeto>").append(String.format(Locale.US, "%.2f", entry.getValue()[0])).append("</sum1:BaseImponibleOimporteNoSujeto>\n")
                    .append("          <sum1:CuotaRepercutida>").append(String.format(Locale.US, "%.2f", entry.getValue()[1])).append("</sum1:CuotaRepercutida>\n")
                    .append("        </sum1:DetalleDesglose>\n");
        }
        return sb.toString();
    }
}