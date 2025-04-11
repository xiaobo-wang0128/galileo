package org.armada.galileo.mvc_plus.converter;

import org.springframework.core.convert.converter.Converter;

public class String2LongConverter implements Converter<String, Long> {

	public Long convert(String arg0) {
		try {
			return Long.valueOf(arg0);
		} catch (Exception e) {
		}
		return null;
	}

}
