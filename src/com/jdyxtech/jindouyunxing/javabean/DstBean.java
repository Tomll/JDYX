package com.jdyxtech.jindouyunxing.javabean;

public class DstBean {
	private String title,addr,parknum,deviceno,gwid;
	private double loc,lat;
	public DstBean(String title, String addr, String parknum, String deviceno, String gwid, double loc, double lat) {
		super();
		this.title = title;
		this.addr = addr;
		this.parknum = parknum;
		this.deviceno = deviceno;
		this.gwid = gwid;
		this.loc = loc;
		this.lat = lat;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getParknum() {
		return parknum;
	}
	public void setParknum(String parknum) {
		this.parknum = parknum;
	}
	public String getDeviceno() {
		return deviceno;
	}
	public void setDeviceno(String deviceno) {
		this.deviceno = deviceno;
	}
	public String getGwid() {
		return gwid;
	}
	public void setGwid(String gwid) {
		this.gwid = gwid;
	}
	public double getLoc() {
		return loc;
	}
	public void setLoc(double loc) {
		this.loc = loc;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}

}
