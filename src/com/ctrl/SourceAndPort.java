package com.ctrl;

import java.util.Random;

public class SourceAndPort {
	Random rr = new Random();
	String str = "";

	public String getSource() {
		str = "";
		str = "PR" + String.valueOf(rr.nextInt(10))
				+ String.valueOf(rr.nextInt(10))
				+ String.valueOf(rr.nextInt(10));
		return str;
	}

	public int getPort() {
		String str = "";
		str = String.valueOf(rr.nextInt(10)) + String.valueOf(rr.nextInt(10))
				+ String.valueOf(rr.nextInt(10))
				+ String.valueOf(rr.nextInt(10));
		return Integer.parseInt(str);
	}
}