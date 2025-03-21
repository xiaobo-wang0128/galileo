package org.armada.galileo.mvc_plus.converter;

import org.springframework.core.convert.converter.Converter;

public class String2IntegerConverter implements Converter<String, Integer> {

	public Integer convert(String arg0) {
		try {
			return Integer.valueOf(arg0);
		} catch (Exception e) {
		}
		return null;
	}

}
