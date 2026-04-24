package com.TFG.TendaSoft.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Converter
public class Encriptado implements AttributeConverter<String, String> {

    // En un entorno real, estas claves irían en application.properties o variables de entorno
    private static final String CLAVE_SECRETA = "MiClaveSuperSecretaTFG";
    private static final String SALT = "5c071755a5398f12"; // Debe ser hexadecimal

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        TextEncryptor encryptor = Encryptors.text(CLAVE_SECRETA, SALT);
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        TextEncryptor encryptor = Encryptors.text(CLAVE_SECRETA, SALT);
        return encryptor.decrypt(dbData);
    }
}