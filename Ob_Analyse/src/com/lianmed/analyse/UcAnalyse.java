package com.lianmed.analyse;

//import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import org.omg.CORBA.INTERNAL;
//import org.omg.CORBA.PUBLIC_MEMBER;

public class UcAnalyse {
	
	
	public final static int MAX_ANALY_LEN_src = 6000;
	public final static int MAX_ANALY_TIME = 80;
	
	//因为点数变了，这里可能有越界问题
	
	public static int[] detectUcBaseline(List<Integer> ucarray,AnalyseResult analyseResult,float fix_change ){
		int MAX_ANALY_LEN = (int)(MAX_ANALY_LEN_src * fix_change);
		int[] ucbaseline = new int[MAX_ANALY_LEN];
		int [] toco_baseline = new int [MAX_ANALY_LEN];
		int[] x_array = new int[MAX_ANALY_LEN];
		
		int[] ucsegbase = new int[MAX_ANALY_LEN/(int)(75* fix_change)];
		
		
		
		
		int samplesize = (int)((ucarray.size()/(75*fix_change))*(75*fix_change));
		int segment = (int)(75 * 10 * fix_change);
		
		int num_of_segment = segment;
		
		int start_of_seg = 0;
		int end_of_seg = num_of_segment;
		
		int j = num_of_segment / 2;//5min
		int x_array_i = 1;
		
		while(end_of_seg<=samplesize){
			
			int[] seg = new int[8];//#记录8个区间的次数
			int[] sumarr = new int[8];//记录8个区间的累加值
			
			//# 1 计算宫缩基线
	        //#1.1 10min为窗口，统计区间出现次数和累加值
			
			for(int ti=start_of_seg;ti<end_of_seg;ti++){
				
				if (0<ucarray.get(ti) && ucarray.get(ti) <20){
					seg[0]+=1;
				}
				else if(10<=ucarray.get(ti) && ucarray.get(ti) <30){
					
					seg[1]+=1;
			        sumarr[1] += ucarray.get(ti);			
				}
				
				else if (20<=ucarray.get(ti)  && ucarray.get(ti)<40){		
					seg[2]+=1;
					sumarr[2] += ucarray.get(ti);
					
				}
				
				else if (30<=ucarray.get(ti)  && ucarray.get(ti)<50){		
					seg[3]+=1;
					sumarr[3] += ucarray.get(ti);				
				}
				
				else if (40<=ucarray.get(ti)  && ucarray.get(ti)<60){		
					seg[4]+=1;
					sumarr[4] += ucarray.get(ti);				
				}
				
				else if (60<=ucarray.get(ti)  && ucarray.get(ti)<80){		
					seg[5]+=1;
					sumarr[5] += ucarray.get(ti);				
				}
				
				else if (80<=ucarray.get(ti)  && ucarray.get(ti)<100){		
					seg[6]+=1;
					sumarr[6] += ucarray.get(ti);				
				}
				
				else if ( ucarray.get(ti)>=100){		
					seg[7]+=1;
					sumarr[7] += ucarray.get(ti);				
				}											
			}
			
			 //#1.2 选出宫缩值次数最多的区间，把该区间的累加值的平均数作为位于时间窗口中央那1分钟基线值
		     int maxvalue = 0;
		     int position = 0;
		     for(int tm=0;tm<8;tm++){
		    	 
		    	 if(seg[tm]>maxvalue){
		    		 maxvalue = seg[tm];
		    	     position = tm;		    	    		    	     
		    	 }	    	 
		     }		     
		     if(maxvalue>0){		    	 
		    	 toco_baseline[j] = sumarr[position]/maxvalue;
		     }		     
			//1.3 下一分钟
		     start_of_seg += (int)(75 *fix_change);		     
		     end_of_seg += (int)(75 *fix_change);
		     
		     x_array[x_array_i] = j;
		     x_array_i+=1;		     
		     j = j+(int)(75*fix_change);		     									
		}
		
		toco_baseline[0] = toco_baseline[x_array[1]];
		
		int k=0;
		
		//1.4 To allocate the UC baseine to every minute
		for(int ti=0;ti<(num_of_segment/2);ti+=(int)(75*fix_change)){			
			 k = ti/(int)(75*fix_change);
			 ucsegbase[k] = toco_baseline[0];			
		}
		
		for(int ti =num_of_segment/2;ti< samplesize-num_of_segment/2; ti+=(int)(75*fix_change)){
			k = ti/(int)(75*fix_change);
	        ucsegbase[k] = toco_baseline[ti];
		}
		
		for (int ti=samplesize-num_of_segment/2; ti<samplesize; ti+=(int)(75*fix_change))
	    {
	        k= ti/(int)(75*fix_change);
	        ucsegbase[k] = toco_baseline[x_array[x_array_i-1]];
	    }

	    //To allocate the UC baseline value(every segment) to every sample
	    for ( int ti=0; ti<num_of_segment/2; ti++)
	    {
	        ucbaseline[ti] = toco_baseline[0];
	    }
	    
	    for ( int ti=num_of_segment/2; ti<samplesize-num_of_segment/2; ti+=(int)(75*fix_change))
	    {
	        for (j=0; j<(int)(75*fix_change); j++)
	        {
	            if ( (ti+j) < samplesize-num_of_segment/2)
	            {
	                ucbaseline[ti+j] = toco_baseline[ti];
	            }
	        }
	    }
	    for ( int ti=samplesize-num_of_segment/2; ti<samplesize; ti++)
	    {
	        ucbaseline[ti] = toco_baseline[x_array[x_array_i-1]];
	    }
	    
	    //To compute the averaged UC baseline which finally displayed on the
	    //Analysis Interface
	    int AvBaseline=0;
	    int count = 0;
	    int i=0;
	    for (i=0; i<samplesize; i++)
	    {
	        if( ucbaseline[i] > 0 )
	        {
	            AvBaseline += ucbaseline[i];
	            count++;
	        }
	    }
	    if (count > 0 )
	    {
	        analyseResult.uc_jx = AvBaseline/count;
	    }
	    else
	    {
	        analyseResult.uc_jx = 0;
	    }
	    return ucbaseline;
		
	}

	public static double Iir(double[] b, double[] a, double[] z, int ord, double x) {
		double tmp;

		tmp = b[0] * x + z[0];

		int i;
		for (i = 0; i < ord; ++i) {
			z[i] = b[i + 1] * x + z[i + 1] - a[i + 1] * tmp;
		}

		return tmp;

	}
	
	public static ArrayList<int[]> detectUc(ArrayList<Integer> ucarray,AnalyseResult analyseResult,float fix_change){
		
		double[] iirtemp = new double[5];
		
		double[] filtereduc = new double[ucarray.size()];
		double[] diffuc = new double[ucarray.size()];
		
		int i;
		int samplesize;
		float input;
		// double filtereduc[4500];
		// double diffuc[4500];
		
		double Num[] = { 0.01006529084343, -0.03700880705134, 0.05404236589874, -0.03700880705134, 0.01006529084343 };
		double Den[] = { 1, -3.83461006038, 5.539935869318, -3.573126533506, 0.8679750116024 };
		
		int[] ucbaseline = detectUcBaseline(ucarray, analyseResult,3.2f);
		samplesize = (ucarray.size()/75)*75;
		
		for (i = 0; i < samplesize; i++) {
			input = (float) ucarray.get(i);
			filtereduc[i] = Iir(Num, Den, iirtemp, 4, input);
		}
		
		 //To eliminate the delay by the filter  这里的20还有待修正
	    samplesize = samplesize-20;
	    for (i=0; i<samplesize; i++)
	    {
	        filtereduc[i] = filtereduc[i+20];
	    }
		
	  //To compute the difference of 8 points
	    int j,m;
	    //modified by lixiao 20050105
	    double sum;
	    for ( i=4; i<samplesize-4; i++)
	    {
	        sum = 0;
	        for (j=1; j<=4; j++)
	        {
	            if( (filtereduc[i+j] == 0) && (filtereduc[i-j] ==0 ) )
	            {
	                continue;
	            }
	            sum  += (filtereduc[i+j]-filtereduc[i-j])/(2*(double)j*0.8);
	        }
	        diffuc[i] = sum/4;
	    }
	    for ( i=0; i<4; i++)
	    {
	        diffuc[i] = 0;
	    }
	    for (i=samplesize-4; i<samplesize+20; i++)
	    {
	        diffuc[i] = 0;
	    }
	    samplesize = samplesize+20;
	    
	    int range;
	    int point_of_window;
	    int start_position;
	    int peak_interval;
	    int peak_position;
	    int min_duration;
	    int peak_position_lasttime;
	    // int min_value;   // Error Behard 20080805 与 diffuc[] 比较, diffuc[] 为 double
	    double min_value;
	    int min_position=0;
	    int minvalue ;
	    int minposition;
	    int max_value;
	    int max_position=0;
	    int max_interval=90;//This is the max time period to search two endpoints of a contraction
	    int point;
	    int peak_start;
	    int peak_end;
	    int duration;
	    int peak_count = 0;
	    
	    
	    int peak_position_array[] = new int[MAX_ANALY_TIME* 2];
	    int peak_start_array[]= new int[MAX_ANALY_TIME* 2];
	    int peak_end_array[] = new int[MAX_ANALY_TIME* 2];
	    
	    range=20;           //the least increased amplitude of the uterine contraction compared to the toco baseline
	    point_of_window=(int)(150 * fix_change);//2mins,  can be modified,but this is the best value to fix an interval of the uterine contraction
	    start_position=(int)(75 * fix_change);
	    peak_interval=(int)(60 * fix_change);   //(second) The minimum interval of two toco peaks, if the peak's interval is less than this, it is considered as one uterine contraction.
	    peak_position=0;    //mark the position of the peak detected at current time
	    min_duration=(int)(30 * fix_change);    //second, the minimum duration of a uterine contraction
	    peak_position_lasttime=0; //mark the position of the peak detected last time
	    
	    while ( (start_position+point_of_window) <= samplesize)
	    {
	        min_value = 100.0;
	        for ( i= start_position; i<start_position+point_of_window; i++)//找微分最小值
	        {
	            //if(i>=4500)ShowMessage("数组越界1:"+IntToStr(i));//maoguanli_find
	            if (diffuc[i] < min_value)
	            {
	                min_value = diffuc[i];
	                min_position = i;
	            }
	        }
	        j = min_position;
	        peak_position =  min_position;
	        // while(diffuc[j] <= 0)    // 向前搜索微分过零点
	        
	        // while(diffuc[j] <= 0)    // 向前搜索微分过零点
	        while((diffuc[j] <= 0) && (j >= start_position-75*fix_change))    // 向前搜索微分过零点
	        {
	            j--;
	            if (j<0)  {
	                break;
	            }
	            if (diffuc[j] > diffuc[peak_position])
	            {
	                peak_position = j;
	            }
	        }
	        
	        if(ucarray.get(peak_position) > ucbaseline[peak_position]+range)
	        {
	            max_value = 0;
	            for (i=peak_position-(int)(10*fix_change); i<=peak_position+10*fix_change; i++)
	            {
	                if (ucarray.get(i) > max_value)
	                {
	                    max_value = ucarray.get(i);
	                    max_position = i;
	                }
	            }
	            peak_position = max_position;
	            //To search the start point of UC
	            point = (int)(max_interval/0.8);
	            peak_start = peak_position;
	            //modified by lixiao 20050513
	            
	            while ((ucarray.get(peak_start) >= ucbaseline[peak_start]+(ucarray.get(peak_position)-ucbaseline[peak_position])/5)
	                    || (ucarray.get(peak_start)-ucbaseline[peak_start] <= 0) )
	            {
	                if (peak_start >=peak_position-point_of_window)
	                {
	                    peak_start--;
	                    if ( peak_start <= peak_position-point)
	                    {
	                        minvalue = 100;
	                        minposition = peak_position-point;
	                        for (i=peak_position-point;i<peak_position;i++)
	                        {
	                            if (ucarray.get(i) < minvalue)
	                            {
	                                minvalue = ucarray.get(i);
	                                minposition = i;
	                            }
	                        }
	                        peak_start =  minposition;
	                        break;
	                    }
	                }
	            }
	            
	            //To search the end point of UC
	            peak_end = peak_position;
	            //modified by lixiao 20050513
	            while ( (ucarray.get(peak_end) >= ucbaseline[peak_end]+(ucarray.get(peak_position)-ucbaseline[peak_position])/5)
	                    || (ucarray.get(peak_end)-ucbaseline[peak_end] <= 0) )
	            {

	                if (peak_end < peak_position+point_of_window )
	                {
	                    peak_end++;
	                    if ( peak_end >= peak_position+point)
	                    {
	                        minvalue = 100;
	                        minposition = peak_position+point;
	                        //maoguanli_overflow
	                        //for (i=peak_position;i<=peak_position+point;i++)
	                        int mgl_max=peak_position+point;
	                        if(mgl_max>samplesize)mgl_max=samplesize;
	                        for (i=peak_position;i<mgl_max;i++)
	                        {
	                            //if(i>=4500)ShowMessage("数组越界2:"+IntToStr(i));//maoguanli_find
	                            if (ucarray.get(i) < minvalue)
	                            {
	                                minvalue = ucarray.get(i);
	                                minposition = i;
	                            }
	                        }
	                        peak_end =  minposition;
	                        break;
	                    }
	                }
	            }
	            
	            duration = (int)((peak_end-peak_start)*0.8);
	            if (duration >= min_duration)
	            {
	                if ( (peak_position-peak_position_lasttime)*0.8>=peak_interval
	                     || peak_position_lasttime == 0)
	                {
	                    peak_position_array[peak_count] = peak_position;
	                    peak_start_array[peak_count] = peak_start;
	                    peak_end_array[peak_count] = peak_end;
	                    peak_position_lasttime = peak_position;
	                    peak_count++;
	                }
	                else//If two peaks are too close, it is considered as one contraction
	                {
	                    if ( ucarray.get(peak_position) > ucarray.get(peak_position_lasttime))
	                    {
	                        peak_count--;
	                        peak_position_array[peak_count] = peak_position;
	                        peak_start_array[peak_count] = peak_start;
	                        peak_end_array[peak_count] = peak_end;
	                        peak_position_lasttime = peak_position;
	                        peak_count++;
	                    }
	                }
	            }
	            
	            //to search the place where is the next peak-searching to start
	            j = min_position;
	            //maoguanli_bug diffuc的类型是FLOAT，当为0.064...等数据时，这是个死循环
	            while ( diffuc[j] <= 0 && j < samplesize )
	            {
	                j++;
	            }
	            if(j == min_position)
	            	j++;//maoguanli 解决死循环
	            start_position = j;
	        }
	        else
	        {
	            peak_position = 0;
	            start_position = start_position+point_of_window;
	        }
	    }
	    
	    int[] CArr = new int[(MAX_ANALY_TIME- 10/ 2)+ 1];	//宫缩次数
	    
	    int k;              //分段计算宫缩次数（从10分钟开始，每两分钟计算一次）
	    for (i=(int)(750 * fix_change); i<=samplesize; i+=150*fix_change)
	    {
	        k = (int)((i-750*fix_change)/(150*fix_change));
	        for (j=0; j<peak_count; j++)
	        {
	            m = peak_position_array[j];
	            if ( m>0 && m<i)
	            {
	                CArr[k]++;
	            }
	        }
	    }
	    //To compute the arguments related to the uc signal
	    int sumucstrong=0;
	    int sumnexttime=0;
	    int sumkeeptime=0;
	    
	    ArrayList<int[]> ucresult = new ArrayList<int[]>();
	    
	    for (i=0; i<peak_count; i++)//To remark the uc position
	    {
	        m = peak_position_array[i];
		//	printf("uc %d\n",m);
	        
//	        peak_end_array[i]-peak_start_array[i];
	        
	        ucresult.add(new int[]{m,peak_start_array[i],peak_end_array[i]});
	        
//	        sline[m].td |= UCHI;
	    }

	    analyseResult.ucresult = ucresult;
	    for (i=0; i<peak_count; i++)
	    {
	        m = peak_position_array[i];
	        sumucstrong += (ucarray.get(m)-ucbaseline[m]);
	    }

	    if (peak_count > 0)
	    {
	        analyseResult.ucstrong = sumucstrong/peak_count;
	        analyseResult.uctimes = peak_count;
	    }
	    else
	    {
	    	analyseResult.ucstrong = 0;
	    	analyseResult.uctimes = 0;
	    }
		
	    if ( peak_count<=1 )
	    {
	    	analyseResult.ucnexttime = 0;
	    }
	    else
	    {
	        for ( i=1; i<peak_count; i++)
	        {
	            sumnexttime += peak_position_array[i]-peak_position_array[i-1];
	        }
	        analyseResult.ucnexttime = (int)(sumnexttime/(peak_count-1)*0.8/60);
	    }


	    if(peak_count > 0)
	    {
	        for ( i=0; i<peak_count; i++)
	        {
	            sumkeeptime += peak_end_array[i]-peak_start_array[i];
	        }
	        analyseResult.uckeeptime = (int)(sumkeeptime/peak_count*0.8);
	    }
	    else
	    {
	    	analyseResult.uckeeptime = 0;
	    }
	    
	    return ucresult;
	}
	
	
	public static List<UcPeak> detectShortPeakCluster(List<Integer> ucarray){
		
		//原本uc是单个检测，为了适应实时检测，同样增加幅度和持续时间属性
		
		int fix_num = 3;
		
		List<UcPeak> ucpeaklist = new ArrayList<UcPeak>();
		
		int ucpeak_start = -1;
		int ucpeak_end = -1;
		
		List<Integer> filteruc = filterUC(ucarray,5 * fix_num);
		
		int peakthreshold = 6;
		int merge_range = 10 * fix_num;
		
		int datalen = ucarray.size();		
		int searchrange = 5 * fix_num;
		
		int ti = searchrange;
		
		int peakcounts = 0;
		int maxampl = 0;
		
		
		while(ti<datalen-searchrange){
			
			int ucdif = ucarray.get(ti) - filteruc.get(ti);	
			//可能为棘波
			if(ucdif>peakthreshold){				
				int min_f =  Collections.min(ucarray.subList(ti - searchrange, ti));
				int min_a = Collections.min(ucarray.subList(ti , ti + searchrange));
				//如果为真的棘波
				if((ucarray.get(ti) - min_f) > peakthreshold && (ucarray.get(ti) - min_a) > peakthreshold){		
					
					//如果不是第一个
					if(ucpeak_start >=0 && ucpeak_end >=0 ){
						int lagtime = ti - ucpeak_end;
						//符合合并条件，更新ucpeak_end 和 peakcounts,maxampl
						if(lagtime <=  merge_range){
							maxampl = ucdif>=maxampl?ucdif:maxampl;
							ucpeak_end = ti;
							peakcounts ++;
						}
						//不符合条件，上一个toco peak生效，同时标记新peak 起点
						else{
							UcPeak curpeak = new UcPeak();
							curpeak.amplitude = maxampl;
							curpeak.peaknums = peakcounts;
							curpeak.u_start = ucpeak_start;
							curpeak.u_end = ucpeak_end;
							
							ucpeaklist.add(curpeak);
							
							maxampl = ucdif;
							peakcounts = 1;
							ucpeak_start = ti;
							ucpeak_end = ti;
							
						}
					}
					//如果是第一个
					else{
						ucpeak_start = ti;
						ucpeak_end = ti;
						peakcounts ++;
						maxampl = ucdif;
					}
					ti = ti + 1;
				}
				//假棘波
				else{
					ti++;
				}
			}
			//不是棘波
			else{
				ti++;
			}		
		}		
		
		//处理最后一个peak,不会漏掉
		if(maxampl != 0){
			
			UcPeak curpeak = new UcPeak();
			curpeak.amplitude = maxampl;
			curpeak.peaknums = peakcounts;
			curpeak.u_start = ucpeak_start;
			curpeak.u_end = ucpeak_end;
			
			ucpeaklist.add(curpeak);
		}
		
		return ucpeaklist;						
	}
	
	public static List<int[]> detectShortPeak(List<Integer> ucarray,float fix_change){
		
		//单个检测
		
		List<int[]> peakarray = new ArrayList<int[]>();
		List<Integer> filteruc = filterUC(ucarray,(int)(5*fix_change));
		
		int peakthreshold = 6;
		
		int datalen = ucarray.size();
		
		int searchrange = (int)(5 * fix_change) ;
		
		for(int ti=searchrange;ti<datalen-searchrange;ti++){
			
			int ucdif = ucarray.get(ti) - filteruc.get(ti);			
			if(ucdif>peakthreshold){				
				int min_f =  Collections.min(ucarray.subList(ti - searchrange, ti));
				int min_a = Collections.min(ucarray.subList(ti , ti + searchrange));
				
				if((ucarray.get(ti) - min_f) > peakthreshold && (ucarray.get(ti) - min_a) > peakthreshold){
					peakarray.add(new int[]{ti,ucdif});
				}				
			}						
		}		
		return peakarray;						
	}
	
	public static List<Integer> filterUC(List<Integer> ucdata,int step){
		
		List<Integer> smoothuc = new ArrayList<Integer>(ucdata);
		
		int datalen = ucdata.size();
		
		for(int ti=step;ti<datalen;ti++){
			int start = ti-step;
			int end = (ti+step+1)<=datalen?(ti+step+1):datalen;
			float avgvalue = FhrAnalyse.getAvg(ucdata.subList(start,end));
			smoothuc.set(ti, Math.round(avgvalue));
		}				
		return smoothuc;				
	}
	
}
