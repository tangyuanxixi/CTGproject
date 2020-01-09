package com.lianmed.analyse;

public class FisherRst {

	public int fhrbaseline_score;
	public int zhenfu_lv_score;
	public int zhouqi_lv_score;
	public int acc_score;
	public int dec_score;
//	public int movement_score;
	
	public int total_score;
	
	public int getTotal_score(){
		total_score = fhrbaseline_score + zhenfu_lv_score + zhouqi_lv_score + acc_score + dec_score ;
		return total_score;
	}
}
