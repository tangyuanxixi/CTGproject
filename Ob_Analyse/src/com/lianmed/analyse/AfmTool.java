package com.lianmed.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ThresholdInfo_T{
	
	public  int threshold;
	public  int mean_non;
	public ThresholdInfo_T(){
		
		threshold = 0;		
		mean_non = 0;
	}
	
}

/*
class FM{
	
	public int fmstart;
	public int fmend;
	public int tempv;
	public int means;
	public int maxdif_index;
	
	
	public FM(int fmstart,int fmend,int tempv,int means,int maxdif_index ){
		this.fmstart = fmstart;
		this.fmend = fmend;
		this.tempv = tempv;
		this.means = means;
		this.maxdif_index = maxdif_index;
		
	}
}
*/

public class AfmTool {
	//private int 
	public ThresholdInfo_T l_stThresholdInfo;
	
	public List<Integer> l_AFMValueBuffer;
	
	public AfmTool(){	
		l_stThresholdInfo = new ThresholdInfo_T();
		
		l_stThresholdInfo.threshold = 25;
		l_stThresholdInfo.mean_non = 10;
		
		l_AFMValueBuffer = new ArrayList<Integer>();
		
	}	
	
	/*
	public void markFromAFMArray(List<Integer> afmvarray ){
		
		List<Integer> afmarray = new ArrayList<Integer>();
		
		int  afmmerge_range = 15;
		
		List<Integer> l_AFMValueBuffer = new ArrayList<Integer>();
		
		int afmvlen = afmvarray.size();	
		
		List<int[]>  markarray_may = new ArrayList<int[]>();
		
		double start_end_thrd = 0.5;
		int ti =0;
		int  tocostart = -1;
		int tocoend = 0;
		
		while(ti<afmvlen){
			
			int afmMark = 0;
			
			int currentValue = afmvarray.get(ti);
			
			l_AFMValueBuffer.add(currentValue);
			
			int nextstep = 38;
			if (currentValue >= l_stThresholdInfo.threshold){
				
				//# 如果前面有start标志
				
				if (tocostart >= 0){
					int lagtime =  ti - tocoend;
					//#新的大于阈值的点与上一个相差小于15点，标记为胎动的新的结束点
					if(lagtime<=afmmerge_range){
						tocoend = ti;					
					}
					//#如果大于15点，结束之前的胎动标记,存胎动,标记新的胎动起点和结束点
					else{
						int[] newtoco = new int[]{tocostart,tocoend};
						afmarray.add(newtoco);
						
					}
					
				}
				
			}
			
			
			
			
			
		}
		
		
		
	}
	*/
	/**
	 * 计算可疑的胎动位置
	 * @param afmvarray
	 * @param fmbaseline
	 * @return
	 */
	public ArrayList<FMData> getFM(List<Integer> afmvarray,List<Integer> fmbaseline){
		
		// 改成0.25秒的值
		int fix_num = 3;

		ArrayList<FMData> fmlist = new ArrayList<FMData>();
		
		int afmmerge_range = 15 * fix_num; 
		
		int afmvlen = afmvarray.size();
		int ti=0;
		int fmstart = -1;
		int fmend = 0;
		List<Integer> difarray = new ArrayList<Integer>();
		
		List<Integer> difpools = new ArrayList<Integer>();
		int currentThreshold = 0;

		while(ti<afmvlen){
			
			
			int currentValue = afmvarray.get(ti);// fmm
			
			int currentBasev = fmbaseline.get(ti); // fmm基线
			
			l_AFMValueBuffer.add(currentValue);
			
			//# 当前值比阈值大或 当前值比上个周期平均值大10单位(该值可能需要进行经验校正)
			
			int currentdif = currentValue-currentBasev; // 计算差值

			currentThreshold = getDifThreshold(currentBasev);
			
			difarray.add(currentdif); // 添加差值
			
			if (currentdif >= (float)0.8*currentThreshold){ // 如果当前差值大于0.8乘以阈值
				
				// 处于标记状态的胎动
				if (fmstart >= 0){
					
					int lagtime = ti - fmend;
					
					//# 新的大于阈值的点与上一个相差小于15点(论文中是5秒以内)，标记为胎动的新的结束点
					if(lagtime <= afmmerge_range){						
						fmend = ti;						
					}else{
						int maxdif_index;
						
						// 求范围内的峰值
						int tempv = Math.round(Collections.max(difpools));
						
						if(fmstart!=fmend){
							List<Integer> difslice = difarray.subList(fmstart, fmend);
							maxdif_index = fmstart + difslice.indexOf(Collections.max(difslice)); // 找到峰值的下标
						}else{
							maxdif_index = fmstart;
						}
						
						int mean_index = ti/75;

						int fm_baselinevalue =  fmbaseline.get((fmstart+fmend)/2);
					

						FMData fm = new FMData(fmstart,fmend,tempv,fm_baselinevalue,maxdif_index);
						fmlist.add(fm);
						fmstart = ti;
						fmend = ti;
						difpools.clear();
					}
					// 未处于标记状态的胎动点
				}else{
					
					fmstart = ti;
					fmend = ti;
				}
				
				difpools.add(currentdif);
			}
			
			ti++;			
		}
		
		//# 把最后的胎动点处理，不然会漏掉最后一个
		
		if(difpools.size()>0 && Collections.max(difpools)>= currentThreshold){
			//int meansindex = ti/75;
			/*
			if(meansindex>=means.size()){
				meansindex = means.size()-1;
			}
			*/
			//int currentBasev =  fmbaseline.get(ti);
			int fm_baselinevalue =  fmbaseline.get((fmstart+fmend)/2);
			FMData fm = new FMData(fmstart,fmend,Collections.max(difpools),fm_baselinevalue,(fmstart+fmend)/2);
			fmlist.add(fm);
		}
		
		return fmlist;
	}
	
	/**
	 * 更新阈值
	 * @param basevalue
	 * @return
	 */
	public int getDifThreshold(int basevalue){
		if(basevalue<10){
			return Math.max(6, basevalue);
		}else if(10<=basevalue&&basevalue<15){			
			return Math.max(10,basevalue);		
		}else{
			return basevalue;		
		}	
	}
	
		
	
}
