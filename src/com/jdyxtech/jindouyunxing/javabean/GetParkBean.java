package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class GetParkBean {
	
	private int  step ,status;
	private List<ParkBean> lists;
	private String desc;
	public GetParkBean(int step, int status, List<ParkBean> lists, String desc) {
		super();
		this.step = step;
		this.status = status;
		this.lists = lists;
		this.desc = desc;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<ParkBean> getLists() {
		return lists;
	}
	public void setLists(List<ParkBean> lists) {
		this.lists = lists;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	
}
