package com.lianmed.analyse;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.List;

public class AnalyseResult {

//	{DecTime,start,end,end-start,DecPeak}
	public ArrayList<int[]> decresult;
	
//	acctime,startindex,endindex,isacc}
	public ArrayList<int[]> accresult;
	
	public ArrayList<Integer> fhrbaseline;
	
	public ArrayList<Integer> fittedfhrSeg;
	
	public int fhrbaselinev;
	
	public int zv;//振幅变异
	public int qv;//周期变异
	
	public int uc_jx;// 基线
	
	public int ucstrong;
	
	public int uctimes;
	
	public int ucnexttime;
	
	public int uckeeptime;
	
	public ArrayList<Integer> fmresult;
	
	public ArrayList<int[]> ucresult;
	
	public int hightime;
	
	public double stv;
	
	public int ed;
	public int ld;
	public int vd;
	
	
	public float getMins(float fix_change){
		float mins = 0;
		mins = fhrbaseline.size()/(75*30*fix_change);
		return mins;
	}
	

	
	public float getAccHalfHour(){
		int accnum = 0;
		for(int i=0;i<accresult.size();i++){
			
			if (accresult.get(i)[3]==1){
				accnum++;
			}
		}
		float acchalfhour = accnum/getMins(3.2f);
		return acchalfhour;
	}
	
	public float getDecHalfHour(){
		float dechalfhour = decresult.size()/getMins(3.2f);
		return dechalfhour;
	}
	
	public float getFmHalfHour(){
		float fmhalfhour = fmresult.size()/getMins(3.2f);
		return fmhalfhour;
	}
	
	
	public float getFmFHRuptime(){
		
		float av_uptime = 0;
//		int accnum = 0;
		int times = 0;
		int uptimesum = 0;
		for(int i=0;i<accresult.size();i++){
			int[] accele = accresult.get(i);
			if (accresult.get(i)[3]==1){
				int uptime = accele[1] - accele[0];
				uptimesum += uptime;
				times ++;
			}
		}
		if(times!=0)
			av_uptime = (float)uptimesum / times;
		
		return av_uptime;
		
		
	}
	
	//胎动FHR变化幅度，这个胎动要以什么为准
	public float getFmFHRupDif(){
		
		float av_updif = 0;
//		int accnum = 0;
		int times = 0;
		int updifsum = 0;
		for(int i=0;i<accresult.size();i++){
			int[] accele = accresult.get(i);
			if (accresult.get(i)[3]==1){
				int updif = accele[4];
				updifsum += updif;
				times ++;
			}
		}
		if(times!=0)
			av_updif = (float)updifsum / times;
		return av_updif;
	}
	
	
}
