package com.jdyxtech.jindouyunxing.javabean;

public class SmsBean {
	private String smsid,altcode;
	private int status;

	public SmsBean(String smsid, String altcode, int status) {
		super();
		this.smsid = smsid;
		this.altcode = altcode;
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
	public String getAltcode() {
		return altcode;
	}
	public void setAltcode(String altcode) {
		this.altcode = altcode;
	}

}
