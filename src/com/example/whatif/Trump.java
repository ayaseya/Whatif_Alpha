package com.example.whatif;

public class Trump {

	//トランプの数字(1～13)
	private int number;
	//トランプの図柄(spade heart club diamond)
	private String suit;
	//トランプの連番(0～51)
	private int serial;
	
	private int color;

	public Trump(int number, String suit,int serial,int color) {
		this.number = number;
		this.suit = suit;
		this.serial=serial;
		this.color=color;
	}



	public void setNumber(int number) {
		this.number = number;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}
	public void setSerial(int serial) {
		this.serial = serial;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getNumber() {
		return number;
	}

	public String getSuit() {
		return suit;
	}

	public int getSerial() {
		return serial;
	}
	public int getColor() {
		return color;
	}


}
