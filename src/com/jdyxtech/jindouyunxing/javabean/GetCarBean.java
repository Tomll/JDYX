package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class GetCarBean {
	private int step,status;
	private List<CarBean> lists;
	private String desc;
	public GetCarBean(int step, int status, List<CarBean> lists, String desc) {
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
	public List<CarBean> getLists() {
		return lists;
	}
	public void setLists(List<CarBean> lists) {
		this.lists = lists;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	@Override
	public String toString() {
		return "GetCarResponse [step=" + step + ", status=" + status + ", lists=" + lists + ", desc=" + desc + "]";
	}
	
}
