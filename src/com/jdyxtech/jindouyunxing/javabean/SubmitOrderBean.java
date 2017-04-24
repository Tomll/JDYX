package com.jdyxtech.jindouyunxing.javabean;

public class SubmitOrderBean {

	private String ordernum,desc;
	private int error;  //下单太慢，车 或 车位 已被预定
	private SubmitResponseCar submitResponseCar;
	private SubmitResponsePark submitResponsePark;
	private int status;
	
	public int getError() {
		return error;
	}
	public void setError(int error) {
		this.error = error;
	}
	public String getOrdernum() {
		return ordernum;
	}
	public void setOrdernum(String ordernum) {
		this.ordernum = ordernum;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public SubmitResponseCar getSubmitResponseCar() {
		return submitResponseCar;
	}
	public void setSubmitResponseCar(SubmitResponseCar submitResponseCar) {
		this.submitResponseCar = submitResponseCar;
	}
	public SubmitResponsePark getSubmitResponsePark() {
		return submitResponsePark;
	}
	public void setSubmitResponsePark(SubmitResponsePark submitResponsePark) {
		this.submitResponsePark = submitResponsePark;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
}
