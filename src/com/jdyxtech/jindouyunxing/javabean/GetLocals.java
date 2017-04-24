package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class GetLocals {
	
	private int step,status;
	private String desc;
	private List<Node> lists;
	
	
	
	public GetLocals(int step, int status, String desc, List<Node> lists) {
		super();
		this.step = step;
		this.status = status;
		this.desc = desc;
		this.lists = lists;
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
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public List<Node> getLists() {
		return lists;
	}
	public void setLists(List<Node> lists) {
		this.lists = lists;
	}

}
