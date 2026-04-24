package com.TFG.TendaSoft.service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.nio.charset.StandardCharsets;

@Service
public class VerifactuHttpService {

    public String enviarFacturaAEAT(String xmlFirmado, String rutaCert, String passCert) throws Exception {

        // 1. Cargar el certificado .p12 (KeyStore)
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream instream = new FileInputStream(new File(rutaCert))) {
            keyStore.load(instream, passCert.toCharArray());
        }

        // 2. Crear el SSLContext con el material del certificado (mTLS)
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, passCert.toCharArray())
                .build();

        // 3. Crear la factoría de sockets SSL
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();

        // 4. Configurar el gestor de conexiones
        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        // 5. Construir el cliente HTTP
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build()) {

            String urlAeat = "https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP";
            HttpPost post = new HttpPost(urlAeat);
            post.setHeader("SOAPAction", "");

            // Limpiar prólogo XML para evitar doble declaración dentro del SOAP envelope
            String xmlLimpio = xmlFirmado
                    .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
                    .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "")
                    .trim();

            // Construcción del SOAP Envelope.
            // Los namespaces van en el Envelope raíz tal como exige la AEAT
            // (estructura confirmada oficialmente):
            String soapEnvelope =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<soapenv:Envelope\n" +
                            "  xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                            "  xmlns:sum=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd\"\n" +
                            "  xmlns:sum1=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd\"\n" +
                            "  xmlns:xd=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                            "  <soapenv:Header/>\n" +
                            "  <soapenv:Body>\n" +
                            "    " + xmlLimpio + "\n" +
                            "  </soapenv:Body>\n" +
                            "</soapenv:Envelope>";

            System.out.println("===============================================");
            System.out.println("SOAP ENVELOPE QUE SE ENVÍA A LA AEAT:");
            System.out.println("===============================================");
            System.out.println(soapEnvelope);
            System.out.println("===============================================");

            post.setEntity(new StringEntity(soapEnvelope, ContentType.create("text/xml", StandardCharsets.UTF_8)));

            // 6. Ejecutar y loguear la respuesta
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                String respuestaHacienda = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                System.out.println("===============================================");
                System.out.println("RESPUESTA OFICIAL DE LA AEAT:");
                System.out.println("===============================================");
                System.out.println(respuestaHacienda);
                System.out.println("===============================================");

                return respuestaHacienda;
            }
        }
    }
}