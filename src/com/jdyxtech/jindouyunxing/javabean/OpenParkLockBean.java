package com.jdyxtech.jindouyunxing.javabean;

public class OpenParkLockBean {
	private String deviceNo,desc,gwid;
	private int updown,status;
	public OpenParkLockBean(String deviceNo, String desc, String gwid, int updown, int status) {
		super();
		this.deviceNo = deviceNo;
		this.desc = desc;
		this.gwid = gwid;
		this.updown = updown;
		this.status = status;
	}
	public String getDeviceNo() {
		return deviceNo;
	}
	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getGwid() {
		return gwid;
	}
	public void setGwid(String gwid) {
		this.gwid = gwid;
	}
	public int getUpdown() {
		return updown;
	}
	public void setUpdown(int updown) {
		this.updown = updown;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

}
