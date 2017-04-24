package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class GetTravelListBean {
	
	private String desc;
	private int step,status;
	private List<MyTravelBean> lists;
	
	public GetTravelListBean(String desc, int step, int status, List<MyTravelBean> lists) {
		super();
		this.desc = desc;
		this.step = step;
		this.status = status;
		this.lists = lists;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
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
	public List<MyTravelBean> getLists() {
		return lists;
	}
	public void setLists(List<MyTravelBean> lists) {
		this.lists = lists;
	}
	
	
}
