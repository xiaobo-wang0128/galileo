package org.armada.galileo.mvc_plus.support;


import java.lang.reflect.Method;
import java.util.List;

public class UriMappedHandler {

	private String uri;

	private Boolean isRewrite = false;

	private Object handleObject;

	private Method handleMethod;

	private List<String> fieldNames;

	public UriMappedHandler(String uri, Object handleObject, Method handleMethod) {
		super();
		this.uri = uri;
		this.handleObject = handleObject;
		this.handleMethod = handleMethod;
		if(handleMethod==null) {
			throw new RuntimeException("handleMethod is null");
		}
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Object getHandleObject() {
		return handleObject;
	}

	public void setHandleObject(Object handleObject) {
		this.handleObject = handleObject;
	}

	public Method getHandleMethod() {
		return handleMethod;
	}

	public void setHandleMethod(Method handleMethod) {
		this.handleMethod = handleMethod;
	}

	public Boolean getIsRewrite() {
		return isRewrite;
	}

	public void setIsRewrite(Boolean isRewrite) {
		this.isRewrite = isRewrite;
	}

}
