package com.jdyxtech.jindouyunxing.javabean;

import java.io.Serializable;
import java.util.List;

public class ParkBean implements Serializable {
	
	private int  dis, kwh,hasCar,status;
	private List<Double> loc;
	private String gwid, deviceNo,area,title,addr,parknum;
	public int getDis() {
		return dis;
	}
	public void setDis(int dis) {
		this.dis = dis;
	}
	public int getKwh() {
		return kwh;
	}
	public void setKwh(int kwh) {
		this.kwh = kwh;
	}
	public int getHasCar() {
		return hasCar;
	}
	public void setHasCar(int hasCar) {
		this.hasCar = hasCar;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<Double> getLoc() {
		return loc;
	}
	public void setLoc(List<Double> loc) {
		this.loc = loc;
	}
	public String getGwid() {
		return gwid;
	}
	public void setGwid(String gwid) {
		this.gwid = gwid;
	}
	public String getDeviceNo() {
		return deviceNo;
	}
	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
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
	

	

}
