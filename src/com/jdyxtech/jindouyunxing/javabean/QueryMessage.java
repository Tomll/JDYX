package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class QueryMessage {
	private String desc;
	private int status;
	private List<Msg> lists;
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<Msg> getLists() {
		return lists;
	}
	public void setLists(List<Msg> lists) {
		this.lists = lists;
	}
	

}
