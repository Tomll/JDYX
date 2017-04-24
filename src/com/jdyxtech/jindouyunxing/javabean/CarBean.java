package com.jdyxtech.jindouyunxing.javabean;

import java.io.Serializable;
import java.util.List;

public class CarBean implements Serializable{
	private int dis, area , seats , power ,highspeed, used , stop ,range;
	private String brand,model , carli , small , big ,neat,title,addr,parknum; 
	private float dis_free,min_free; 
	private List<String> imgs;
	private List<Double> loc;
	public int getDis() {
		return dis;
	}
	public void setDis(int dis) {
		this.dis = dis;
	}
	public int getArea() {
		return area;
	}
	public void setArea(int area) {
		this.area = area;
	}
	public int getSeats() {
		return seats;
	}
	public void setSeats(int seats) {
		this.seats = seats;
	}
	public int getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
	public int getHighspeed() {
		return highspeed;
	}
	public void setHighspeed(int highspeed) {
		this.highspeed = highspeed;
	}
	public int getUsed() {
		return used;
	}
	public void setUsed(int used) {
		this.used = used;
	}
	public int getStop() {
		return stop;
	}
	public void setStop(int stop) {
		this.stop = stop;
	}
	public int getRange() {
		return range;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getCarli() {
		return carli;
	}
	public void setCarli(String carli) {
		this.carli = carli;
	}
	public String getSmall() {
		return small;
	}
	public void setSmall(String small) {
		this.small = small;
	}
	public String getBig() {
		return big;
	}
	public void setBig(String big) {
		this.big = big;
	}
	public String getNeat() {
		return neat;
	}
	public void setNeat(String neat) {
		this.neat = neat;
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
	public float getDis_free() {
		return dis_free;
	}
	public void setDis_free(float dis_free) {
		this.dis_free = dis_free;
	}
	public float getMin_free() {
		return min_free;
	}
	public void setMin_free(float min_free) {
		this.min_free = min_free;
	}
	public List<String> getImgs() {
		return imgs;
	}
	public void setImgs(List<String> imgs) {
		this.imgs = imgs;
	}
	public List<Double> getLoc() {
		return loc;
	}
	public void setLoc(List<Double> loc) {
		this.loc = loc;
	}
	
	
}
