package com.jdyxtech.jindouyunxing.javabean;

import java.io.Serializable;
import java.util.List;

public class Local implements Serializable{
	private int dis,area,cartotal,parktotal;
	private String city, title, addr;
	private List<Double> loc;
	public int getDis() {
		return dis;
	}
	public void setDis(int dis) {
		this.dis = dis;
	}
	public int getParktotal() {
		return parktotal;
	}
	public void setParktotal(int parktotal) {
		this.parktotal = parktotal;
	}
	public int getCartotal() {
		return cartotal;
	}
	public void setCartotal(int cartotal) {
		this.cartotal = cartotal;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getArea() {
		return area;
	}
	public void setArea(int area) {
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
	public List<Double> getLoc() {
		return loc;
	}
	public void setLoc(List<Double> loc) {
		this.loc = loc;
	}
	
	

}
