package com.lianmed.analyse;

public class FMData{
	
	
	
	public int fmstart; // ��ʼλ��
	public int fmend; //����λ��
	public int tempv; // ��ֵ
	public float basevalue; // ����ֵ
	public int maxdif_index; // ��ֵ�±�
	
	
	public FMData(int fmstart,int fmend,int tempv,float basevalue,int maxdif_index ){
		this.fmstart = fmstart;
		this.fmend = fmend;
		this.tempv = tempv;
		this.basevalue = basevalue;
		this.maxdif_index = maxdif_index;
		
	}
}