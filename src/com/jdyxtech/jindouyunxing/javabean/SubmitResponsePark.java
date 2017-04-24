package com.jdyxtech.jindouyunxing.javabean;

public class SubmitResponsePark {

	private String deviceNo,gwid,msg;
	private int sure;
	public SubmitResponsePark(String deviceNo, String gwid, String msg, int sure) {
		super();
		this.deviceNo = deviceNo;
		this.gwid = gwid;
		this.msg = msg;
		this.sure = sure;
	}
	public String getDeviceNo() {
		return deviceNo;
	}
	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}
	public String getGwid() {
		return gwid;
	}
	public void setGwid(String gwid) {
		this.gwid = gwid;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getSure() {
		return sure;
	}
	public void setSure(int sure) {
		this.sure = sure;
	}
	
	
}
