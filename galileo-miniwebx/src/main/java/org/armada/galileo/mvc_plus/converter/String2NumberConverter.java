package org.armada.galileo.mvc_plus.converter;

import org.springframework.core.convert.converter.Converter;

public class String2NumberConverter implements Converter<String, Number> {

	public Number convert(String arg0) {
		try {
			return Double.valueOf(arg0);
		} catch (Exception e) {
		}
		try {
			return Float.valueOf(arg0);
		} catch (Exception e) {
		}
		try {
			return Integer.valueOf(arg0);
		} catch (Exception e) {
		}

		return null;
	}

}
