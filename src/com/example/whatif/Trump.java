package com.example.whatif;

public class Trump {

	//トランプの数字(1～13)
	private int number;
	//トランプの図柄(spade heart club diamond)
	private String suit;

	public Trump(int number, String suit) {
		this.number = number;
		this.suit = suit;

	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}

	public int getNumber() {
		return number;
	}

	public String getSuit() {
		return suit;
	}

}
