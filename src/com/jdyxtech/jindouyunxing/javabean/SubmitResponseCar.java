package com.jdyxtech.jindouyunxing.javabean;

public class SubmitResponseCar {

	private String card,msg ;
	private int sure;
	public SubmitResponseCar(String card, String msg, int sure) {
		super();
		this.card = card;
		this.msg = msg;
		this.sure = sure;
	}
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
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
