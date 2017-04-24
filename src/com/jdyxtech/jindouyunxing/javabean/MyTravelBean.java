package com.jdyxtech.jindouyunxing.javabean;

public class MyTravelBean {
	private String order_num,org_name, dst_name , starttime ,endtime;
	private int orderstatus,paystatus,assess,cha_time;
	private float free,dist,disttime,activity_free,deduction_free;
	private long opendoor_at;
	private OrgBean org;
	private DstBean dst;
	
	public float getDeduction_free() {
		return deduction_free;
	}
	public void setDeduction_free(float deduction_free) {
		this.deduction_free = deduction_free;
	}
	public int getCha_time() {
		return cha_time;
	}
	public void setCha_time(int cha_time) {
		this.cha_time = cha_time;
	}
	public float getActivity_free() {
		return activity_free;
	}
	public void setActivity_free(float activity_free) {
		this.activity_free = activity_free;
	}
	public String getOrder_num() {
		return order_num;
	}
	public void setOrder_num(String order_num) {
		this.order_num = order_num;
	}
	public String getOrg_name() {
		return org_name;
	}
	public void setOrg_name(String org_name) {
		this.org_name = org_name;
	}
	public String getDst_name() {
		return dst_name;
	}
	public void setDst_name(String dst_name) {
		this.dst_name = dst_name;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public int getOrderstatus() {
		return orderstatus;
	}
	public void setOrderstatus(int orderstatus) {
		this.orderstatus = orderstatus;
	}
	public int getPaystatus() {
		return paystatus;
	}
	public void setPaystatus(int paystatus) {
		this.paystatus = paystatus;
	}
	public float getFree() {
		return free;
	}
	public void setFree(float free) {
		this.free = free;
	}
	public float getDist() {
		return dist;
	}
	public void setDist(float dist) {
		this.dist = dist;
	}
	public float getDisttime() {
		return disttime;
	}
	public void setDisttime(float disttime) {
		this.disttime = disttime;
	}
	public OrgBean getOrg() {
		return org;
	}
	public void setOrg(OrgBean org) {
		this.org = org;
	}
	public DstBean getDst() {
		return dst;
	}
	public void setDst(DstBean dst) {
		this.dst = dst;
	}
	public int getAssess() {
		return assess;
	}
	public void setAssess(int assess) {
		this.assess = assess;
	}
	public long getOpendoor_at() {
		return opendoor_at;
	}
	public void setOpendoor_at(long opendoor_at) {
		this.opendoor_at = opendoor_at;
	}
	
}
