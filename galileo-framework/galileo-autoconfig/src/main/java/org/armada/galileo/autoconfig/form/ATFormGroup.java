package org.armada.galileo.autoconfig.form;

import java.util.List;

import lombok.Data;

@Data
public class ATFormGroup {

	private String className;
	
	private String group;
	
	private String desc;
	
	private Integer sort ;

	private List<ATField> fields;

}
