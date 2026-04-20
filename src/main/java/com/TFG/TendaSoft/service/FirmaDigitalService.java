package com.TFG.TendaSoft.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Service
public class FirmaDigitalService {

    /**
     * Firma un documento XML utilizando un certificado digital PKCS12 (.p12 o .pfx).
     *
     * @param xmlOriginal El XML en formato String validado.
     * @param rutaCertificado Ruta en el disco duro al archivo .p12 / .pfx.
     * @param passwordCertificado Contraseña del certificado.
     * @return El XML firmado en formato String.
     */
    public String firmarXml(String xmlOriginal, String rutaCertificado, String passwordCertificado) {
        try {
            // 1. Cargar el Certificado Digital y la Clave Privada
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(rutaCertificado)) {
                keyStore.load(fis, passwordCertificado.toCharArray());
            }

            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, passwordCertificado.toCharArray());
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

            // 2. Convertir el String XML a un Documento DOM de Java
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlOriginal.getBytes("UTF-8")));

            // 3. Configurar el motor de firma XMLDSig de Java
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Crear la referencia (apunta a la raíz del documento y usa la transformación ENVELOPED)
            Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null),
                    Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null, null);

            // Configurar SignedInfo (Canonicalización y método de firma RSA-SHA256)
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    Collections.singletonList(ref));

            // Configurar KeyInfo (Añadir el certificado público al XML para que Hacienda pueda verificarlo)
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data xd = kif.newX509Data(Collections.singletonList(cert));
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            // 4. Firmar el documento
            // Hacienda exige que la firma cuelgue de <sum:RegistroAlta> o <sum:RegistroAnulacion>
            NodeList nodosAlta = doc.getElementsByTagNameNS("*", "RegistroAlta");
            if (nodosAlta.getLength() == 0) {
                throw new RuntimeException("No se encontró la etiqueta RegistroAlta en el XML");
            }
            Element nodoAlta = (Element) nodosAlta.item(0);

            // Le decimos al contexto de firma dónde incrustarla
            DOMSignContext dsc = new DOMSignContext(privateKey, nodoAlta);
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            // 5. Convertir el Documento DOM firmado de vuelta a un String
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error fatal al firmar el XML de VeriFactu: " + e.getMessage(), e);
        }
    }
}