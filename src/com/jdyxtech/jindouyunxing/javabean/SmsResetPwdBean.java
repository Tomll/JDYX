package com.jdyxtech.jindouyunxing.javabean;

public class SmsResetPwdBean {
	
	private String smsid;
	private int status;
	public SmsResetPwdBean(String smsid, int status) {
		super();
		this.smsid = smsid;
		this.status = status;
	}
	public String getSmsid() {
		return smsid;
	}
	public void setSmsid(String smsid) {
		this.smsid = smsid;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	

}
