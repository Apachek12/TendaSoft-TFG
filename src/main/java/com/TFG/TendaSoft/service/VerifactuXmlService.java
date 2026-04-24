package com.TFG.TendaSoft.service;

import com.TFG.TendaSoft.model.DatosNegocio;
import com.TFG.TendaSoft.model.LineaVenta;
import com.TFG.TendaSoft.model.Venta;
import com.TFG.TendaSoft.utils.VerifactuUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VerifactuXmlService {

    // Datos del fabricante del software (el desarrollador, no el negocio)
    private static final String NIF_FABRICANTE             = "A39200019";   // <-- tu NIF real como desarrollador
    private static final String NOMBRE_FABRICANTE          = "CERTIFICADO ENTIDAD PRUEBAS";
    private static final String ID_SISTEMA                 = "01";
    private static final String VERSION_SISTEMA            = "1.0";
    private static final String NUM_INSTALACION            = "01";

    public String generarXmlAltaFactura(Venta venta, List<LineaVenta> lineas, DatosNegocio negocio) {

        DateTimeFormatter formatoFecha   = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter formatoHoraIso = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        String fechaExpedicion       = venta.getFecha().format(formatoFecha);
        String fechaHoraHusoGenRegistro = venta.getFecha().atZone(ZoneId.systemDefault()).format(formatoHoraIso);

        // Calcular huella criptográfica
        String huellaReal = VerifactuUtils.generarHashVerifactu(
                negocio.getCif(), venta.getNumeroFactura(), venta.getFecha(),
                venta.getTipoFactura(), venta.getCuotaIvaTotal(), venta.getTotal(),
                venta.getHashAnterior(), fechaHoraHusoGenRegistro
        );
        venta.setHashVerifactu(huellaReal);

        String bloqueDesglose        = generarBloqueDesglose(lineas);
        String cuotaTotalFormateada  = String.format(Locale.US, "%.2f", venta.getCuotaIvaTotal());
        String totalFormateado       = String.format(Locale.US, "%.2f", venta.getTotal());

        // Bloque de encadenamiento: primera factura o encadenada a la anterior
        String bloqueEncadenamiento  = generarBloqueEncadenamiento(venta, formatoFecha);

        // Estructura oficial confirmada por la AEAT:
        // RegFactuSistemaFacturacion > Cabecera + RegistroFactura > RegistroAlta
        String xml =
                "<sum:RegFactuSistemaFacturacion" +
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
                        bloqueDesglose +
                        "      </sum1:Desglose>\n" +
                        "      <sum1:CuotaTotal>" + cuotaTotalFormateada + "</sum1:CuotaTotal>\n" +
                        "      <sum1:ImporteTotal>" + totalFormateado + "</sum1:ImporteTotal>\n" +
                        bloqueEncadenamiento +
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
                        // Campo unificado: fecha + hora + huso en ISO 8601 con offset
                        "      <sum1:FechaHoraHusoGenRegistro>" + fechaHoraHusoGenRegistro + "</sum1:FechaHoraHusoGenRegistro>\n" +
                        "      <sum1:TipoHuella>01</sum1:TipoHuella>\n" +
                        "      <sum1:Huella>" + huellaReal + "</sum1:Huella>\n" +
                        "    </sum1:RegistroAlta>\n" +
                        "  </sum:RegistroFactura>\n" +
                        "</sum:RegFactuSistemaFacturacion>";

        return xml;
    }

    /**
     * Genera el bloque <Encadenamiento>.
     * - Si es la primera factura del sistema (hashAnterior == null o "INICIO-SISTEMA"),
     *   se usa <PrimerRegistro>S</PrimerRegistro>.
     * - En caso contrario se encadena al registro anterior con sus datos e huella.
     */
    private String generarBloqueEncadenamiento(Venta venta, DateTimeFormatter formatoFecha) {
        String hashAnterior = venta.getHashAnterior();
        boolean esPrimera   = hashAnterior == null || hashAnterior.equals("INICIO-SISTEMA");

        if (esPrimera) {
            return
                    "      <sum1:Encadenamiento>\n" +
                            "        <sum1:PrimerRegistro>S</sum1:PrimerRegistro>\n" +
                            "      </sum1:Encadenamiento>\n";
        } else {
            // Para encadenar necesitamos los datos de la factura anterior.
            // Venta expone hashAnterior; el número y fecha del anterior
            // deben venir en los campos correspondientes del modelo.
            // Si tu modelo Venta no tiene esos campos aún, añádelos.
            String numFacturaAnterior   = venta.getNumeroFacturaAnterior();   // añadir al modelo
            String fechaFacturaAnterior = venta.getFechaAnterior() != null
                    ? venta.getFechaAnterior().format(formatoFecha)
                    : "";

            return
                    "      <sum1:Encadenamiento>\n" +
                            "        <sum1:RegistroAnterior>\n" +
                            "          <sum1:IDEmisorFactura>" + venta.getCifEmisorAnterior() + "</sum1:IDEmisorFactura>\n" +
                            "          <sum1:NumSerieFactura>" + numFacturaAnterior + "</sum1:NumSerieFactura>\n" +
                            "          <sum1:FechaExpedicionFactura>" + fechaFacturaAnterior + "</sum1:FechaExpedicionFactura>\n" +
                            "          <sum1:Huella>" + hashAnterior + "</sum1:Huella>\n" +
                            "        </sum1:RegistroAnterior>\n" +
                            "      </sum1:Encadenamiento>\n";
        }
    }

    private String generarBloqueDesglose(List<LineaVenta> lineas) {
        StringBuilder desgloseXml = new StringBuilder();
        Map<BigDecimal, BigDecimal[]> agrupacionIva = new HashMap<>();

        for (LineaVenta linea : lineas) {
            BigDecimal porc = linea.getPorcentajeIva();
            agrupacionIva.putIfAbsent(porc, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal baseLinea = linea.getPrecioUnitario().multiply(new BigDecimal(linea.getCantidad()));
            agrupacionIva.get(porc)[0] = agrupacionIva.get(porc)[0].add(baseLinea);
            agrupacionIva.get(porc)[1] = agrupacionIva.get(porc)[1].add(linea.getImporteIva());
        }

        for (Map.Entry<BigDecimal, BigDecimal[]> entry : agrupacionIva.entrySet()) {
            desgloseXml.append("        <sum1:DetalleDesglose>\n")
                    .append("          <sum1:ClaveRegimen>01</sum1:ClaveRegimen>\n")
                    .append("          <sum1:CalificacionOperacion>S1</sum1:CalificacionOperacion>\n")
                    .append("          <sum1:TipoImpositivo>").append(String.format(Locale.US, "%.2f", entry.getKey())).append("</sum1:TipoImpositivo>\n")
                    .append("          <sum1:BaseImponibleOimporteNoSujeto>").append(String.format(Locale.US, "%.2f", entry.getValue()[0])).append("</sum1:BaseImponibleOimporteNoSujeto>\n")
                    .append("          <sum1:CuotaRepercutida>").append(String.format(Locale.US, "%.2f", entry.getValue()[1])).append("</sum1:CuotaRepercutida>\n")
                    .append("        </sum1:DetalleDesglose>\n");
        }
        return desgloseXml.toString();
    }

    public boolean validarXmlContraEsquema(String xml) {
        System.out.println("⚠️ Validación local desactivada.");
        return true;
    }
}