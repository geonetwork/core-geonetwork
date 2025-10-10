package org.fao.geonet.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AnonymousAccessLinkHashConverter implements AttributeConverter<String, String> {

	@Override
	public String convertToDatabaseColumn(String hash) {
		return "momo_" + hash;
	}

	@Override
	public String convertToEntityAttribute(String hash) {
		return hash.substring(5);
	}
}
