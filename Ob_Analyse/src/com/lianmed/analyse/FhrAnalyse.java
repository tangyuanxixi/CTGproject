package com.lianmed.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

class histInformation{
	
	int event;
	int frequency;
	
	public histInformation(int event){
		this.event = event;
		this.frequency = 0;
	}
	
}

public class FhrAnalyse {

	public static int globalFlag = 0;
	int hislen = 300;
	
	public static float getAvg(List<Integer> datalist){
		float avg = 0;
		
		int total = 0;
		int nums = datalist.size();
		for(int i=0;i<nums;i++){
			total += datalist.get(i);			
		}	
		avg = (float)total/nums;
		return avg ;	
	}
	
	
	
	/**
	 * 简单的去掉加速
	 * @param srcarrfhr
	 * @param analyseResult
	 * @param fix_change
	 * @return
	 */
	public ArrayList<int[]> getAccSimple(List<Integer> srcarrfhr,AnalyseResult analyseResult,float fix_change){
		
		// tyl: 两点间的差值
		List<Integer> arrdiff  = new ArrayList<Integer>(); 
		ArrayList<Integer> fhrbaseline  = new ArrayList<Integer>();
		List<Integer> fhrHist  = new ArrayList<Integer>();
		List<Float> fhrbaselinetemp  = new ArrayList<Float>();
		List<Integer> workarrfhr  = new ArrayList<Integer>();
		
		int tmpbp = 0;
		int cutstart = 0;
		int cutend = 0;
		
		List<Integer> fhr_arr = new ArrayList<Integer>(srcarrfhr);
		
		int doclength = fhr_arr.size();
		
		int worklen = doclength;
		
		//求出胎心率曲线非零部分的平均数
		List<Integer> fhr_arr_notzero = new ArrayList<Integer>();
		for(int i=0;i<fhr_arr.size();i++){
			if(fhr_arr.get(i)>0){
				fhr_arr_notzero.add(fhr_arr.get(i));
			}
		}
		
		float avgfhr = getAvg(fhr_arr_notzero);
		
		// 3 cut off starts and ends where contains broken Line
		 // kisi 2016-04-06
		int tmpworklen1 = worklen;
		
		// tyl:去除前面无效部分
		for(int i=0;i<tmpworklen1;i++){
			
			if(fhr_arr.get(i)<1 || (avgfhr - fhr_arr.get(i))>60){
				worklen -= 1;
			}		
			else{			
				workarrfhr = new ArrayList<Integer>(fhr_arr.subList(i, tmpworklen1));
				cutstart = i;
				break;
			}	
		}
		
		int tmpworklen2 = workarrfhr.size();
		
		// tyl:去除后面无效部分
		for(int i=tmpworklen2-1;i>0;i--){
			if (workarrfhr.get(i) < 1 || (avgfhr - workarrfhr.get(i)) > 60){
				worklen -= 1;
			}else{
				workarrfhr = new ArrayList<Integer>(workarrfhr.subList(0, i+1));
				cutend = i;
				break;
			}		
		}
		
		worklen = workarrfhr.size();
		
		//# 4 break or singular point fitting
	    //# 通过线性插值的方法去除断点
		
		for(int i=0;i<worklen-1;i++){
			arrdiff.add(Math.abs(workarrfhr.get(i+1)-workarrfhr.get(i)));
		}
		
		int i = 0;
		int searchpoints = 0;
		
		// tyl:0.8秒的值
		searchpoints = 400;
		// tyl:0.25秒的值
		searchpoints = (int)(400 * fix_change);
		
		while(i<worklen-1){
			
			//# 如果两点间突变大于30：
			if (arrdiff.get(i) > 30){
				//# 向后搜索400个样本点，找到一个满足：如果FHRdiff[j]绝对值小于10并且FHRseg[j+1]与平均数meanFHR之差的绝对值小于50				
				for(int j=i;j<i+searchpoints;j++){
					
					if(j>(worklen-1) || j>=arrdiff.size()){
						break;
					}
					
					if(arrdiff.get(j)<10 && Math.abs(workarrfhr.get(j+1) - avgfhr)<50 && workarrfhr.get(j+1)!=0 ){
						//#对于出现两个相邻采样点小于10且当前胎心率值比之前胎心率值的差绝对值小于50且不等于0的情况
						tmpbp = j;// #记录下当前点的前一点作为伪迹段搜寻的结束点						
						break;					
					}
					//若j在i之后399个点仍没有找到满足的伪迹段结束点,则将temp赋值为0
					if(j == (i+searchpoints-1)){
						tmpbp = 0;
					}										
				}
				
				//若temp不等于0，则伪迹段所有点与点间的差都赋予0
				if(tmpbp !=0){
					
					for(int k=0;k<tmpbp;k++){
						arrdiff.set(k, 0);
					}
					
					//# 5 interval between value
					float interval = (workarrfhr.get(tmpbp) - workarrfhr.get(i)) / (tmpbp - i); //#斜率
					//#线性插值
					for(int s=i;s<tmpbp-1;s++){
						workarrfhr.set(s + 1, (int) (workarrfhr.get(s)+interval));
					}
					
					// 6 tmpbp+1 to i reloop
					i = tmpbp + 1;
					tmpbp = 0;
					continue;					
				}								
			}			
			i ++;
		}
			
		int workarrlen = workarrfhr.size();
		int ti = 2;
		
		while(ti<workarrlen-2){
			int wl = workarrfhr.get(ti-1);
			int wm = workarrfhr.get(ti);
			int wr = workarrfhr.get(ti + 1);
			
			if(Math.abs(wm-wl)>10 && Math.abs(wm - wr)>10 && Math.abs(wl-wr)<5){				
				workarrfhr.set(ti, (wl + wr) / 2);												
			}
			ti ++;		
		}
		//这个地方和0.8秒和0.25秒暂时认定没有关系
		for(int i1=0;i1<worklen;i1++){
			fhrbaselinetemp.add((float)workarrfhr.get(i1));			
			if(workarrfhr.get(i1) != 0){
				float tempfhr = (float) (60000 / (float)(workarrfhr.get(i1)) + 0.5);
				if (tempfhr >600 || tempfhr<300){
					fhrHist.add(0);
				}else{
					fhrHist.add((int)tempfhr);
				}				
			}			
		}
		
		
		//# 现在fhrHist为脉冲间隔值数组 （去除区间[300-600]之外)
	    //# 求数组中每个脉冲间隔值的频数
		
		List<histInformation> arrayhis = histgram(fhrHist,worklen);		
		 //# 脉冲间隔值从低到高累加频数数组
		List<Integer> arraycum = cumulant(arrayhis);
		
		int sumvalue = isum(arrayhis);
		
		int startpoint = 0;
		//# Find position where 1/8 of the histogram area 找到频率累积量大于0.875*频率总和
		for(int i1=0;i1<hislen;i1++){
			
			if(arraycum.get(i1) > 0.875 * sumvalue){
				startpoint = i1 - 1;
				break;
			}
		}
		
		int peak = maxpt(arrayhis);
		//System.out.println("maxpt peak:"+peak);
		for(int i1=startpoint-5;i1>=5;i1--){
			
			if(arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-2).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-3).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-4).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-5).frequency &&
					(arrayhis.get(i1).frequency > 0.005 * sumvalue || Math.abs(299 + startpoint - peak) <= 30)){			
					peak = 300 + i1;
					break;					
			}			
		}
		
		if(peak ==0){
			peak = 100;
		}else{
			peak = (int) (60000.0/peak);
		}
		
		
		//# 进行五次滤波四修剪
		
		//System.out.println("peak peak@:"+ peak);
		basfilter(peak, worklen ,fhrbaselinetemp);
		bastrimma(workarrfhr,fhrbaselinetemp, 20, 20, worklen);
		
		basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 15, 15, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 10, 10, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 5, 10, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 3, 5, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    
	    int sum_fhr = 0;
		
		//#补全fhr
	    
	    List<Integer> fittedfhrSeg = new ArrayList<Integer>();
	    
	    
	    for(int t1=0;t1<cutstart;t1++){
	    	fittedfhrSeg.add(0);
	    	fhrbaseline.add(Math.round(fhrbaselinetemp.get(0)));
	    	sum_fhr+=fhrbaselinetemp.get(0);
	    }
	    
		for(int t1=0;t1<fhrbaselinetemp.size();t1++){
			
			fittedfhrSeg.add(workarrfhr.get(t1));
			fhrbaseline.add(Math.round(fhrbaselinetemp.get(t1)));
			sum_fhr += fhrbaselinetemp.get(t1);
		}
		
		if(cutend!=0){
			for(int t1 = 0;t1<(doclength -1 - (cutstart+cutend));t1++){
				fittedfhrSeg.add(0);
				fhrbaseline.add(Math.round(fhrbaselinetemp.get(fhrbaselinetemp.size()-1)));
				sum_fhr += fhrbaselinetemp.get(fhrbaselinetemp.size()-1);
			}
		}
		
		int fhrbasev = 0;
		if(fhrbaseline.size()>0)
			fhrbasev = sum_fhr/fhrbaseline.size();
		
		analyseResult.fhrbaselinev = fhrbasev;
		
	    
	    if(fhrbaseline.size()>0){
	    	findDeviationFromBaseline(fhrbaseline, srcarrfhr,fix_change);
	    }
 		
		//前面这部分跟获取基线的方法一样
	   // ArrayList<int[]> arrayacc = acc_RT(0, fittedfhrSeg.size(), avgfhr, fittedfhrSeg, fhrbaseline, fix_change);     
	   ArrayList<int[]> arrayacc = accSimple(fittedfhrSeg, fhrbaseline, fix_change);
	    
		analyseResult.fhrbaseline = fhrbaseline;
		analyseResult.accresult = new ArrayList<int[]>(arrayacc);
		analyseResult.fittedfhrSeg = new ArrayList<Integer>(fittedfhrSeg);
		
		return arrayacc;
		
	}
	public ArrayList<int[]> getAcc(List<Integer> srcarrfhr,AnalyseResult analyseResult,float fix_change){
		
		// tyl: 两点间的差值
		List<Integer> arrdiff  = new ArrayList<Integer>(); 
		ArrayList<Integer> fhrbaseline  = new ArrayList<Integer>();
		List<Integer> fhrHist  = new ArrayList<Integer>();
		List<Float> fhrbaselinetemp  = new ArrayList<Float>();
		List<Integer> workarrfhr  = new ArrayList<Integer>();
		
		int tmpbp = 0;
		int cutstart = 0;
		int cutend = 0;
		
		List<Integer> fhr_arr = new ArrayList<Integer>(srcarrfhr);
		
		int doclength = fhr_arr.size();
		
		int worklen = doclength;
		
		//求出胎心率曲线非零部分的平均数
		List<Integer> fhr_arr_notzero = new ArrayList<Integer>();
		for(int i=0;i<fhr_arr.size();i++){
			if(fhr_arr.get(i)>0){
				fhr_arr_notzero.add(fhr_arr.get(i));
			}
		}
		
		float avgfhr = getAvg(fhr_arr_notzero);
		
		// 3 cut off starts and ends where contains broken Line
		 // kisi 2016-04-06
		int tmpworklen1 = worklen;
		
		// tyl:去除前面无效部分
		for(int i=0;i<tmpworklen1;i++){
			
			if(fhr_arr.get(i)<1 || (avgfhr - fhr_arr.get(i))>60){
				worklen -= 1;
			}		
			else{			
				workarrfhr = new ArrayList<Integer>(fhr_arr.subList(i, tmpworklen1));
				cutstart = i;
				break;
			}	
		}
		
		int tmpworklen2 = workarrfhr.size();
		
		// tyl:去除后面无效部分
		for(int i=tmpworklen2-1;i>0;i--){
			if (workarrfhr.get(i) < 1 || (avgfhr - workarrfhr.get(i)) > 60){
				worklen -= 1;
			}else{
				workarrfhr = new ArrayList<Integer>(workarrfhr.subList(0, i+1));
				cutend = i;
				break;
			}		
		}
		
		worklen = workarrfhr.size();
		
		//# 4 break or singular point fitting
	    //# 通过线性插值的方法去除断点
		
		for(int i=0;i<worklen-1;i++){
			arrdiff.add(Math.abs(workarrfhr.get(i+1)-workarrfhr.get(i)));
		}
		
		int i = 0;
		int searchpoints = 0;
		
		// tyl:0.8秒的值
		searchpoints = 400;
		// tyl:0.25秒的值
		searchpoints = (int)(400 * fix_change);
		
		while(i<worklen-1){
			
			//# 如果两点间突变大于30：
			if (arrdiff.get(i) > 30){
				//# 向后搜索400个样本点，找到一个满足：如果FHRdiff[j]绝对值小于10并且FHRseg[j+1]与平均数meanFHR之差的绝对值小于50				
				for(int j=i;j<i+searchpoints;j++){
					
					if(j>(worklen-1) || j>=arrdiff.size()){
						break;
					}
					
					if(arrdiff.get(j)<10 && Math.abs(workarrfhr.get(j+1) - avgfhr)<50 && workarrfhr.get(j+1)!=0 ){
						//#对于出现两个相邻采样点小于10且当前胎心率值比之前胎心率值的差绝对值小于50且不等于0的情况
						tmpbp = j;// #记录下当前点的前一点作为伪迹段搜寻的结束点						
						break;					
					}
					//若j在i之后399个点仍没有找到满足的伪迹段结束点,则将temp赋值为0
					if(j == (i+searchpoints-1)){
						tmpbp = 0;
					}										
				}
				
				//若temp不等于0，则伪迹段所有点与点间的差都赋予0
				if(tmpbp !=0){
					
					for(int k=0;k<tmpbp;k++){
						arrdiff.set(k, 0);
					}
					
					//# 5 interval between value
					float interval = (workarrfhr.get(tmpbp) - workarrfhr.get(i)) / (tmpbp - i); //#斜率
					//#线性插值
					for(int s=i;s<tmpbp-1;s++){
						workarrfhr.set(s + 1, (int) (workarrfhr.get(s)+interval));
					}
					
					// 6 tmpbp+1 to i reloop
					i = tmpbp + 1;
					tmpbp = 0;
					continue;					
				}								
			}			
			i ++;
		}
			
		int workarrlen = workarrfhr.size();
		int ti = 2;
		
		while(ti<workarrlen-2){
			int wl = workarrfhr.get(ti-1);
			int wm = workarrfhr.get(ti);
			int wr = workarrfhr.get(ti + 1);
			
			if(Math.abs(wm-wl)>10 && Math.abs(wm - wr)>10 && Math.abs(wl-wr)<5){				
				workarrfhr.set(ti, (wl + wr) / 2);												
			}
			ti ++;		
		}
		//这个地方和0.8秒和0.25秒暂时认定没有关系
		for(int i1=0;i1<worklen;i1++){
			fhrbaselinetemp.add((float)workarrfhr.get(i1));			
			if(workarrfhr.get(i1) != 0){
				float tempfhr = (float) (60000 / (float)(workarrfhr.get(i1)) + 0.5);
				if (tempfhr >600 || tempfhr<300){
					fhrHist.add(0);
				}else{
					fhrHist.add((int)tempfhr);
				}				
			}			
		}
		
		
		//# 现在fhrHist为脉冲间隔值数组 （去除区间[300-600]之外)
	    //# 求数组中每个脉冲间隔值的频数
		
		List<histInformation> arrayhis = histgram(fhrHist,worklen);		
		 //# 脉冲间隔值从低到高累加频数数组
		List<Integer> arraycum = cumulant(arrayhis);
		
		int sumvalue = isum(arrayhis);
		
		int startpoint = 0;
		//# Find position where 1/8 of the histogram area 找到频率累积量大于0.875*频率总和
		for(int i1=0;i1<hislen;i1++){
			
			if(arraycum.get(i1) > 0.875 * sumvalue){
				startpoint = i1 - 1;
				break;
			}
		}
		
		int peak = maxpt(arrayhis);
		//System.out.println("maxpt peak:"+peak);
		for(int i1=startpoint-5;i1>=5;i1--){
			
			if(arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-2).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-3).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-4).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-5).frequency &&
					(arrayhis.get(i1).frequency > 0.005 * sumvalue || Math.abs(299 + startpoint - peak) <= 30)){			
					peak = 300 + i1;
					break;					
			}			
		}
		
		if(peak ==0){
			peak = 100;
		}else{
			peak = (int) (60000.0/peak);
		}
		
		
		//# 进行五次滤波四修剪
		
		//System.out.println("peak peak@:"+ peak);
		basfilter(peak, worklen ,fhrbaselinetemp);
		bastrimma(workarrfhr,fhrbaselinetemp, 20, 20, worklen);
		
		basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 15, 15, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 10, 10, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 5, 10, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    
	    int sum_fhr = 0;
		
		//#补全fhr
	    
	    List<Integer> fittedfhrSeg = new ArrayList<Integer>();
	    
	    
	    for(int t1=0;t1<cutstart;t1++){
	    	fittedfhrSeg.add(0);
	    	fhrbaseline.add(Math.round(fhrbaselinetemp.get(0)));
	    	sum_fhr+=fhrbaselinetemp.get(0);
	    }
	    
		for(int t1=0;t1<fhrbaselinetemp.size();t1++){
			
			fittedfhrSeg.add(workarrfhr.get(t1));
			fhrbaseline.add(Math.round(fhrbaselinetemp.get(t1)));
			sum_fhr += fhrbaselinetemp.get(t1);
		}
		
		if(cutend!=0){
			for(int t1 = 0;t1<(doclength -1 - (cutstart+cutend));t1++){
				fittedfhrSeg.add(0);
				fhrbaseline.add(Math.round(fhrbaselinetemp.get(fhrbaselinetemp.size()-1)));
				sum_fhr += fhrbaselinetemp.get(fhrbaselinetemp.size()-1);
			}
		}
		
		int fhrbasev = 0;
		if(fhrbaseline.size()>0)
			fhrbasev = sum_fhr/fhrbaseline.size();
		
		analyseResult.fhrbaselinev = fhrbasev;
		
	    
		//int res_fhr_avg = sum_fhr / (cutstart + worklen);

		/*
		List<Integer> base_min = new ArrayList<Integer>();
		
		for(int t1 = 0;t1<mins_length-1;t1++){
			sum_min =0;
			for(int t2=t1*75;t2<(t1+1)*75;t2++ ){
				
				System.out.println("&&&&&&&&&&&&&&" + fhrbaseline.size());
				System.out.println("##########t2" + t2);
				sum_min += fhrbaseline.get(t2);				
			}
			base_min.add(sum_min/75);
		}
		
		int sumtmpbase = 0;
		*/
		/*
		int avg10 = 0;
		for(int i1=1;i1<mins_length-1;i1++){
			sumtmpbase += base_min.get(i1);
			if(i1>10 && i1%2 ==0){
				avg10 = sumtmpbase/i;
			}			
		}*/
	    if(fhrbaseline.size()>0){
	    	findDeviationFromBaseline(fhrbaseline, srcarrfhr,fix_change);
	    }
 		
		//前面这部分跟获取基线的方法一样
	             
	 
	    ArrayList<int[]> arrayacc = acc(0, fittedfhrSeg.size(), avgfhr, fittedfhrSeg, fhrbaseline,fix_change);
	    //ArrayList<int[]> arrayacc = acc_RT(0, fittedfhrSeg.size(), avgfhr, fittedfhrSeg, fhrbaseline, fix_change);
		analyseResult.fhrbaseline = fhrbaseline;
		analyseResult.accresult = new ArrayList<int[]>(arrayacc);
		analyseResult.fittedfhrSeg = new ArrayList<Integer>(fittedfhrSeg);
		
		return arrayacc;
		
	}
	
	/**
	 * 实时加速计算	
	 * @param srcarrfhr
	 * @param analyseResult
	 * @param fix_change
	 * @param fhrBaseLineList
	 * @return
	 */
	public ArrayList<int[]> getAcc_RT(List<Integer> srcarrfhr,AnalyseResult analyseResult,
			float fix_change, ArrayList<Integer> fhrBaseLineList,List<FMData> fmobarray,List<UcPeak> ucdetect_list){
		
		// tyl: 两点间的差值
		List<Integer> arrdiff  = new ArrayList<Integer>(); 
		
		ArrayList<Integer> fhrbaseline  = new ArrayList<Integer>();
		List<Integer> fhrHist  = new ArrayList<Integer>();
		List<Float> fhrbaselinetemp  = new ArrayList<Float>();
		List<Integer> workarrfhr  = new ArrayList<Integer>();
		
		int tmpbp = 0;
		int cutstart = 0;
		int cutend = 0;
		
		List<Integer> fhr_arr = new ArrayList<Integer>(srcarrfhr);
		
		int doclength = fhr_arr.size();
		
		int worklen = doclength;
		
		//求出胎心率曲线非零部分的平均数
		List<Integer> fhr_arr_notzero = new ArrayList<Integer>();
		for(int i=0;i<fhr_arr.size();i++){
			if(fhr_arr.get(i)>0){
				fhr_arr_notzero.add(fhr_arr.get(i));
			}
		}
		
		float avgfhr = getAvg(fhr_arr_notzero);
		
		// 3 cut off starts and ends where contains broken Line
		 // kisi 2016-04-06
		int tmpworklen1 = worklen;
		
		// tyl:去除前面无效部分
		for(int i=0;i<tmpworklen1;i++){
			
			if(fhr_arr.get(i)<1 || (avgfhr - fhr_arr.get(i))>60){
				worklen -= 1;
			}		
			else{			
				workarrfhr = new ArrayList<Integer>(fhr_arr.subList(i, tmpworklen1));
				cutstart = i;
				break;
			}	
		}
		
		int tmpworklen2 = workarrfhr.size();
		
		// tyl:去除后面无效部分
		for(int i=tmpworklen2-1;i>0;i--){
			if (workarrfhr.get(i) < 1 || (avgfhr - workarrfhr.get(i)) > 60){
				worklen -= 1;
			}else{
				workarrfhr = new ArrayList<Integer>(workarrfhr.subList(0, i+1));
				cutend = i;
				break;
			}		
		}
		
		worklen = workarrfhr.size();
		
		//# 4 break or singular point fitting
	    //# 通过线性插值的方法去除断点
		
		for(int i=0;i<worklen-1;i++){
			arrdiff.add(Math.abs(workarrfhr.get(i+1)-workarrfhr.get(i)));
		}
		
		int i = 0;
		int searchpoints = 0;
		
		// tyl:0.8秒的值
		searchpoints = 400;
		// tyl:0.25秒的值
		searchpoints = (int)(400 * fix_change);
		
		while(i<worklen-1){
			
			//# 如果两点间突变大于30：
			if (arrdiff.get(i) > 30){
				//# 向后搜索400个样本点，找到一个满足：如果FHRdiff[j]绝对值小于10并且FHRseg[j+1]与平均数meanFHR之差的绝对值小于50				
				for(int j=i;j<i+searchpoints;j++){
					
					if(j>(worklen-1) || j>=arrdiff.size()){
						break;
					}
					
					if(arrdiff.get(j)<10 && Math.abs(workarrfhr.get(j+1) - avgfhr)<50 && workarrfhr.get(j+1)!=0 ){
						//#对于出现两个相邻采样点小于10且当前胎心率值比之前胎心率值的差绝对值小于50且不等于0的情况
						tmpbp = j;// #记录下当前点的前一点作为伪迹段搜寻的结束点						
						break;					
					}
					//若j在i之后399个点仍没有找到满足的伪迹段结束点,则将temp赋值为0
					if(j == (i+searchpoints-1)){
						tmpbp = 0;
					}										
				}
				
				//若temp不等于0，则伪迹段所有点与点间的差都赋予0
				if(tmpbp !=0){
					
					for(int k=0;k<tmpbp;k++){
						arrdiff.set(k, 0);
					}
					
					//# 5 interval between value
					float interval = (workarrfhr.get(tmpbp) - workarrfhr.get(i)) / (tmpbp - i); //#斜率
					//#线性插值
					for(int s=i;s<tmpbp-1;s++){
						workarrfhr.set(s + 1, (int) (workarrfhr.get(s)+interval));
					}
					
					// 6 tmpbp+1 to i reloop
					i = tmpbp + 1;
					tmpbp = 0;
					continue;					
				}								
			}			
			i ++;
		}
			
		int workarrlen = workarrfhr.size();
		int ti = 2;
		
		while(ti<workarrlen-2){
			int wl = workarrfhr.get(ti-1);
			int wm = workarrfhr.get(ti);
			int wr = workarrfhr.get(ti + 1);
			
			if(Math.abs(wm-wl)>10 && Math.abs(wm - wr)>10 && Math.abs(wl-wr)<5){				
				workarrfhr.set(ti, (wl + wr) / 2);												
			}
			ti ++;		
		}
		//这个地方和0.8秒和0.25秒暂时认定没有关系
		for(int i1=0;i1<worklen;i1++){
			fhrbaselinetemp.add((float)workarrfhr.get(i1));			
			if(workarrfhr.get(i1) != 0){
				float tempfhr = (float) (60000 / (float)(workarrfhr.get(i1)) + 0.5);
				if (tempfhr >600 || tempfhr<300){
					fhrHist.add(0);
				}else{
					fhrHist.add((int)tempfhr);
				}				
			}			
		}
		
		
		//# 现在fhrHist为脉冲间隔值数组 （去除区间[300-600]之外)
	    //# 求数组中每个脉冲间隔值的频数
		
		List<histInformation> arrayhis = histgram(fhrHist,worklen);		
		 //# 脉冲间隔值从低到高累加频数数组
		List<Integer> arraycum = cumulant(arrayhis);
		
		int sumvalue = isum(arrayhis);
		
		int startpoint = 0;
		//# Find position where 1/8 of the histogram area 找到频率累积量大于0.875*频率总和
		for(int i1=0;i1<hislen;i1++){
			
			if(arraycum.get(i1) > 0.875 * sumvalue){
				startpoint = i1 - 1;
				break;
			}
		}
		
		int peak = maxpt(arrayhis);
		//System.out.println("maxpt peak:"+peak);
		for(int i1=startpoint-5;i1>=5;i1--){
			
			if(arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-2).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-3).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-4).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-5).frequency &&
					(arrayhis.get(i1).frequency > 0.005 * sumvalue || Math.abs(299 + startpoint - peak) <= 30)){			
					peak = 300 + i1;
					break;					
			}			
		}
		
		if(peak ==0){
			peak = 100;
		}else{
			peak = (int) (60000.0/peak);
		}
		
		
		//# 进行五次滤波四修剪
		
		//System.out.println("peak peak@:"+ peak);
		basfilter(peak, worklen ,fhrbaselinetemp);
		bastrimma(workarrfhr,fhrbaselinetemp, 20, 20, worklen);
		
		basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 15, 15, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 10, 10, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 5, 10, worklen);
	    
	    basfilter(peak, worklen,fhrbaselinetemp);
	    
	    int sum_fhr = 0;
		
		//#补全fhr
	    
	    List<Integer> fittedfhrSeg = new ArrayList<Integer>();
	    
	    
	    for(int t1=0;t1<cutstart;t1++){
	    	fittedfhrSeg.add(0);
	    	fhrbaseline.add(Math.round(fhrbaselinetemp.get(0)));
	    	sum_fhr+=fhrbaselinetemp.get(0);
	    }
	    
		for(int t1=0;t1<fhrbaselinetemp.size();t1++){
			
			fittedfhrSeg.add(workarrfhr.get(t1));
			fhrbaseline.add(Math.round(fhrbaselinetemp.get(t1)));
			sum_fhr += fhrbaselinetemp.get(t1);
		}
		
		if(cutend!=0){
			for(int t1 = 0;t1<(doclength -1 - (cutstart+cutend));t1++){
				fittedfhrSeg.add(0);
				fhrbaseline.add(Math.round(fhrbaselinetemp.get(fhrbaselinetemp.size()-1)));
				sum_fhr += fhrbaselinetemp.get(fhrbaselinetemp.size()-1);
			}
		}
		
		int fhrbasev = 0;
		if(fhrbaseline.size()>0)
			fhrbasev = sum_fhr/fhrbaseline.size();
		
		analyseResult.fhrbaselinev = fhrbasev;
		
	    
		//int res_fhr_avg = sum_fhr / (cutstart + worklen);

	    if(fhrbaseline.size()>0){
	    	findDeviationFromBaseline(fhrbaseline, srcarrfhr,fix_change);
	    }
	    
	    // 求加速
	    ArrayList<int[]> arrayacc = acc_RT(0, fittedfhrSeg.size(), avgfhr, fittedfhrSeg, 
	    		fhrBaseLineList, fix_change, fmobarray,ucdetect_list);
	    
	    
		analyseResult.fhrbaseline = fhrbaseline;
		analyseResult.accresult = new ArrayList<int[]>(arrayacc);
		analyseResult.fittedfhrSeg = new ArrayList<Integer>(fittedfhrSeg);
		
		return arrayacc;
		
	}
	public static void sort(int[] A,int N){
		int j,t;
		int i;
		for(i=1;i<N;i++){
			
			t=A[i];
			j=i-1;
			while(j>=0 && A[j]>t){
				A[j+1] = A[j];
				j--;
			}
			A[j+1] =t;
			
		}
		
	}
	
	public boolean checkInAcc(int index,AnalyseResult analyseResult){
		boolean isIn = false;
		
		for(int i=0;i<analyseResult.accresult.size();i++){
			
			int[] accele = analyseResult.accresult.get(i);
			if(index>=accele[1] && index<=accele[2]){
				isIn = true;
				return isIn;
			}
		}
		
		return isIn;
	}
	
	
	public boolean checkInAccOrDec(int index,AnalyseResult analyseResult){
		boolean isIn = false;
		if(analyseResult == null || analyseResult.accresult == null)
			return isIn;
		for(int i=0;i<analyseResult.accresult.size();i++){
			
			int[] accele = analyseResult.accresult.get(i);
			if(index>=accele[1] && index<=accele[2]){
				isIn = true;
				return isIn;
			}
		}
		if(analyseResult == null || analyseResult.decresult == null)
			return isIn;
		for(int i=0;i<analyseResult.decresult.size();i++){
			
			int[] decele = analyseResult.decresult.get(i);
			if(index>=decele[1] && index<=decele[2]){
				isIn = true;
				return isIn;
			}
		}
		return isIn;
	}

	public boolean checkInUC(int index,AnalyseResult analyseResult){
		boolean isIn = false;
		if(analyseResult == null || analyseResult.ucresult == null){
			return isIn;
			
		}
		for(int i=0;i<analyseResult.ucresult.size();i++){
			
			int[] ucele = analyseResult.ucresult.get(i);
			if(index>=ucele[1] && index<=ucele[2]){
				isIn = true;
				return isIn;
			}
		}
		return isIn;
	}
	
	
//	public boolean checkInFm(int index,AnalyseResult analyseResult){
//		boolean isIn = false;
//		
//		for(int i=0;i<analyseResult.accresult.size();i++){
//			
//			int fmele = analyseResult.fmresult.get(i);
//			if(index>=accele[1] && index<=accele[2]){
//				isIn = true;
//				return isIn;
//			}
//		}
//		
//		for(int i=0;i<analyseResult.decresult.size();i++){
//			
//			int[] decele = analyseResult.decresult.get(i);
//			if(index>=decele[1] && index<=decele[2]){
//				isIn = true;
//				return isIn;
//			}
//		}
//		
//		return isIn;
//		
//	}

	
	
	public void getLtv(List<Integer> srcarrfhr, AnalyseResult analyseResult) {

		int MAX_ANALY_TIME = 80;
		
		int MIN_FHR = 30;
		int MAX_FHR = 240;
		int k = 0,i,j;
		boolean stop = false;
		
		ArrayList<int[]> ltvlist = new ArrayList<int[]>();
		
		int[] LtvAmp = new int[MAX_ANALY_TIME];
		int[] LtvTime = new int[MAX_ANALY_TIME];
		
		
		for (i = 0; i + 75 < srcarrfhr.size(); i += 75) {

			stop = false;
			for (j = 0; j < 75; j++) {
				if (checkInAccOrDec(j, analyseResult)) // 排除加速、减速发生的时间段
				{
					stop = true;
				}
			}
			
			if(!stop)
			{
	            int min = MAX_FHR;
	            int max = 0;
	            int sumfhr = 0;
				for(j=0;j<75;j++)
				{
	                sumfhr += srcarrfhr.get(i+j);
					if ( (srcarrfhr.get(i+j) > max) && (srcarrfhr.get(i+j) <= MAX_FHR) )
	                {
	                    max = srcarrfhr.get(i+j);
	                }
	                if ( (srcarrfhr.get(i+j) < min) && (srcarrfhr.get(i+j) >= MIN_FHR) )
	                {
	                    min = srcarrfhr.get(i+j);
	                }
				}
				
				int LtvAmp_v = max - min;
				LtvAmp[k] = max- min;
				sumfhr = sumfhr / 75;
				int count = 0;
				for (j = 0; j < 75; j++) {
					if ((srcarrfhr.get(i + j) < sumfhr && srcarrfhr.get(i + j) >= sumfhr)
							|| (srcarrfhr.get(i + j) >= sumfhr && srcarrfhr.get(i + j) < sumfhr)) {
						count++;
					}
				}
				int LtvTime_v = count / 2;
				LtvTime[k] = count/2;
				ltvlist.add(new int[]{LtvAmp_v,LtvTime_v});  //HighEpis,LowEpis
				k++;
				
			}
			else {
				LtvAmp[k] = 0;
				LtvTime[k] = 0;
				ltvlist.add(new int[]{0,0});
				k++;
			}		

			if ((i + 75) >= srcarrfhr.size()) {
				break;
			}
		}
		
		int sumqv = 0;
	    int sumzv = 0;
	    int count = 0;
	    for (i=0; i<k; i++)
	    {
//	        if(ltvlist.get(i)[0] > 0)
	    	if(LtvAmp[i] > 0)
	        {
	            sumzv += LtvAmp[i];
//	            sumqv += ltvlist.get(i)[1];
	            sumqv += LtvTime[i];
	            count++;
	        }
	    }
	    if ( count > 0)
	    {
	        sumzv = sumzv/count;
	        sumqv = sumqv/count;
	    }
	    else
	    {
	        sumzv = 0;
	        sumqv = 0;
	    }
	    
	    
	    analyseResult.zv = sumzv;//振幅变异Final value is the average value
	    analyseResult.qv = sumqv;//周期变异
	    
	    
	    int[] HighEpis = new int[srcarrfhr.size()];
	    int[] LowEpis = new int[srcarrfhr.size()];
	    
	    
	    int m=0;
	    for (i=0; i<srcarrfhr.size(); i+=75)  //计算识别高变异，低变异，对应阈值分别为10bpm,5bpm
	    {
	        m = i/75;
	        for (j=0; j<75;j++)
	        {
//	            if (ltvlist.get(m)[0] >= 10)
	        	if (LtvAmp[m] >= 10)
	            {
	                HighEpis[i+j] = 1;
	            }
//	            else if (ltvlist.get(m)[0] <= 5 && ltvlist.get(m)[0] > 0 )
	        	else if(LtvAmp[m] <= 5 && LtvAmp[m] > 0)
	            
	            {
	                LowEpis[i+j] = 1;
	            }
	        }
	        if ((i + 75) >= srcarrfhr.size()) {
				break;
			}
	    }
	    
	    int  HighMArr[] = new int[(MAX_ANALY_TIME- 10/ 2)+ 1];           //分段高变异分钟数
	    
	    
	    m = 0;  // Modify by Behard 2012.12.07 很短时间(小于10分钟时下面的代码无效)进行分析时不会出错
	    for ( i=10; i<=k+1; i+=2) //计算分段高变异分钟数
	    {
	        m = (i-10)/2;
	        for (j=0; j<i; j++)
	        {
//	            if (ltvlist.get(m)[0] >= 10)
	        	if (LtvAmp[j] >= 10)
	            {
	                HighMArr[m]++;
	            }
	        }
	    }
	    analyseResult.hightime = HighMArr[m];

	}
	
	public void getStv(List<Integer> srcarrfhr,AnalyseResult analyseResult){
		
		int MAX_ANALY_TIME = 80;
		int i,j,k,m,n,p,r;
		boolean stop;
		int count;

		float sumfhr,sum1;
		float stvrecord[] = new float[MAX_ANALY_TIME];    //存放每分钟的短变异值
		float stvtemp[]= new float[15];		//存放每分钟内15个相连的4秒内的胎心率平均值

		m = 0;
		
		for(i=0;i<srcarrfhr.size();i+=75)
		{
			if(i+75>=srcarrfhr.size())
				break;
			stop = false;
			count = 0;
			for(j=0;j<75;j++)
			{
				if(srcarrfhr.get(i+j)== 0)
				{
					count++;
				}
			}
			if(count >= 15 )    //1分钟之内漏采点达12秒以?
			{
				stop = true;
			}
			
			if( !stop)
			{
				n=0;
				for(j=0;j<75;j+=5)
				{
					sumfhr=0.0f;
					r=0;
					for(k=0; k<5; k++)
					{
						if( srcarrfhr.get(i+j+k) > 0)
						{
							sumfhr+=60.0/(float)srcarrfhr.get(i+j+k);
							r++;
						}
					}
					if(r > 0)
					{
						sumfhr=sumfhr/(float)r;
					}
					
					stvtemp[n]=sumfhr;
					n++;
				}
				
				sum1=0.0f;
				for (p=0;p<14;p++)
				{
					sum1+=Math.abs((stvtemp[p+1]-stvtemp[p]));
				}
				stvrecord[m]=(float)(sum1/14.0f);
				m++;
				
			}else{// 这里可以设置 stvrecord[m]、m++, 最后计算平均值时会被排除，但是不能使用 m 值计算，而是使用 count 值计算
				
				stvrecord[m] = 0.00f;
				m++;
				
			}
			
			if ((i + 75) >= srcarrfhr.size()) {
				break;
			}
			
		}
		
		
		double STVArr[] = new double[(MAX_ANALY_TIME- 10/ 2)+ 1];           //分段短变异值
		
		float sumstv;
		k = 0; 
		
		for ( i=10; i<=m+1; i+=2){
			
			k = (i-10)/2;
			sumstv = 0.0f;
			count = 0;
			// for (j=0; j<i; j++)
			for (j=0; j<i && j<m; j++)
			{
				if (stvrecord[j] > 0)
				{
					sumstv += stvrecord[j];
					count++;
				}
			}
			if ( count > 0 ){
				STVArr[k] = 1000.0* sumstv/ count;
			}
		}
		
		analyseResult.stv = STVArr[k];  // 该数值为所有的平均值
	}
	
	
	/**
	 * tyl:简单的去掉减速的方法
	 * @param srcarrfhr
	 * @param analyseResult
	 * @param fix_change
	 * @return
	 */
	public ArrayList<int[]> getDecSimple(List<Integer> srcarrfhr, AnalyseResult analyseResult, float fix_change){
		
		ArrayList<int[]> arraydec = new ArrayList<int[]>();
		
		int searchstep = (int)(38*fix_change);

		int keeptime = (int)(5*fix_change);
		int keepvalue = 5;
		
		int searchrange = (int)(60*fix_change);
		
		ArrayList<Integer> fhrbaseline = analyseResult.fhrbaseline;
		ArrayList<Integer> fittedfhrSeg = analyseResult.fittedfhrSeg;
		
		float avgfhr = getAvg(fhrbaseline);
		int i = 0;
		int length = fittedfhrSeg.size();

		while(i < length){
			
			int curfhr = fittedfhrSeg.get(i);
			int curbaselinevalue = fhrbaseline.get(i);
			
			if(curfhr + keepvalue < curbaselinevalue){
				
				int decpeak = 300;//减速峰值
				int dectime = 0;
				int startIndex = 0;
				int endIndex = 0;
				boolean sstate = false;
				boolean estate = false;
				
				// 找峰值
				for(int m = i; m < i+searchrange; m++){
					
					if(m >= fittedfhrSeg.size())
						break;
					if(fittedfhrSeg.get(m) < decpeak)
					{
						decpeak = fittedfhrSeg.get(m);
						dectime = m;
					}
				}

				int curmin = 0;
				int fix_dif = 0;
				
				// 向前搜索
				for(int m = dectime - 1; m >= (int)(dectime - 55*fix_change); m--){
					
					if(m < 0)
						break;
					
					int m_fhr = fittedfhrSeg.get(m);
					int m_fhrbaseline = fhrbaseline.get(m);
					
					if(m_fhr < curmin)
						curmin = m_fhr;
					
					if((m==0 || m==(int)(dectime - 54*fix_change))){
						
						startIndex = m;
						sstate = true;
						break;
					}
					
					else if((m_fhr >= m_fhrbaseline - fix_dif)){
						
						startIndex = m;
						sstate = true;
						break;
					}
				}
				
				// 向后搜索
				
				for(int m = dectime; m < (dectime + 55*fix_change); m++){
					
					if(m > fittedfhrSeg.size())
						break;
					
					int m_fhr = fittedfhrSeg.get(m);
					int m_fhrbaseline = fhrbaseline.get(m);
					
					if(m_fhr < curmin)
					{
						curmin = m_fhr;
					}
					if((m == fittedfhrSeg.size() - 1 || m == (int)(dectime - 55*fix_change))){
						
						endIndex = m;
						estate = true;
						break;
					}
					else if((m_fhr >= m_fhrbaseline - fix_dif)){
						
						endIndex = m;
						estate = true;
						break;
					}
				}

				// 判断条件，达到减速
				if((endIndex - startIndex) >= keeptime && sstate && estate){

					int deckeeptime = endIndex - startIndex;
					
					int[] dectemp = new int[]{dectime, startIndex, endIndex, deckeeptime, decpeak};
					arraydec.add(dectemp);
					i = dectime + searchstep;
				}
				else
				{
					i++;
				}
			}
			else
			{
				i++;
			}
		}
		
		return arraydec;
	}
	
	/**
	 * tyl：改造getDec()方法，将重复的代码整合到一起
	 * @param srcarrfhr
	 * @param analyseResult
	 * @param fix_change
	 * @return
	 */
	public ArrayList<int[]> getDecNew(List<Integer> srcarrfhr,AnalyseResult analyseResult,float fix_change){
		
		ArrayList<int[]> arraydec = new ArrayList<int[]>();
		
		int m = 0;

		int[] temp_forward = new int[(int)(150*fix_change)];
		int[] temp_backward = new int[(int)(150*fix_change)];
		
		int DecTime = 0; // 峰值位置
		int MIN_FHR = 30; 

		
		int i=0;
//		int samplesize = 1000;//TODO
		
		ArrayList<Integer> fhrbaseline = analyseResult.fhrbaseline;
		ArrayList<Integer> fittedfhrSeg = analyseResult.fittedfhrSeg;

		int samplesize = fhrbaseline.size();
		int n=0;
		
		while (i < samplesize) {
			int sum_fhrbaseline = 0;
			for (n = 0; n < i + 1; n++) {
				sum_fhrbaseline = sum_fhrbaseline + fhrbaseline.get(n);
			}
			
			float mean_fhrbaseline = (float)sum_fhrbaseline / (i+1);
			
			// 第一种情况
			if ((fittedfhrSeg.get(i) <= fhrbaseline.get(i) - 25) && (fittedfhrSeg.get(i) > MIN_FHR)
					&& (i + 60 * fix_change <= samplesize) && fittedfhrSeg.get(i) <= mean_fhrbaseline - 25) {
				
				DecReturnType calDec = calDec(fittedfhrSeg, fhrbaseline, fix_change, i, temp_backward, temp_forward, 18);
				if(calDec == null)
					i++;
				else {
					arraydec.add(new int[]{calDec.getDecTime(),calDec.getStart(),calDec.getEnd(),calDec.getKeepTime(),calDec.getDecPeak()});
					DecTime = calDec.getDecTime();
					i = DecTime+(int)(75*fix_change);
				}
			}
			
			// 第二种情况
			 else if( (fittedfhrSeg.get(i) <= fhrbaseline.get(i)-20) && (fittedfhrSeg.get(i) > fhrbaseline.get(i) - 25) 
					 &&( fittedfhrSeg.get(i)> MIN_FHR  )&& (i+60<=samplesize) && fittedfhrSeg.get(i) <= mean_fhrbaseline - 20)
			 {
				 DecReturnType calDec = calDec(fittedfhrSeg, fhrbaseline, fix_change, i, temp_backward, temp_forward, 37);
				 if(calDec == null)
					i++;
				 else {
					arraydec.add(new int[]{calDec.getDecTime(),calDec.getStart(),calDec.getEnd(),calDec.getKeepTime(),calDec.getDecPeak()});
					DecTime = calDec.getDecTime();
					i = DecTime+(int)(75*fix_change);
				}
			 }
			// 第三种情况
			 else if ((fittedfhrSeg.get(i) <= fhrbaseline.get(i) - 10) && (fittedfhrSeg.get(i) > fhrbaseline.get(i) - 20)
						&& (fittedfhrSeg.get(i) > MIN_FHR) && (i + 60*fix_change <= samplesize)
						&& fittedfhrSeg.get(i) <= mean_fhrbaseline - 15) {
				 
				 DecReturnType calDec = calDec(fittedfhrSeg, fhrbaseline, fix_change, i, temp_backward, temp_forward, 75);
				 if(calDec == null)
					i++;
				 else {
					arraydec.add(new int[]{calDec.getDecTime(),calDec.getStart(),calDec.getEnd(),calDec.getKeepTime(),calDec.getDecPeak()});
					DecTime = calDec.getDecTime();
					i = DecTime+(int)(75*fix_change);
				}
			 }
			else {
				i++;
			}
		}
		
		for(int ti=0;ti<arraydec.size();ti++){
			int[] decele = arraydec.get(ti);
			int dectime = decele[0];
			int k = dectime;
			boolean findED = false;
			boolean findLD= false;
			
			if(decele[3]>75*fix_change && srcarrfhr.get(dectime)>fhrbaseline.get(dectime)-50){
				findED = false;
				for (m = 0; m < 19*fix_change; m++) {

					if(checkInUC(k-m, analyseResult)){

							
						analyseResult.ed++; // 早发减速

						findED = true;
						break;
					}
				}

				if(findED) 
					continue;

				for (m = (int) (37 * fix_change); m <= 75 * fix_change; m++) {
					if (checkInUC(k - m, analyseResult)) {

						findLD = true;
						analyseResult.ld++;// 迟发减速

						break;
					}
				}
			}
			
			if (!(findED || findLD)) // 不是LD、ED，肯定是VD
			{
				analyseResult.vd++; // 变异减速
			}
			
		}
		analyseResult.decresult = arraydec;
		return arraydec;
}
	
	
	//原来是0.8S一个点，现在是0.25秒一个点，得转换时间比例，0.8/0.25=3.2
	public ArrayList<int[]> getDec(List<Integer> srcarrfhr,AnalyseResult analyseResult,float fix_change){
		
//		ArrayList<DecData> decarray = new ArrayList<DecData>();
		ArrayList<int[]> arraydec = new ArrayList<int[]>();
		
		int m = 0;
		int start=0;
		int end=0;
		int mark = 0;
		int[] temp_forward = new int[(int)(150*fix_change)];
		int[] temp_backward = new int[(int)(150*fix_change)];
		
		int DecPeak = 0; // 峰值
		int DecTime = 0; // 峰值位置
		int MIN_FHR = 30; 

		
		int i=0;
//		int samplesize = 1000;//TODO
		
		ArrayList<Integer> fhrbaseline = analyseResult.fhrbaseline;
		ArrayList<Integer> fittedfhrSeg = analyseResult.fittedfhrSeg;

		int samplesize = fhrbaseline.size();
		boolean sta1 = true;
		boolean sta2 = true;
		int n=0;
		int searchnum  = (int)(60 * fix_change);
		while (i < samplesize) {
			int sum_fhrbaseline = 0;
			for (n = 0; n < i + 1; n++) {
				sum_fhrbaseline = sum_fhrbaseline + fhrbaseline.get(n);
			}
			
			float mean_fhrbaseline = (float)sum_fhrbaseline / (i+1);
			
			// 第一种情况
			if ((fittedfhrSeg.get(i) <= fhrbaseline.get(i) - 25) && (fittedfhrSeg.get(i) > MIN_FHR)
					&& (i + 60 * fix_change <= samplesize) && fittedfhrSeg.get(i) <= mean_fhrbaseline - 25) {
				
				DecPeak = 300;
				
				// tyl:找减速峰值(searchnum已经进行了比例转换)
				for( m = i; (m < i + searchnum) && (m < samplesize); m++) {
					if (fittedfhrSeg.get(m) < DecPeak) {
						DecPeak = fittedfhrSeg.get(m);
						DecTime = m;
					}
				}
				
				// 向前搜索,找出减速开始位置
				for( m = DecTime; m > DecTime - 150*fix_change; m--) {
					
					if(m<0)
	                {
	                   break;
	                }
					
					int l = 0;
	                int s;
	                for (s = DecTime; s>=m; s--)
	                {
	                    temp_forward[l] = fittedfhrSeg.get(s);
	                    l = l+1;
	                }
					
	                if(DecTime-m+1>1)
	                {
	                   sort(temp_forward, DecTime-m+1);
	                }
	                
	                if ( m == DecTime-149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m)-5  && (temp_forward[0] >= fittedfhrSeg.get(DecTime)))
	                {
	                    start = m;
	                    sta1 = false;
	                    break;
	                }
	                
	                else if (fittedfhrSeg.get(m) > fhrbaseline.get(m)-5 && (temp_forward[0] >= fittedfhrSeg.get(DecTime))){
	                    start = m;
	                    sta1 = false;
	                    break;
	                }
				}
				
				// 向后搜索,找出减速结束位置
				for (m = DecTime; m < DecTime + 150*fix_change; m++) {

					if (m >= samplesize) {
						break;
					}
					
					int l = 0;
	                int s;
					for (s = DecTime; s <= m; s++) { // modified by alex on
														// Dec.9
						temp_backward[l] = fittedfhrSeg.get(s);
						l = l + 1;
					}

					if (m - DecTime + 1 > 1) {
						sort(temp_backward, m - DecTime + 1);
					}
	                
					if (m == DecTime + 149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m) - 5
							&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
						end = m;
						sta2 = false;
						break;
					}
	                
					else if (fittedfhrSeg.get(m) > fhrbaseline.get(m) - 5
							&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
						end = m;
						sta2 = false;
						break;
					}
				}
				
	            int dec_elimination_criteria1 = 0; // 减速消除标准
	            int dec_elimination_criteria2 = 0;
	            
	            // tyl: 修改
	            int endFlag = (int) (end + 100 * fix_change);
	            if( endFlag > samplesize)
	            	endFlag = samplesize;

	            for(mark=end; mark<endFlag; mark++)
	             {
	                if(fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)-fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10)
	                {
	                   dec_elimination_criteria1 = 1;
	                   break;
	                }
	             }
	            
	            // tyl: 修改
	            int startFlag = (int)(start - 100*fix_change);
	            if(startFlag < 0)
	            	startFlag = 0;
	            for(mark=start; mark>startFlag; mark--)
	               {
	                  if(fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)-fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10)
	                  {
	                     dec_elimination_criteria2 = 1;
	                     break;
	                  }
	               }

				
	            if ( (end-start) > 18*fix_change && !(sta1 ||sta2) && (dec_elimination_criteria1 == 0 && dec_elimination_criteria2 == 0) )
	             {
	            	 
	            	 DecData decele = new DecData();

	                 decele.DecPeak = DecPeak;

	                 decele.DecTime = DecTime;
	                 decele.Dec_KeepTime = end-start;
	                 
	                 arraydec.add(new int[]{DecTime,start,end,end-start,DecPeak});
	                 
	                 i = DecTime+(int)(75*fix_change);

	             }
	             else
	             {
	                 i++;
	             }
			}
			
			// 第二种情况
			 else if( (fittedfhrSeg.get(i) <= fhrbaseline.get(i)-20) && (fittedfhrSeg.get(i) > fhrbaseline.get(i) - 25) 
					 &&( fittedfhrSeg.get(i)> MIN_FHR  )&& (i+60<=samplesize) && fittedfhrSeg.get(i) <= mean_fhrbaseline - 20)
			 {
				DecPeak = 300;
				for (m = i; m < i + 60*fix_change; m++) {
					if(m >= samplesize)
						break;
					if (fittedfhrSeg.get(m) < DecPeak) {
						DecPeak = fittedfhrSeg.get(m);
						DecTime = m;
					}
				}

				for (m = DecTime; m > DecTime - 150*fix_change; m--) {

					if (m < 0) {
						break;
					}
					int l = 0;
					int s;
					for (s = DecTime; s >= m; s--) {
						temp_forward[l] = fittedfhrSeg.get(s);
						l = l + 1;
					}

					if (DecTime - m + 1 > 1) {
						sort(temp_forward, DecTime - m + 1);
					}

					if (m == DecTime - 149*fix_change && fittedfhrSeg.get(m) == fhrbaseline.get(m) - 5
							&& (temp_forward[0] >= fittedfhrSeg.get(DecTime))) {
						start = m;
						sta1 = false;
						break;
					}

					else if (fittedfhrSeg.get(m) > fhrbaseline.get(m) - 5
							&& (temp_forward[0] >= fittedfhrSeg.get(DecTime))) {
						start = m;
						sta1 = false;
						break;
					}

				}
				
				for (m = DecTime; m < DecTime + 150*fix_change; m++) {

					if (m >= samplesize) {
						break;
					}
					int l = 0;
					int s;
					for (s = DecTime; s <= m; s++) { // modified by alex on
														// Dec.8
						temp_backward[l] = fittedfhrSeg.get(s);
						l = l + 1;
					}

					if (m - DecTime + 1 > 1) {
						sort(temp_backward, m - DecTime + 1);
					}

					if (m == DecTime + 149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m) - 5
							&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
						end = m;
						sta2 = false;
						break;
					}

					else if (fittedfhrSeg.get(m) > fhrbaseline.get(m) - 5
							&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
						end = m;
						sta2 = false;
						break;
					}

				}
				
				int dec_elimination_criteria1 = 0;
	            int dec_elimination_criteria2 = 0;
	            
				if (end + 100*fix_change <= samplesize) {
					for (mark = end; mark < end + 100*fix_change; mark++) {
						if (fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)
								- fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10) {
							dec_elimination_criteria1 = 1;
							break;
						}
					}
				}
				
				else{
					
					for (mark = end; mark < samplesize; mark++) {
						if (fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)
								- fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10) {
							dec_elimination_criteria1 = 1;
							break;
						}
					}
					
				}
				
				if (start - 100*fix_change >= 0) {
					for (mark = start; mark > start - 100*fix_change; mark--) {
						if (fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)
								- fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10) {
							dec_elimination_criteria2 = 1;
							break;
						}
					}
				}
				else {
					for (mark = start; mark >= 0; mark--) {
						if (fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)
								- fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10) {
							dec_elimination_criteria2 = 1;
							break;
						}
					}
				}
				if ((end - start) > 37*fix_change && !(sta1|| sta2)
						&& (dec_elimination_criteria1 == 0 && dec_elimination_criteria2 == 0)) {

					DecData decele = new DecData();
					decele.DecPeak = DecPeak;
					decele.DecTime = DecTime;

					decele.Dec_KeepTime = end - start;

					arraydec.add(new int[]{DecTime,start,end,end-start,DecPeak});

					
					i = DecTime + (int)(75*fix_change);
//					j++;
				} else {
					i++;
				}
				
			 }
			
			// 第三种情况
			else if ((fittedfhrSeg.get(i) <= fhrbaseline.get(i) - 10) && (fittedfhrSeg.get(i) > fhrbaseline.get(i) - 20)
					&& (fittedfhrSeg.get(i) > MIN_FHR) && (i + 60*fix_change <= samplesize)
					&& fittedfhrSeg.get(i) <= mean_fhrbaseline - 15) {
				
				DecPeak = 300;
				for (m = i; m < i + 60*fix_change; m++) {
					if (fittedfhrSeg.get(m) < DecPeak) {
						DecPeak = fittedfhrSeg.get(m);
						DecTime = m;
					}
				}
				
				for (m = DecTime; m > DecTime - 150*fix_change; m--) {
					if (m < 0) {
						break;
					}
					int l = 0;
					int s;
					for (s = DecTime; s >= m; s--) {
						temp_forward[l] = fittedfhrSeg.get(s);
						l = l + 1;
					}

					if (DecTime - m + 1 > 1) {
						sort(temp_forward, DecTime - m + 1);
					}

					if (m == DecTime - 149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m) - 2
							&& (temp_forward[0] >= fittedfhrSeg.get(DecTime))) {
						start = m;
						sta1 = false;
						break;
					} else if (fittedfhrSeg.get(m) > fhrbaseline.get(m) - 2 && (temp_forward[0] >= fittedfhrSeg.get(DecTime))) {
						start = m;
						sta1 = false;
						break;
					}

				}

				for (m = DecTime; m < DecTime + 150*fix_change; m++) {
					if (m >= samplesize) {
						break;
					}
					int l = 0;
					int s;
					for (s = DecTime; s <= m; s++) { // modified by alex on
														// Dec.8
						temp_backward[l] = fittedfhrSeg.get(s);
						l = l + 1;
					}

					if (m - DecTime + 1 > 1) {
						sort(temp_backward, m - DecTime + 1);
					}

					if (m == 357) {
						int x = 1; // ceshi
						x++;
					}

					if (m == DecTime + 149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m) - 2
							&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
						end = m;
						sta2 = false;
						break;
					} else if (fittedfhrSeg.get(m) > fhrbaseline.get(m) - 2 && temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
						end = m;
						sta2 = false;
						break;
					}

				}
				
	            int dec_elimination_criteria1 = 0;
	            int dec_elimination_criteria2 = 0;
	            
				if (end + 100*fix_change <= samplesize) {
					for (mark = end; mark < end + 100*fix_change; mark++) {
						if (fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 10 && fhrbaseline.get(DecTime)
								- fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 5) {
							dec_elimination_criteria1 = 1;
							break;
						}
					}
				}

				
				
				else {
					for (mark = end; mark < samplesize; mark++) {
						if (fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 10 && fhrbaseline.get(DecTime)
								- fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 5) {
							dec_elimination_criteria1 = 1;
							break;
						}
					}
				}
				
				if(start - 100*fix_change >=0)
	             {
	               for(mark=start; mark>start-100*fix_change; mark--)
	               {
	                  if(fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 10 && fhrbaseline.get(DecTime)-fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 5)
	                  {
	                     dec_elimination_criteria2 = 1;
	                     break;
	                  }
	               }
	             }
	             else
	             {
	               for(mark=start; mark>=0; mark--)
	               {
	                  if(fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 10 && fhrbaseline.get(DecTime)-fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark)
	                		  - fhrbaseline.get(mark) + 5)
	                  {
	                     dec_elimination_criteria2 = 1;
	                     break;
	                  }
	               }
	             }
				
				if ((end - start) > 75*fix_change && !(sta1 || sta2)
						&& (dec_elimination_criteria1 == 0 && dec_elimination_criteria2 == 0)) {
//					tdtc++;
//					sline[DecTime].td |= 0x08;// Remark the FHR deceleration
												// event
					//TODO 加入到减速数组中
					DecData decele = new DecData();
					decele.DecPeak = DecPeak;
					decele.DecTime = DecTime;
					decele.Dec_KeepTime = end - start;
					
//					Dec_Peak[j] = DecPeak;
//					Dec_Time[j] = DecTime;
//					Dec_KeepTime[j] = end - start;
//					arraydec.add(new int[]{DecPeak,DecTime,end - start});
					arraydec.add(new int[]{DecTime,start,end,end-start,DecPeak});
//					decarray.add(decele);
					
					i = DecTime + (int)(75*fix_change);
					// printf("%d %d\n",start,end);
//					j++;
				} else {
					i++;
				}
			}
			else{
				i++;
			}
			
		}
		
//		return decarray;
		
		
		for(int ti=0;ti<arraydec.size();ti++){
			int[] decele = arraydec.get(ti);
			int dectime = decele[0];
			int k = dectime;
			boolean findED = false;
			boolean findLD= false;
			
			if(decele[3]>75*fix_change && srcarrfhr.get(dectime)>fhrbaseline.get(dectime)-50){
				findED = false;
				for (m = 0; m < 19*fix_change; m++) {
//					if (srcarrfhr.get(k - m).td & UCHI) {
					if(checkInUC(k-m, analyseResult)){
//						if (srcarrfhr.get(k - m).td & UCHI) {
							// modified by lixiao 20050528
							
						analyseResult.ed++; // 早发减速
//							sline[k].td += FHRED;
						findED = true;
						break;
					}
				}
//					
				
				if(findED) 
					continue;

				for (m = (int) (37 * fix_change); m <= 75 * fix_change; m++) {
					if (checkInUC(k - m, analyseResult)) {
						// modified by lixiao 20050528
						findLD = true;
						analyseResult.ld++;// 迟发减速
						// sline[k].td += FHRLD;
						break;
					}
				}
			}
			
			if (!(findED || findLD)) // 不是LD、ED，肯定是VD
			// else if (sline[k].fhr < fhrbaseline[k]-70)
			{
				analyseResult.vd++; // 变异减速
				// sline[k].td += FHRVD;
			}
			
		}
		analyseResult.decresult = arraydec;
		
		return arraydec;
		
	}
	
	private DecReturnType calDec(ArrayList<Integer> fittedfhrSeg, ArrayList<Integer> fhrbaseline, float fix_change, int index, 
			int[] temp_backward, int[] temp_forward, int keeptime) {
		
		int DecPeak = 300;
		int searchnum  = (int)(60 * fix_change);
		int m = 0;
		int samplesize = fhrbaseline.size();
		int DecTime = 0;
		boolean sta1 = true;
		boolean sta2 = true;
		int start = 0;
		int end = 0;
		int mark = 0;
		
		// tyl:找减速峰值(searchnum已经进行了比例转换)
		for( m = index; (m < index + searchnum) && (m < samplesize); m++) {
			if (fittedfhrSeg.get(m) < DecPeak) {
				DecPeak = fittedfhrSeg.get(m);
				DecTime = m;
			}
		}
		
		// 向前搜索,找出减速开始位置
		for( m = DecTime; m > DecTime - 150*fix_change; m--) {
			
			if(m<0)
            {
               break;
            }
			
			int l = 0;
            int s;
            for (s = DecTime; s>=m; s--)
            {
                temp_forward[l] = fittedfhrSeg.get(s);
                l = l+1;
            }
			
            if(DecTime-m+1>1)
            {
               sort(temp_forward, DecTime-m+1);
            }
            
            if ( m == DecTime-149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m)-5  && (temp_forward[0] >= fittedfhrSeg.get(DecTime)))
            {
                start = m;
                sta1 = false;
                break;
            }
            
            else if (fittedfhrSeg.get(m) > fhrbaseline.get(m)-5 && (temp_forward[0] >= fittedfhrSeg.get(DecTime))){
                start = m;
                sta1 = false;
                break;
            }
		}
		
		// 向后搜索,找出减速结束位置
		for (m = DecTime; m < DecTime + 150*fix_change; m++) {

			if (m >= samplesize) {
				break;
			}
			
			int l = 0;
            int s;
			for (s = DecTime; s <= m; s++) { // modified by alex on
												// Dec.9
				temp_backward[l] = fittedfhrSeg.get(s);
				l = l + 1;
			}

			if (m - DecTime + 1 > 1) {
				sort(temp_backward, m - DecTime + 1);
			}
            
			if (m == DecTime + 149*fix_change && fittedfhrSeg.get(m) <= fhrbaseline.get(m) - 5
					&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
				end = m;
				sta2 = false;
				break;
			}
            
			else if (fittedfhrSeg.get(m) > fhrbaseline.get(m) - 5
					&& temp_backward[0] >= fittedfhrSeg.get(DecTime)) {
				end = m;
				sta2 = false;
				break;
			}
		}
		
        int dec_elimination_criteria1 = 0; // 减速消除标准
        int dec_elimination_criteria2 = 0;
        
        // tyl: 修改
        int endFlag = (int) (end + 100 * fix_change);
        if( endFlag > samplesize)
        	endFlag = samplesize;

        for(mark=end; mark<endFlag; mark++)
         {
            if(fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)-fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10)
            {
               dec_elimination_criteria1 = 1;
               break;
            }
         }
        
        // tyl: 修改
        int startFlag = (int)(start - 100*fix_change);
        if(startFlag < 0)
        	startFlag = 0;
        for(mark=start; mark>startFlag; mark--)
           {
              if(fittedfhrSeg.get(mark) >= fhrbaseline.get(mark) + 15 && fhrbaseline.get(DecTime)-fittedfhrSeg.get(DecTime) < fittedfhrSeg.get(mark) - fhrbaseline.get(mark) + 10)
              {
                 dec_elimination_criteria2 = 1;
                 break;
              }
           }
        if ( (end-start) > keeptime*fix_change && !(sta1||sta2) && (dec_elimination_criteria1 == 0 && dec_elimination_criteria2 == 0) )
        {
       	 
       	 	return new DecReturnType(DecTime, start, end, end - start, DecPeak);
        }
        else
        {
        	return null;
        }
		
		
	}


	/**
	 * hmj师兄的方法，求基线的方法
	 * @param srcarrfhr
	 * @param fix_change
	 * @return
	 */
	public ArrayList<Integer> getFhrBaseline(List<Integer> srcarrfhr,float fix_change){
		List<Integer> arrdiff  = new ArrayList<Integer>();
		ArrayList<Integer> fhrbaseline  = new ArrayList<Integer>();//存储基线值
		List<Integer> fhrHist  = new ArrayList<Integer>();
		List<Float> fhrbaselinetemp  = new ArrayList<Float>();
		List<Integer> workarrfhr  = new ArrayList<Integer>();
		
		int cutstart = 0;
		int cutend = 0;
		
		List<Integer> fhr_arr = new ArrayList<Integer>(srcarrfhr);//fhr数组
		
		int doclength = fhr_arr.size();//数组长度
//		int mins_length = (int)(doclength / 75);
		
		int worklen = doclength;
		
		//求出胎心率曲线非零部分的平均数
		List<Integer> fhr_arr_notzero = new ArrayList<Integer>();
		for(int i=0;i<fhr_arr.size();i++){
			if(fhr_arr.get(i)>0){
				fhr_arr_notzero.add(fhr_arr.get(i));
			}
		}
		
		float avgfhr = getAvg(fhr_arr_notzero);
		
		// 3 cut off starts and ends where contains broken Line
		 // kisi 2016-04-06
		int tmpworklen1 = worklen;
		
		//tyl:求FHRseg[]数组，去除样本点-胎心率值平均值>60的前边部分的值
		for(int i=0;i<tmpworklen1;i++){
			
			if(fhr_arr.get(i)<1 || (avgfhr - fhr_arr.get(i))>60){
				worklen -= 1;
			}		
			else{			
				workarrfhr = new ArrayList<Integer>(fhr_arr.subList(i, tmpworklen1));
				cutstart = i;
				break;
			}	
		}
		
		int tmpworklen2 = workarrfhr.size();
		
		//tyl:求FHRseg[]数组，去除样本点-胎心率值平均值>60的后边部分的值
		for(int i=tmpworklen2-1;i>0;i--){
			if (workarrfhr.get(i) < 1 || (avgfhr - workarrfhr.get(i)) > 60){
				worklen -= 1;
			}else{
				workarrfhr = new ArrayList<Integer>(workarrfhr.subList(0, i+1));
				cutend = i;
				break;
			}		
		}
		
		
		
		//# 4 break or singular point fitting
	    //# 通过线性插值的方法去除断点
		linearInterpolationToRemoveBreakpoints(arrdiff, workarrfhr, fix_change, avgfhr);
		
		//采用频率直方图求peak值
		int peak = calPeakByIBI(workarrfhr, fhrbaselinetemp,fhrHist, worklen);	
		

		//# 进行五次滤波四修剪

		basfilter(peak, worklen ,fhrbaselinetemp);
		bastrimma(workarrfhr,fhrbaselinetemp, 20, 20, worklen);

		basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 15, 15, worklen);

	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 10, 10, worklen);

	    basfilter(peak, worklen,fhrbaselinetemp);
	    bastrimma(workarrfhr,fhrbaselinetemp, 5, 10, worklen);
	    basfilter(peak, worklen,fhrbaselinetemp);
	    int sum_fhr = 0;
		
		
		//#补全fhr
	    
	    List<Integer> fittedfhrSeg = new ArrayList<Integer>();
	    
	    
	    for(int t1=0;t1<cutstart;t1++){
	    	fittedfhrSeg.add(0);
	    	fhrbaseline.add(Math.round(fhrbaselinetemp.get(0)));
	    	sum_fhr+=fhrbaselinetemp.get(0);
	    }
	    
		for(int t1=0;t1<fhrbaselinetemp.size();t1++){
			
			fittedfhrSeg.add(workarrfhr.get(t1));
			fhrbaseline.add(Math.round(fhrbaselinetemp.get(t1)));
			sum_fhr += fhrbaselinetemp.get(t1);
		}
		
		if(cutend!=0){
			for(int t1 = 0;t1<(doclength -1 - (cutstart+cutend));t1++){
				fittedfhrSeg.add(0);
				fhrbaseline.add(Math.round(fhrbaselinetemp.get(fhrbaselinetemp.size()-1)));
				sum_fhr += fhrbaselinetemp.get(fhrbaselinetemp.size()-1);
				
			}
		}
	    

	    if(fhrbaseline.size()>0){
	    	findDeviationFromBaseline(fhrbaseline, srcarrfhr,fix_change);
	    }
 		
		

		return fhrbaseline;
		
	}
	
	public ArrayList<Integer> getRemoveAccOrDecFhr( List<Integer> srcarrfhr,int lastIndex,float fix_change){
		
		AnalyseResult analyseResult = new AnalyseResult();
		
		ArrayList<Integer> tempfhrList = new ArrayList<>(srcarrfhr);
	
		
		
		// 1、根据前面求的粗糙基线找出加减速点，{acctime,startindex,i,isacc,diffhr}
		ArrayList<int[]> accResult = getAccSimple(srcarrfhr, analyseResult, fix_change);//加速
		ArrayList<Integer> tempfhrbaseline = analyseResult.fhrbaseline;
		
		for(int i = 0; i < accResult.size(); i++){
			
			int[] acc = accResult.get(i);
			for(int j = acc[1]; j <= acc[2]; j++){
				
				tempfhrList.set(j, tempfhrbaseline.get(acc[1]));
				//tempfhrList.set(j, 0);
			}
			
		}

		ArrayList<int[]> decResult = getDecSimple(srcarrfhr, analyseResult, fix_change);
		for(int i = 0; i < decResult.size(); i++){
			
			int[] dec = decResult.get(i);

			for(int j = dec[1]; j <= dec[2]; j++){
				
				tempfhrList.set(j, tempfhrbaseline.get(dec[1]));
				//	tempfhrList.set(j, 0);
			}
			

		}
		

		 ArrayList<Integer> resultfhr = new ArrayList<>();
		 
		    for(int i = lastIndex; i < tempfhrList.size(); i++){
		    	resultfhr.add(tempfhrList.get(i));
		    }
		    

		return resultfhr;
	}
	/**
	 * tyl:求基线实时版本，其中包含去掉加减速
	 * 
	 * @param simplefhrbaseline
	 * @param fix_change
	 * @param lastIndex
	 * @param srcarrfhr
	 * @return
	 */
	public ArrayList<Integer> getFhrBaseline_RT_removeAccAndDec( List<Integer> srcarrfhr,int lastIndex,float fix_change){
		
		AnalyseResult analyseResult = new AnalyseResult();
		
		ArrayList<Integer> tempfhrList = new ArrayList<>(srcarrfhr);
		
		

		
		// 1、根据前面求的粗糙基线找出加减速点，{acctime,startindex,i,isacc,diffhr}
		ArrayList<int[]> accResult = getAccSimple(srcarrfhr, analyseResult, fix_change);//加速
		ArrayList<Integer> tempfhrbaseline = analyseResult.fhrbaseline;
		int accStartnum = 0;
		for(int i = tempfhrbaseline.size() - 1; i >= 0; i--){
			if(tempfhrbaseline.get(i) == tempfhrList.get(i))
			{
				accStartnum = tempfhrbaseline.get(i);
				break;
			}
		}
		for(int i = 0; i < accResult.size(); i++){
			
			int[] acc = accResult.get(i);
			
			for(int j = acc[1]; j <= acc[2]; j++){
				
			//	tempfhrList.set(j, tempfhrbaseline.get(acc[1]));
				tempfhrList.set(j, accStartnum);
			}
			
		}
   

		ArrayList<int[]> decResult = getDecSimple(srcarrfhr, analyseResult, fix_change);
		
		int decStartnum = 0;
		for(int i = tempfhrbaseline.size() - 1; i >= 0; i--){
			if(tempfhrbaseline.get(i) == tempfhrList.get(i))
			{
				decStartnum = tempfhrbaseline.get(i);
				break;
			}
		}
		
		for(int i = 0; i < decResult.size(); i++){
			
			int[] dec = decResult.get(i);

			for(int j = dec[1]; j <= dec[2]; j++){
				
				tempfhrList.set(j, decStartnum);
			}
			

		}

		
		// 2、对削峰过后的fhr曲线求基线
		ArrayList<Integer> fhrBaseline_RT = getFhrBaseline_RT(tempfhrList, fix_change, lastIndex);
		 
		return fhrBaseline_RT;
	}
	/**
	 * tyl：实时求基线，根据每次传进来的srcarrfhr的数据，更新基线的长度，不修改已经求出来的基线数据
	 * @param srcarrfhr
	 * @param fix_change
	 * @param lastIndex
	 * @return
	 */
	public ArrayList<Integer> getFhrBaseline_RT(List<Integer> srcarrfhr,float fix_change, int lastIndex){
		
			List<Integer> arrdiff  = new ArrayList<Integer>();
			ArrayList<Integer> fhrbaseline  = new ArrayList<Integer>();//存储基线值
			List<Integer> fhrHist  = new ArrayList<Integer>();
			List<Float> fhrbaselinetemp  = new ArrayList<Float>();
			List<Integer> workarrfhr  = new ArrayList<Integer>();
			
			int cutstart = 0;
			int cutend = 0;
			
			List<Integer> fhr_arr = new ArrayList<Integer>(srcarrfhr);//fhr数组
			
			int doclength = fhr_arr.size();//数组长度
			
			//System.out.println("目前计算的fhr数组长度 " + fhr_arr.size());
			int worklen = doclength;
			
			//求出胎心率曲线非零部分的平均数
			List<Integer> fhr_arr_notzero = new ArrayList<Integer>();
			for(int i=0;i<fhr_arr.size();i++){
				if(fhr_arr.get(i)>0){
					fhr_arr_notzero.add(fhr_arr.get(i));
				}
			}
			
			float avgfhr = getAvg(fhr_arr_notzero);
			
			// 3 cut off starts and ends where contains broken Line
			 // kisi 2016-04-06
			int tmpworklen1 = worklen;
			//tyl:求FHRseg[]数组，去除样本点-胎心率值平均值>60的前边部分的值
			for(int i=0;i<tmpworklen1;i++){
				
				if(fhr_arr.get(i)<1 || (avgfhr - fhr_arr.get(i))>60){
					worklen -= 1;
				}		
				else{			
					workarrfhr = new ArrayList<Integer>(fhr_arr.subList(i, tmpworklen1));
					cutstart = i;
					break;
				}	
			}
			
			int tmpworklen2 = workarrfhr.size();
			//tyl:求FHRseg[]数组，去除样本点-胎心率值平均值>60的后边部分的值
			for(int i=tmpworklen2-1;i>0;i--){
				if (workarrfhr.get(i) < 1 || (avgfhr - workarrfhr.get(i)) > 60){
					worklen -= 1;
				}else{
					workarrfhr = new ArrayList<Integer>(workarrfhr.subList(0, i+1));
					cutend = i;
					break;
				}		
			}
			
			
			
			//# 4 break or singular point fitting
		    //# 通过线性插值的方法去除断点
			linearInterpolationToRemoveBreakpoints(arrdiff, workarrfhr, fix_change, avgfhr);
			
			//采用频率直方图求peak值
			int peak = calPeakByIBI(workarrfhr, fhrbaselinetemp,fhrHist, worklen);	
			
			//# 进行五次滤波四修剪

			basfilter(peak, worklen ,fhrbaselinetemp);
			bastrimma(workarrfhr,fhrbaselinetemp, 20, 20, worklen);

			basfilter(peak, worklen,fhrbaselinetemp);
		    bastrimma(workarrfhr,fhrbaselinetemp, 15, 15, worklen);

		    basfilter(peak, worklen,fhrbaselinetemp);
		    bastrimma(workarrfhr,fhrbaselinetemp, 10, 10, worklen);

		    basfilter(peak, worklen,fhrbaselinetemp);
		    bastrimma(workarrfhr,fhrbaselinetemp, 5, 10, worklen);
		    basfilter(peak, worklen,fhrbaselinetemp);
		    
		    bastrimma(workarrfhr,fhrbaselinetemp, 3, 5, worklen);
		    basfilter(peak, worklen,fhrbaselinetemp);
		    int sum_fhr = 0;
			
			
			//#补全fhr
		    
		    List<Integer> fittedfhrSeg = new ArrayList<Integer>();
		    
		    
		    for(int t1=0;t1<cutstart;t1++){
		    	fittedfhrSeg.add(0);
		    	fhrbaseline.add(Math.round(fhrbaselinetemp.get(0)));
		    	sum_fhr+=fhrbaselinetemp.get(0);
		    }
		    
			for(int t1=0;t1<fhrbaselinetemp.size();t1++){
				
				fittedfhrSeg.add(workarrfhr.get(t1));
				fhrbaseline.add(Math.round(fhrbaselinetemp.get(t1)));
				sum_fhr += fhrbaselinetemp.get(t1);
			}
			
			if(cutend!=0){
				for(int t1 = 0;t1<(doclength -1 - (cutstart+cutend));t1++){
					fittedfhrSeg.add(0);
					fhrbaseline.add(Math.round(fhrbaselinetemp.get(fhrbaselinetemp.size()-1)));
					sum_fhr += fhrbaselinetemp.get(fhrbaselinetemp.size()-1);
					
				}
			}
			
			
			int fhrbasev = 0;
			if(fhrbaseline.size()>0)
				fhrbasev = sum_fhr/fhrbaseline.size();
			
		    

		    if(fhrbaseline.size()>0){
		    	findDeviationFromBaseline(fhrbaseline, srcarrfhr,fix_change);
		    } 
	 		
			

		    ArrayList<Integer> resultfhrbaseline = new ArrayList<>();

		    for(int i = lastIndex; i < fhrbaseline.size(); i++){
		    	resultfhrbaseline.add(fhrbaseline.get(i));
		    }
		    
			return resultfhrbaseline;
			
		}
	private int calPeakByIBI(List<Integer> workarrfhr, List<Float> fhrbaselinetemp, List<Integer> fhrHist, int worklen) {
		
		int workarrlen = workarrfhr.size();
		int ti = 2;
		
		//因为这个地方变成了一秒4点，这个地方的差值阈值可能以后需要再调整
		while(ti<workarrlen-2){
			int wl = workarrfhr.get(ti-1);
			int wm = workarrfhr.get(ti);
			int wr = workarrfhr.get(ti + 1);
			
			if(Math.abs(wm-wl)>10 && Math.abs(wm - wr)>10 && Math.abs(wl-wr)<5){				
				workarrfhr.set(ti, (wl + wr) / 2);												
			}
			ti ++;		
		}
		
		//这个地方和0.8秒和0.25秒暂时认定没有关系
		for(int i1=0;i1<worklen;i1++){
			fhrbaselinetemp.add((float)workarrfhr.get(i1));			
			if(workarrfhr.get(i1) != 0){
				float tempfhr = (float) (60000 / (float)(workarrfhr.get(i1)) + 0.5);
				if (tempfhr >600 || tempfhr<300){
					fhrHist.add(0);
				}else{
					fhrHist.add((int)tempfhr);
				}				
			}			
		}
		
		
		//# 现在fhrHist为脉冲间隔值数组 （去除区间[300-600]之外)
	    //# 求数组中每个脉冲间隔值的频数
		
		List<histInformation> arrayhis = histgram(fhrHist,worklen);		
		 //# 脉冲间隔值从低到高累加频数数组
		List<Integer> arraycum = cumulant(arrayhis);
		
		int sumvalue = isum(arrayhis);
		
		int startpoint = 0;
		//# Find position where 1/8 of the histogram area 找到频率累积量大于0.875*频率总和
		for(int i1=0;i1<hislen;i1++){
			
			if(arraycum.get(i1) > 0.875 * sumvalue){
				startpoint = i1 - 1;
				break;
			}
		}
		
		int peak = maxpt(arrayhis);
		//System.out.println("maxpt peak:"+peak);
		for(int i1=startpoint-5;i1>=5;i1--){
			
			if(arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-1).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-2).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-3).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-4).frequency &&
					arrayhis.get(i1).frequency > arrayhis.get(i1-5).frequency &&
					(arrayhis.get(i1).frequency > 0.005 * sumvalue || Math.abs(299 + startpoint - peak) <= 30)){			
					peak = 300 + i1;
					break;					
			}			
		}
		
		if(peak ==0){
			peak = 100;
		}else{
			peak = (int) (60000.0/peak);
		}
		return peak;
	}


	private void linearInterpolationToRemoveBreakpoints(List<Integer> arrdiff, List<Integer> workarrfhr, float fix_change, float avgfhr) {
		
		int tmpbp = 0;
		int worklen = workarrfhr.size();
		for(int i=0;i<worklen-1;i++){
			arrdiff.add(Math.abs(workarrfhr.get(i+1)-workarrfhr.get(i)));
		}
		
		int i = 0;
		int searchpoints = 0;
		
		searchpoints = 400;
		
		searchpoints = (int)(400 * fix_change);
		
		while(i<worklen-1){
			
			//# 如果两点间突变大于30：
			if (arrdiff.get(i) > 30){
				//# 向后搜索400个样本点，找到一个满足：如果FHRdiff[j]绝对值小于10并且FHRseg[j+1]与平均数meanFHR之差的绝对值小于50				
				for(int j=i;j<i+searchpoints;j++){
					
					if(j>(worklen-1) || j>=arrdiff.size()){
						break;
					}
					
					
					if(arrdiff.get(j)<10 && Math.abs(workarrfhr.get(j+1) - avgfhr)<50 && workarrfhr.get(j+1)!=0 ){
						//#对于出现两个相邻采样点小于10且当前胎心率值比之前胎心率值的差绝对值小于50且不等于0的情况
						tmpbp = j;// #记录下当前点的前一点作为伪迹段搜寻的结束点						
						break;					
					}
					//若j在i之后399个点仍没有找到满足的伪迹段结束点,则将temp赋值为0
					if(j == (i+searchpoints-1)){
						tmpbp = 0;
					}										
				}
				
				//若temp不等于0，则伪迹段所有点与点间的差都赋予0
				if(tmpbp !=0){
					
					for(int k=0;k<tmpbp;k++){
						arrdiff.set(k, 0);
					}
					
					//# 5 interval between value
					float interval = (workarrfhr.get(tmpbp) - workarrfhr.get(i)) / (tmpbp - i); //#斜率
					//#线性插值
					for(int s=i;s<tmpbp-1;s++){
						workarrfhr.set(s + 1, (int) (workarrfhr.get(s)+interval));
					}
					
					// 6 tmpbp+1 to i reloop
					i = tmpbp + 1;
					tmpbp = 0;
					continue;					
				}								
			}			
			i ++;
		}
		
	}
	/**
	 * tyl:实时版本，修改真加速和小加速的判断条件
	 * isacc = 0:表示加速时间足够，幅度足够; =1：表示加速时间足够，幅度不够; =2表示加速时间不够，但是幅度足够
	 * @param isover32
	 * @param length
	 * @param avgfhr
	 * @param arrayfhr
	 * @param fhrbaseline
	 * @param fix_change
	 * @return
	 */
	public ArrayList<int[]> acc_RT(int isover32, int length, float avgfhr, List<Integer> arrayfhr,
			 List<Integer> fhrbaseline,float fix_change,List<FMData> fmobarray,List<UcPeak> ucdetect_list){
		
		ArrayList<int[]> arrayacc = new ArrayList<int[]>();

		int searchstep = (int)(38*fix_change); // 真加速的步伐
		
		int abnormal = 250; // 加速时间超过 1分半左右判断为基线变异
		isover32 = 0;
		int keeptime = 0;
		int keepvalue = 0;
				
		if (isover32 == 1){	
			
			keeptime = (int)(11 * fix_change);
			keepvalue = 10;						
			
		}
		else{
			
			keeptime  = (int)(18 * fix_change);
			keepvalue = 15;								
		}
		
		int searchrange = (int)(60 * fix_change);
		
		// 这里也需要修改
		int small_keeptime = (int)(8 * fix_change);
		
		// tyl: 基准值，fhr曲线值比基线值超过这个数才可以判断是否是加速
		int small_keepvalue = 10; 
		int isacc = -1;
		
		int i=0;
		
		while(i < length){
			
			
			// 1、当i还没有来到新加的点的位置的时候，按原来的差值计算
			int curbaselineValue = 0;
			if(i > length - 20)
			{
				curbaselineValue = fhrbaseline.get(length - 20); 
			}
			
			// 2、当i来到新加的点的位置的时候，按照临界点的差值计算
			else
			{
				if(i == fhrbaseline.size())
					break;
				//TODO 改成不将之前的结果放进去
				curbaselineValue = fhrbaseline.get(i); 
			}
			
			
			int curfhr = arrayfhr.get(i);
		/*	int curbaselineValue = fhrbaseline.get(i);*/
			
			int peak = 0;
			int time = 0;
			int fix_dif = 0;

			// 达到真加速的标准，（真加速的标准按照规范定义）
			if((curfhr > curbaselineValue + keepvalue) && (curbaselineValue + keepvalue > (avgfhr*0.9))){
				
				boolean estate = false;
				boolean sstate = false;
				// 向后续60元素中搜索最大值
				for(int m = i; m < i+searchrange; m++){
					if(m>=arrayfhr.size())
						break;
					
					// 求得峰值和峰值对应的时间点
					if(arrayfhr.get(m) > peak)
					{
						peak = arrayfhr.get(m);
						time = m;
					}
				}
				int startindex = 0;
				int currmax = 0;

				
				 // 搜索峰值前55个元素
			    for(int m=i-1;m >= (int)(i - 55*fix_change);m--){
			    	

			    	if (m<0){
			    		break;
			    	}
			    	
			    	int m_fhr = arrayfhr.get(m);
			    	int m_fhrbaseline = fhrbaseline.get(m);
			    	
			    	if(m_fhr > currmax){
			    		currmax = m_fhr;
			    	}
			    	if (((m == 0 || m == (int)(time - 54*fix_change)) && (m_fhr >= m_fhrbaseline + fix_dif) && currmax <= peak)){
			    		startindex = m;
			    		sstate = true;
			    		break;
			    	}
			    	else if ((m_fhr <= m_fhrbaseline + fix_dif) && (currmax <= peak)){
			    		startindex = m;
			    		sstate = true;
			    		break;
			    	}					    	
			    }
			    currmax = 0;
			    
			    // 搜索峰后的55个元素
			    int endindex = 0;

			    for(int m=time;m<(time + 55*fix_change);m++){	
			    	
			    	if(m>=arrayfhr.size()){			    		
			    		break;
			    	}
			    	
			    	int m_fhr = arrayfhr.get(m);
			    	int m_fhrbaseline = fhrbaseline.get(m);
			    	
			    	if(arrayfhr.get(m)>currmax){
			    		currmax = arrayfhr.get(m);	    		
			    	}
			    	
			    	//>= fhrbaseline.get(m) + 2 这个地方需要看看怎么修改
			    	if ((m == length-1 || m == (int)((time + 54*fix_change))) && 
			    			(m_fhr >= m_fhrbaseline + fix_dif) && currmax <= peak){
			    		
			    		endindex = m;
			    		estate = true;
			    		break;
			    	}
			    	else if((m_fhr < m_fhrbaseline + fix_dif) && (currmax <= peak)){
			    		endindex = m;
			    		estate = true;
			    		break;		    		
			    	}			    	
			    }
			    
			    // 达到持续时间，且峰值足够
			    if((endindex - startindex >= keeptime && endindex - startindex <= abnormal && estate && sstate)){


			    	int acctime = i - startindex;
			    	isacc = 0;
			    	int diffhr = curfhr - curbaselineValue;
			    	int[] acctmp = new int[]{acctime,startindex,i,isacc,diffhr};
			    	arrayacc.add(acctmp);
			    	i = i + searchstep;
			    }
			    else if((endindex - startindex > abnormal) && estate && sstate){
			    	
			    	
			    	// 如果伴随着胎动和宫缩出现，则为胎儿活跃
			    	
			    	
					int fmCount = 0;//胎儿活动统计

					//如果在这个范围内的胎动信号数量大于3则表示胎儿活跃
					for(int fmIndex = 0; fmIndex < fmobarray.size(); fmIndex++){
						
						if(fmobarray.get(fmIndex).maxdif_index > startindex 
								&& fmobarray.get(fmIndex).maxdif_index < endindex)
						{
							fmCount++;
						}
					}
					
					
					int tocoCount = 0;//宫缩统计
					
					for(int fmIndex = 0; fmIndex < ucdetect_list.size(); fmIndex++){
						
						if(ucdetect_list.get(fmIndex).u_start > startindex 
								&& ucdetect_list.get(fmIndex).u_start < endindex)
						{
							tocoCount++;
						}
					}
					
					if(tocoCount >= 10 && fmCount >= 10){
						
						System.out.println("s:" + startindex + "e:" + endindex  + "   不是基线变异");
					}
			    	// 如果没有胎动和宫缩出现，则为基线变异
			    	
			    	
					else{
				    	
				    	// 作为下一次计算基线的FHR曲线的起始点
				    	globalFlag = time;
				    	
						System.out.println("s:" + startindex + "e:" + endindex  + "基线变异");
					}
			    	i = i+2;

			    	
			    }
			    // 持续时间不够
			    else{
			    	
			    	if(estate && sstate && (endindex - startindex < abnormal))
			    	{

					    int acctime = i - startindex;
					    isacc = 1;
					    int diffhr = curfhr - curbaselineValue;
					    int[] acctmp = new int[]{acctime,startindex,i,isacc,diffhr};
					    arrayacc.add(acctmp);
				    
			    	}
			    	
			    	i = i + 2;
			    }
			    
			}
			
			// 达到小加速标准（小加速的参数设置是根据自己经验设置，可调整）
			else if((curfhr > curbaselineValue + small_keepvalue)){

				int startindex = -1;
				boolean sstate = false;
				
				 // 搜索峰值前55个元素
			    for(int m=i-1;m>=(int)(i - 55*fix_change);m--){
			    	
			    	if (m<0){
			    		break;
			    	}
			    	int m_fhr = arrayfhr.get(m);
			    	int m_fhrbaseline = fhrbaseline.get(m);

			    	if((m==0||m==(int)(i - 54*fix_change)))
			    	{
			    		startindex = m;
			    		sstate = true;
			    		break;
			    	}
			    	else if((m_fhr < m_fhrbaseline + fix_dif)){
			    		startindex = m;
			    		sstate = true;
			    		break;		    		
			    	}	
			    }


			    // 达到持续时间，且峰值足够
			    if(((i - startindex) >= small_keeptime / 2) && sstate){

			    	int acctime = i - startindex;
			    	isacc = 2;
			    	int diffhr = curfhr - curbaselineValue;
			    	int[] acctmp = new int[]{acctime,startindex,i,isacc,diffhr};

			    	arrayacc.add(acctmp);
	
			    }
			    // 持续时间不够
			    else if(sstate){
			    	
			    	int acctime = i - startindex;
			    	isacc = 3;
			    	int diffhr = curfhr - curbaselineValue;
			    	int[] acctmp = new int[]{acctime,startindex,i,isacc,diffhr};
			    	arrayacc.add(acctmp);
	
			    }
		    	i = i + 2;
				
			}
			else {
				i++;
			}
		}

		return arrayacc;
	}

	public ArrayList<int[]> accSimple(List<Integer> arrayfhr, List<Integer> fhrbaseline, float fix_change){
		
		ArrayList<int[]> arrayacc = new ArrayList<int[]>();
		int searchstep = (int)(38*fix_change);
		int keeptime = (int)(5*fix_change);
		int keepvalue = 5;
		
		int searchrange = (int)(60*fix_change);
		
		List<Integer> fittedfhrSeg = arrayfhr;
		
		float avgfhr = getAvg(fhrbaseline);
		int i = 0;
		int length = fittedfhrSeg.size();

		while(i < length){
			
			int curfhr = fittedfhrSeg.get(i);
			int curbaselinevalue = fhrbaseline.get(i);
			
			if(curfhr  > curbaselinevalue + keepvalue){
				
				int accpeak = 0;//减速峰值
				int acctime = 0;
				int startIndex = 0;
				int endIndex = 0;
				boolean sstate = false;
				boolean estate = false;
				
				// 找峰值
				for(int m = i; m < i+searchrange; m++){
					
					if(m >= fittedfhrSeg.size())
						break;
					if(fittedfhrSeg.get(m) > accpeak)
					{
						accpeak = fittedfhrSeg.get(m);
						acctime = m;
					}
				}

				int fix_dif = 0;
				
				// 向前搜索
				for(int m = acctime - 1; m >= (int)(acctime - 55*fix_change); m--){
					
					if(m < 0)
						break;
					
					int m_fhr = fittedfhrSeg.get(m);
					int m_fhrbaseline = fhrbaseline.get(m);
					
					if((m==0 || m==(int)(acctime - 54*fix_change))){
						
						startIndex = m;
						sstate = true;
						break;
					}
					
					else if((m_fhr <= m_fhrbaseline)){
						
						startIndex = m;
						sstate = true;
						break;
					}
				}
				
				// 向后搜索
				
				for(int m = acctime; m < (acctime + 55*fix_change); m++){
					
					if(m > fittedfhrSeg.size())
						break;
					
					int m_fhr = fittedfhrSeg.get(m);
					int m_fhrbaseline = fhrbaseline.get(m);
					
					if((m == fittedfhrSeg.size() - 1 )|| (m == (int)(acctime - 55*fix_change))){
						
						endIndex = m;
						estate = true;
						break;
					}
					else if((m_fhr <= m_fhrbaseline + fix_dif)){
						
						endIndex = m;
						estate = true;
						break;
					}  
				}
			//	System.out.println("差值：" + (endIndex - startIndex));
				// 判断条件，达到加速
				if((endIndex - startIndex) >= keeptime && sstate && estate){

					int acckeeptime = endIndex - startIndex;
					
					int[] acctemp = new int[]{acctime, startIndex, endIndex, acckeeptime, accpeak};
					arrayacc.add(acctemp);
					i = acctime + searchstep;
				}
				else
				{
					i++;
				}
			}
			else
			{
				i++;
			}
		}
		return arrayacc;
	}
	
	/**
	 * //datatype 0:0.8秒     1：0.25秒
	 * //tyl:int数组中的四个参数：acctime:加速持续时间,startindex:开始下标,endindex:结束下标,isacc:是否是真加速,diffhr
	 * 
	 * @param isover32 # 孕周是否超过32周
	 * @param length
	 * @param avgfhr
	 * @param arrayfhr
	 * @param fhrbaseline
	 * @param fix_change
	 * @return
	 */
	public ArrayList<int[]> acc(int isover32, int length, float avgfhr, List<Integer> arrayfhr, List<Integer> fhrbaseline,float fix_change){
		ArrayList<int[]> arrayacc = new ArrayList<int[]>();
		
		//float fix_change = 3.2f;
		
		//int searchstep = 38;//这里可能需要改
		
		int searchstep = (int)(38 * fix_change); // 每次搜索的步伐
		
		int abnormalFlag = 200; // 定义加速时间超过该阈值为变异
		
		int keeptime = 0;
		int keepvalue = 0;
						
		if (isover32 == 1){	
			
			keeptime = (int)(11 * fix_change);
			keepvalue = 10;						
			
		}
		else{
			//tyl: 参数下降
			//keeptime = (int)(15 * fix_change);
			keeptime  = (int)(18 * fix_change);
			keepvalue = 13;								
		}
		
		int searchrange = (int)(60 * fix_change);
		//TODO 这里也需要修改
		int small_keeptime = (int)(8 * fix_change);

		// tyl: 基准值，fhr曲线值比基线值超过这个数才可以判断是否是加速
		int small_keepvalue = 10; 
		
		int i=0;
		while(i<length){
			
			int curfhr = arrayfhr.get(i);
			int curbaselinevaue = fhrbaseline.get(i);
			if(i==230){//测试用
				int j=0;
			}
			//if ((arrayfhr.get(i) > fhrbaseline.get(i) + keepvalue) && (fhrbaseline.get(i) + keepvalue > (avgfhr*0.93))){
			if ((arrayfhr.get(i) > curbaselinevaue + keepvalue) && (curbaselinevaue + keepvalue > (avgfhr*0.9))){
				
				int accpeak = 0;// 峰值
			    int acctime = 0;//持续时间
			    int startindex = 0;
			    int endindex = 0;
			    boolean sstate = false;
			    boolean estate = true;
			    
			    //# 后续60元素中搜索最大值
			    for(int m =i;m<i+searchrange;m++){
			    	
			    	if(m>=arrayfhr.size()){
			    		break;
			    	}
			    	if(arrayfhr.get(m) > accpeak){
			    		accpeak = arrayfhr.get(m);
			    		acctime = m;
			    	}	    	
			    }
			    int currmax = 0;
			    
			    int fix_dif = 0;
			    
			    // 搜索峰值前55个元素
			    for(int m=acctime-1;m>=(int)(acctime - 55*fix_change);m--){
			    	
			    	if (m<0){
			    		break;
			    	}
			    	int m_fhr = arrayfhr.get(m);
			    	int m_fhrbaseline = fhrbaseline.get(m);

			    	if (m_fhr > currmax){
			    		currmax = m_fhr;
			    	}
			    	if (((m == 0 || m == (int)(acctime - 54*fix_change)) && (m_fhr > m_fhrbaseline + fix_dif) && currmax < accpeak)){
			    		startindex = m;
			    		sstate = true;
			    		break;
			    	}
			    	else if ((m_fhr <= m_fhrbaseline + fix_dif) && (currmax < accpeak)){
			    		startindex = m;
			    		sstate = true;
			    		break;
			    	}					    				    	
			    }
			    
			    currmax = 0;
			    // 搜索峰值后55个元素
			    for(int m=acctime;m<(acctime + 55*fix_change);m++){	    	
			    	if(m>=arrayfhr.size()){			    		
			    		break;
			    	}
			    	
			    	if(arrayfhr.get(m)>currmax){
			    		currmax = arrayfhr.get(m);	    		
			    	}
			    	//>= fhrbaseline.get(m) + 2 这个地方需要看看怎么修改
			    	if ((m == length-1 || m == (int)((acctime + 54*fix_change))) && (arrayfhr.get(m) >= fhrbaseline.get(m) + 2) && currmax <= accpeak){
			    		endindex = m;
			    		estate = true;
			    		break;
			    	}
			    	else if((arrayfhr.get(m) < fhrbaseline.get(m) + 2) && (currmax <= accpeak)){
			    		endindex = m;
			    		estate = true;
			    		break;		    		
			    	}			    	
			    }
			    //达到真加速的持续 时间
			    if (((endindex - startindex) >= keeptime) && ((endindex - startindex) <= 200)  && sstate && estate){
/*			    	System.out.println("k: " + startindex + " e: " + endindex);*/
			    	int isacc = 0;
			    	//如果达到真加速的幅度
			    	int accfhr = arrayfhr.get(acctime);
			    	int accfhrbasevalue = fhrbaseline.get(acctime);
			    	int diffhr = accfhr - accfhrbasevalue;
			    	if(diffhr > keepvalue){
			    		
			    		isacc = 1;			    		
			    	}
			    	int[] acctmp = new int[]{acctime,startindex,endindex,isacc,diffhr};
			    	arrayacc.add(acctmp);
			    	i = acctime + searchstep;			    	
			    }
/*			    else if(endindex - startindex > abnormalFlag  && sstate && estate)
			    {
			    	System.out.println("发生变异");
			    }*/
			    //小加速, 加速时间只是达到符合加速时间，持续的时间不够长
			    else if(endindex - startindex >= small_keeptime && endindex - startindex < keeptime  && sstate && estate){	
			    	int accfhr = arrayfhr.get(acctime);
			    	int accfhrbasevalue = fhrbaseline.get(acctime);
			    	int diffhr = accfhr - accfhrbasevalue;
			    	int isacc = 0;
			    	int[] acctmp = new int[]{acctime,startindex,endindex,isacc,diffhr};
			    	arrayacc.add(acctmp);
			    	i = acctime + searchstep;			    	
			    }
			    else{
			    	i++;
			    }			    			    			  			    
			}
			else{
				i++;
			}
		}
		
		return arrayacc;
		
	}
	
	
	//这个方法主要参照了2015 胎心宫缩图参数分析和胎儿状态评估方法的研究_李晓东 论文中的方法2.2.3节基线的修正
	public void findDeviationFromBaseline(List<Integer> baseline,List<Integer> fhrline,float fix_change){
		
		int basepoint = getBasePointFromBaseline(baseline,fhrline);
		//basepoint = 150;
		//System.out.println("dev basepoint:" + basepoint);
		//#找出与基准值相差大于2次每分的基线片段
		
		List<Integer> deviations = new ArrayList<Integer>();
		int baselineindex = 0;
		int baselinelen = baseline.size();
		
		int start = 0;
		int stop = 0;
		
		float max_k = 0.02f;
		
		while(baselineindex<baselinelen){
			
			//#如果差大于2
			if ((baseline.get(baselineindex)-basepoint)>2){
				//#标记新嫌疑偏差起点
				start = baselineindex;
				while(baselineindex<baselinelen){
					
					if (baseline.get(baselineindex)<basepoint+2){
						break;
					}
					baselineindex+=1;
				}
				
				if (baselineindex==baselinelen-1 && baseline.get(baselineindex)>basepoint+2){
					stop = baselineindex;
				}else{
					stop = baselineindex-1;
				}
				
				//#极值差
				int[] tmp = getExtremeDiff(baseline.subList(start, stop+1));
				int exdiff = tmp[0];
				int e_xdif = tmp[1];
				
				
				//#极值点斜率
				float exk =(float) exdiff/(e_xdif/fix_change);
				int crosscount = 0;
				
				for(int ti=start;ti<stop;ti++){
					if ((fhrline.get(ti)-baseline.get(ti))*(fhrline.get(ti+1)-baseline.get(ti))<0){
						crosscount+=1;
					}
				}
				
				//#交叉率
				float excross = crosscount/(stop-start+1);
				if (exdiff>2 && Math.abs(exk)>0.02 && excross<0.15){
					//deviations.add(e)
					//修正
					for(int ti = start;ti<stop+1;ti++){
						baseline.set(ti, (int)(0.5*baseline.get(ti) + 0.5*basepoint));
					}				
				}		
			}
			else{
				baselineindex+=1;
			}
				
			
		}
		
		
	}
	
	//#确定胎心率基线的偏差部分
	//#检查极值差
	
	public int[] getExtremeDiff(List<Integer> segm){
		
		int maxv = Collections.max(segm);
		int minv = Collections.min(segm);
		
		int maxindex = segm.indexOf(maxv);
		int minindex = segm.indexOf(minv);
		
		int mdif = maxv - minv;
		int indexdif = Math.abs(maxindex - minindex);
		return new int[]{mdif,indexdif};
	}
	
	//#寻找胎心率基线中的基准值
	
	public int getBasePointFromBaseline(List<Integer> baseline,List<Integer> fhrline){
		int bmin = Collections.min(baseline);
		int bmax = Collections.max(baseline);
		
		int frelen = bmax - bmin + 1;
		//System.out.println("frelen1:" + frelen);
		
		if(frelen<3){			
			frelen=3;
		}
		
		List<Integer> frevarray = new ArrayList<Integer> ();
		for(int i=0;i<frelen;i++){
			frevarray.add(0);
		}
		
		for(int i=0;i<baseline.size();i++){
			frevarray.set(baseline.get(i)-bmin, frevarray.get(baseline.get(i)-bmin)+1);			
		}
		
		
		
		Integer [][] value_fre_array = new Integer[frelen][2];
		
		
		for(int i=0;i<frelen;i++){
			value_fre_array[i][0] = bmin+i;
			value_fre_array[i][1] = frevarray.get(i);
		}
		
		Arrays.sort(value_fre_array,new Comparator<Integer[]>(){
			@Override
			public int compare(Integer[] o1, Integer[] o2) {
			
				if (o1[1] < o2[1]) {
					return 1;
				}
				else if (o1[1] > o2[1]) {
					return -1;
				} else {
					return 0;
				}
			}
		}
				
		);
		
		//System.out.println("frelen:" + frelen);
		
		Integer maxfrev1 = value_fre_array[0][0];
		Integer maxfrev2 = value_fre_array[1][0];
		Integer maxfrev3 = value_fre_array[2][0];
		
		Integer[] crosspoints = new Integer[]{0,0,0};
		int fhrlen = fhrline.size();	
		
		for(int ti=0;ti<fhrlen-1;ti++){
			
			if(cross(fhrline.get(ti),fhrline.get(ti+1),maxfrev1)){
				crosspoints[0]+= 1;				
			}
			if(cross(fhrline.get(ti),fhrline.get(ti+1),maxfrev2)){
				crosspoints[1]+= 1;				
			}
			if(cross(fhrline.get(ti),fhrline.get(ti+1),maxfrev3)){
				crosspoints[2]+= 1;				
			}
			
		}
		
		//List<Integer> numList = Arrays.asList(crosspoints);
		ArrayList<Integer> numList = new ArrayList<Integer>(Arrays.asList(crosspoints));
		int maxvalue = Collections.max(numList);
		int maxindex = numList.indexOf(maxvalue);
		
		int basepointv = value_fre_array[maxindex][0];
		
		return basepointv;	
		
	}
	
	public boolean cross(int v1,int v2,int v){
		if((v1-v)*(v2-v)<0){
			return true;
		}else{
			return false;
		}
			
	}
	
	
	
	public void bastrimma(List<Integer> workarrfhr,List<Float> fhrbaselinetemp,int over,int under,int length ){
		
		List<Float> arrfhrny = new ArrayList<Float>();
		int shake = 0;
		int shakepoint = 0;
		int shakestart = 0;

		for(int i=0;i<workarrfhr.size();i++){
			arrfhrny.add((float)workarrfhr.get(i));
		}
		/*
		 *     for fhr in workarrfhr:
        arrfhrny.append(fhr)
		 */
		
		for(int i=0;i<length;i++){
			if(shake ==0){
				if(workarrfhr.get(i)> fhrbaselinetemp.get(i) + over){
					shake = 1;
					shakepoint = i;					
				}else if( workarrfhr.get(i) < fhrbaselinetemp.get(i) - under){
					shake = 2;
			        shakepoint = i;
				}
			}		
			else if(shake == 1){
				if (workarrfhr.get(i) < fhrbaselinetemp.get(i)){
					shake = 0;
					for(int k=shakepoint;k>=0;k--){
						if (workarrfhr.get(k) < fhrbaselinetemp.get(k)){
							shakestart = k;
							break;
						}
					}				
					for(int j=i-1;j>=shakestart;j--){
						arrfhrny.set(j, fhrbaselinetemp.get(j));
					}					
				}
				
			}
			
			else if(shake ==2){
				
				if (workarrfhr.get(i) > fhrbaselinetemp.get(i)){
					shake = 0;
					for(int k=shakepoint-1;k>=0;k--){
						if(workarrfhr.get(k)>fhrbaselinetemp.get(k)){
							shakestart = k;
							break;		
						}
					}
					
					for(int j=i-1;j>=shakestart;j--){
						arrfhrny.set(j, fhrbaselinetemp.get(j));
						//arrfhrny[j] = fhrbaselinetemp[j]
					}									
				}	
			}	
		}
		
		for(int k = 0;k<length;k++){
			//fhrbaselinetemp[i] = arrfhrny[i]
			fhrbaselinetemp.set(k, arrfhrny.get(k));
		}
		
	}
	
	public void basfilter(int peak,int length,List<Float> fhrbaselinetemp){
		
		float base0 = peak;
		
		// tyl: 对基础胎心率值进行更新
		for(int i=length-1;i>=0;i--){
			if (Math.abs(fhrbaselinetemp.get(i) - peak) <= 50){
				base0 = (float) (0.975 * base0 + 0.025 * fhrbaselinetemp.get(i));
			}			
		}
		for(int i=0;i<length;i++){
			
			if(Math.abs(fhrbaselinetemp.get(i) - peak) <= 50){
				if(i==0){
					//fhrbaselinetemp[0] = 0.975 * base0 + 0.025 * fhrbaselinetemp[0];
					fhrbaselinetemp.set(0, (float) (0.975 * base0 + 0.025 * fhrbaselinetemp.get(0)));
				}else{
					fhrbaselinetemp.set(i, (float) (0.975 * fhrbaselinetemp.get(i-1) + 0.025 * fhrbaselinetemp.get(i)));
				}		
			}else{
				if(i==0){
					fhrbaselinetemp.set(0,base0);
				}else{
					fhrbaselinetemp.set(i, fhrbaselinetemp.get(i-1));
				}		
			}	
		}
		
		for(int i=length-2;i>=0;i--){
			fhrbaselinetemp.set(i, (float) (0.975 * fhrbaselinetemp.get(i + 1) + 0.025 * fhrbaselinetemp.get(i)));
			//fhrbaselinetemp[i] = 0.975 * fhrbaselinetemp[i + 1] + 0.025 * fhrbaselinetemp[i]
		}
		
		
	}
	
	public int maxpt(List<histInformation> dicthis){
		
		int maxPoint = 0;
		int mark = 600;
		
		for(int i=0;i<dicthis.size();i++){
			if(dicthis.get(i).frequency > maxPoint && dicthis.get(i).event !=0){
				maxPoint = dicthis.get(i).frequency;
				mark = dicthis.get(i).event;
			}		
		}		
		return mark;		
	}
	
	public int isum(List<histInformation> arrayhis){
		
		int isum = 0;
		for(int i=0;i<arrayhis.size();i++){
			isum += arrayhis.get(i).frequency; 	
		}
		return isum;
		
	}
	
	//计算300-600各个的频数
	public List<histInformation> histgram(List<Integer> fhrHist,int worklen){
		
		List<Integer> statichist = new ArrayList<Integer> ();
		for(int i=300;i<600;i++){
			statichist.add(i);
		}
		
		//int hislen = 300;
		
		List<histInformation> arr_his = new ArrayList<histInformation>();
		
		for (int curhis=300;curhis<600;curhis++){
			histInformation tmp = new histInformation(curhis);
			arr_his.add(tmp);
		}
		
		//for(int i=0;i<worklen;i++){
		
		for(int i=0;i<fhrHist.size();i++){
			for(int j=0;j<hislen;j++){
				int ln = statichist.get(j);
				int rn = fhrHist.get(i);
				//System.out.println("ln=" + ln + ",rn=" + rn);
				//if(statichist.get(j) == fhrHist.get(i)){
				if(ln == rn){
					//System.out.println("uuuuuu");
					//System.out.println("before chagne frequecny:"+ arr_his.get(j).frequency);
					arr_his.get(j).frequency = arr_his.get(j).frequency + 1;
					//System.out.println("after chagne frequecny:"+ arr_his.get(j).frequency);
					//System.out.println("histgram plus 1,j: "+j + "");										
				}
			}			
		}		
		return arr_his;
	}
	
	/*
	 * 按脉冲间隔值从低到高的顺序累加频数，返回累加数组
	 */
	public List<Integer> cumulant(List<histInformation> arrayhis){
		//int hislen = 300;
		List<Integer>  arrcumulant = new ArrayList<Integer>();
		arrcumulant.add(arrayhis.get(0).frequency);
		
		for(int i=0;i<hislen-1;i++){
			arrcumulant.add(arrcumulant.get(i) + arrayhis.get(i + 1).frequency);
		}
		
		return arrcumulant;
		
	}
	
}
