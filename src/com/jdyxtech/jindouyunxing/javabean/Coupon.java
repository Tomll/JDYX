package com.jdyxtech.jindouyunxing.javabean;

public class Coupon {

	private int id,user_id,status;
	private float free;
	private String created_at,outtime,intra,use_order;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public float getFree() {
		return free;
	}
	public void setFree(float free) {
		this.free = free;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getOuttime() {
		return outtime;
	}
	public void setOuttime(String outtime) {
		this.outtime = outtime;
	}
	public String getIntra() {
		return intra;
	}
	public void setIntra(String intra) {
		this.intra = intra;
	}
	public String getUse_order() {
		return use_order;
	}
	public void setUse_order(String use_order) {
		this.use_order = use_order;
	}
	
	
}
