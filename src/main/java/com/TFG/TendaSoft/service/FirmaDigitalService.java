package com.TFG.TendaSoft.service;

import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Service
public class FirmaDigitalService {

    static {
        Init.init();
    }

    public String firmarXml(String xml, String rutaP12, String password) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(rutaP12)) {
                ks.load(fis, password.toCharArray());
            }

            String alias = ks.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            Element root = doc.getDocumentElement();

            // FIX: setAttribute() sola no registra el atributo como tipo ID en el DOM.
            // Apache Santuario usa doc.getElementById() internamente para resolver "#root",
            // y ese método solo funciona si el atributo está declarado como ID con
            // setIdAttribute(). Sin esta línea el resolver lanza ReferenceNotInitializedException.
            root.setAttribute("Id", "root");
            root.setIdAttribute("Id", true);

            XMLSignature sig = new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
            root.appendChild(sig.getElement());

            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            // FIX: Añadir canonicalización exclusiva (exc-c14n) para que la firma
            // sea estable cuando el XML se inserta dentro del SOAP envelope,
            // evitando que los namespaces heredados del envelope la invaliden.
            transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

            // FIX: Referenciar solo el elemento raíz por su ID, no todo el documento
            sig.addDocument("#root", transforms, "http://www.w3.org/2001/04/xmlenc#sha256");

            sig.addKeyInfo(cert);
            sig.sign(privateKey);

            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();

            // Omitir el prólogo XML — se añade manualmente al construir el SOAP envelope
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(
                    new javax.xml.transform.dom.DOMSource(doc),
                    new javax.xml.transform.stream.StreamResult(writer)
            );

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error al firmar el XML: " + e.getMessage(), e);
        }
    }
}