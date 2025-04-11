package com.haigui.common.pdf.util.location;

 
public class ReplaceRegion {
 
	private String aliasName;
	private Float x;
	private Float y;
	private Float w;
	private Float h;
	
	private int page;
	
	public ReplaceRegion(String aliasName){
		this.aliasName = aliasName;
	}
	
	 
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	public Float getX() {
		return x;
	}
	public void setX(Float x) {
		this.x = x;
	}
	public Float getY() {
		return y;
	}
	public void setY(Float y) {
		this.y = y;
	}
	public Float getW() {
		return w;
	}
	public void setW(Float w) {
		this.w = w;
	}
	public Float getH() {
		return h;
	}
	public void setH(Float h) {
		this.h = h;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
}
