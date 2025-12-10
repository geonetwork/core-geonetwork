/**
 * JASYPT: Java Simplified Encryption
 * ----------------------------------
 *
 * Jasypt (Java Simplified Encryption) is a java library which allows the
 * developer to add basic encryption capabilities to his/her projects with
 * minimum effort, and without the need of having deep knowledge on how
 * cryptography works.
 *
 * To learn more and download latest version:
 *
 *    http://www.jasypt.org
 *
 * Reference:
 * - https://github.com/jasypt/jasypt/issues/147
 * - https://stackoverflow.com/questions/76541331/replace-typedef-with-hibernate-6-equivalent
 */
package org.fao.geonet.domain.utils;

import jakarta.persistence.AttributeConverter;
import org.jasypt.encryption.StringEncryptor;

public class EncryptedStringConverter implements AttributeConverter<String, String> {
    public static StringEncryptor stringEncryptor;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return stringEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return stringEncryptor.decrypt(dbData);
    }
}
