package com.jdyxtech.jindouyunxing.javabean;

public class OpenOrderBean {
	
	private String desc;
	private int status;

	public OpenOrderBean(String desc, int status) {
		super();
		this.desc = desc;
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
