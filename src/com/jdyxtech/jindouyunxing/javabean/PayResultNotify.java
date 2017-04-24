package com.jdyxtech.jindouyunxing.javabean;

public class PayResultNotify {

	private String desc,ordernum;
	private int status,orderstatus,paystatus;
	public PayResultNotify(String desc, String ordernum, int status, int orderstatus, int paystatus) {
		super();
		this.desc = desc;
		this.ordernum = ordernum;
		this.status = status;
		this.orderstatus = orderstatus;
		this.paystatus = paystatus;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getOrdernum() {
		return ordernum;
	}
	public void setOrdernum(String ordernum) {
		this.ordernum = ordernum;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getOrderstatus() {
		return orderstatus;
	}
	public void setOrderstatus(int orderstatus) {
		this.orderstatus = orderstatus;
	}
	public int getPaystatus() {
		return paystatus;
	}
	public void setPaystatus(int paystatus) {
		this.paystatus = paystatus;
	}
	
}
