package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class MyDeposits {
	private int status;
	private float deposit ;
	private float value_card ;
	private String desc;
	private List<Deposit> lists;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public float getDeposit() {
		return deposit;
	}
	public void setDeposit(float deposit) {
		this.deposit = deposit;
	}
	public float getValue_card() {
		return value_card;
	}
	public void setValue_card(float value_card) {
		this.value_card = value_card;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<Deposit> getLists() {
		return lists;
	}
	public void setLists(List<Deposit> lists) {
		this.lists = lists;
	}


}
