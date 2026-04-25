package com.TFG.TendaSoft.utils;

import com.TFG.TendaSoft.config.EncriptadoConfig;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.crypto.encrypt.Encryptors;

// Converter JPA que cifra/descifra automáticamente la contraseña del certificado en BD.
// Las claves vienen de application.properties a través de EncriptadoConfig,
// que actúa como puente ya que los @Converter JPA no admiten inyección de Spring directamente.
@Converter
public class Encriptado implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return Encryptors.text(EncriptadoConfig.getClave(), EncriptadoConfig.getSalt()).encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Encryptors.text(EncriptadoConfig.getClave(), EncriptadoConfig.getSalt()).decrypt(dbData);
    }
}