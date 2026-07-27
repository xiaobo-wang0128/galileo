package org.armada.galileo.autoconfig.form;

import lombok.Data;

import java.util.List;

@Data
public class ATFormGroup {

	private String className;

	private String group;

	private String desc;

	private Integer sort ;

	private List<ATField> fields;

}
