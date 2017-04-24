package com.jdyxtech.jindouyunxing.javabean;

public class OpenCarDoorBean {

	private String desc,pw;
	private int status;
	
	public OpenCarDoorBean(String desc, String pw, int status) {
		super();
		this.desc = desc;
		this.pw = pw;
		this.status = status;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	
}
