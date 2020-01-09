package com.lianmed.analyse;

public class FMData{
	
	
	
	public int fmstart; // 开始位置
	public int fmend; //结束位置
	public int tempv; // 峰值
	public float basevalue; // 基线值
	public int maxdif_index; // 峰值下标
	
	
	public FMData(int fmstart,int fmend,int tempv,float basevalue,int maxdif_index ){
		this.fmstart = fmstart;
		this.fmend = fmend;
		this.tempv = tempv;
		this.basevalue = basevalue;
		this.maxdif_index = maxdif_index;
		
	}
}