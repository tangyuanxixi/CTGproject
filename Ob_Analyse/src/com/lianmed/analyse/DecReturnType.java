package com.lianmed.analyse;

public class DecReturnType {

	private int DecTime;
	private int start;
	private int end;
	private int keepTime;
	private int DecPeak;
	
	public int getDecTime() {
		return DecTime;
	}
	public void setDecTime(int decTime) {
		DecTime = decTime;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public int getKeepTime() {
		return keepTime;
	}
	public void setKeepTime(int keepTime) {
		this.keepTime = keepTime;
	}
	public int getDecPeak() {
		return DecPeak;
	}
	public void setDecPeak(int decPeak) {
		DecPeak = decPeak;
	}
	public DecReturnType(int decTime, int start, int end, int keepTime, int decPeak) {
		super();
		DecTime = decTime;
		this.start = start;
		this.end = end;
		this.keepTime = keepTime;
		DecPeak = decPeak;
	}
	@Override
	public String toString() {
		return "DecReturnType [DecTime=" + DecTime + ", start=" + start + ", end=" + end + ", keepTime=" + keepTime
				+ ", DecPeak=" + DecPeak + "]";
	}
	
	

	
}
