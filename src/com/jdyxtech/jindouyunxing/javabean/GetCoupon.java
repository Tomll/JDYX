package com.jdyxtech.jindouyunxing.javabean;

import java.util.List;

public class GetCoupon {
	private int status;
	private String desc;
	private List<Coupon>lists;
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
	public List<Coupon> getLists() {
		return lists;
	}
	public void setLists(List<Coupon> lists) {
		this.lists = lists;
	}
	

}
