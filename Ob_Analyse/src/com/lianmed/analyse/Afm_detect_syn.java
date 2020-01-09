package com.lianmed.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.orsoncharts.graphics3d.World;

public class Afm_detect_syn {
	
	public FhrAnalyse fhrAnalysetool;
	
	public Afm_detect_syn(){
		fhrAnalysetool = new FhrAnalyse();	
	}
	
	public float globalDifThreshold(List<FMData> fmarray){
		
		List<Integer> peakvalues = new ArrayList<Integer>();
		
		for(int i=0;i<fmarray.size();i++){	
			peakvalues.add(fmarray.get(i).tempv);		
		}		
		//python sort默认升序
		Collections.sort(peakvalues);
		int peaklen = peakvalues.size();
		
		float difthreshold = FhrAnalyse.getAvg(peakvalues.subList(peaklen/2, peakvalues.size()));
				
		return difthreshold;		
		
	}
	/**
	 * tyl：计算综合后的胎动位置(fhr, uc, 应该还有一个fmm)
	 * @param fhrarrayy
	 * @param ucarray
	 * @param fmarray
	 * @param analyseResult
	 * @param fix_change
	 * @return
	 */
	public  List<Integer> calFMSysn_Fix(List<Integer> fhrarrayy,List<Integer> ucarray,ArrayList<Integer> fmarray,AnalyseResult analyseResult,float fix_change){				
		
		int fm_accept_deltalen = 20;//5s
		
		List<Integer> afmarray = new ArrayList<Integer>();
		List<Integer> returnresult = new ArrayList<Integer>();						
		List<int[]> acc_result = fhrAnalysetool.getAcc(fhrarrayy,analyseResult,fix_change);
		
		boolean debugtag = false;
		
		if(debugtag){
			for(int i=0;i<acc_result.size();i++){
				System.out.println("acc:"+acc_result.get(i)[1]);
			}
			
		}
		
		//#UC矛刺		
		List<int[]> ucdetect_result =  UcAnalyse.detectShortPeak(ucarray,fix_change);
		
		List<UcPeak> ucdetect_list = UcAnalyse.detectShortPeakCluster(ucarray);
		
		if(debugtag){
			for(int i=0;i<ucdetect_result.size();i++){
				System.out.println("ucpeak:"+ucdetect_result.get(i)[0]+" ");
			}
		}

		// tyl:根据 move 文件求fmm曲线
		List<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmarray); 
				
		//ArrayList<Float> means = Fm_process.getMeans_hist(newafmarray);
		// tyl:根据fmm曲线求fmm基线
		List<Integer> fmbaseline = Fm_process.getFMBaseline(newafmarray);
		
		AfmTool afmtool = new AfmTool();
		List<FMData> fmobarray = afmtool.getFM(newafmarray, fmbaseline);
		
		if(debugtag){
			for(int i=0;i<fmobarray.size();i++){
				System.out.println("fmbyfm:"+fmobarray.get(i).fmstart+" ");
			}
		}
		
		
		float globaldifthreshold = 0;
		if (fmobarray.size()==0){
			globaldifthreshold = 20;
		}else{
			globaldifthreshold = globalDifThreshold(fmobarray);
		}	
		
		ArrayList<Integer> fmyes_array = new ArrayList<Integer>();
		
		int searchrange = (int)(25 * fix_change); //#这两个搜索范围得看看有没有参考文献
		
		boolean fmtag = false;
		
		int acc_nums = acc_result.size();
		
		int acc_index = 0;
		
		int fmposi = 0;
		
		for(int ti=0;ti<acc_result.size();ti++){
			//boolean 
			int accstart = acc_result.get(ti)[1];
			int accend = acc_result.get(ti)[2];
			
			int cmprange_start = accstart-searchrange;
			int cmprange_end = accend+searchrange;
			
			
			for(int tj=0;tj<ucdetect_result.size();tj++){	
				//如果ac and uc矛刺:
				if(inRange(ucdetect_result.get(tj)[0],cmprange_start,cmprange_end)){
					for(int k=0;k<fmobarray.size();k++){ //#更新tocoarray
						if(inRange(fmobarray.get(k).fmstart,cmprange_start,cmprange_end)
							|| inRange(accstart,fmobarray.get(k).fmstart,fmobarray.get(k).fmend) 
								){
							//#如果胎动位置在加速的范围内 
							if (acc_index>=(acc_nums-1)){
								List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
								fmobarray.clear();
								//TODO 这里是否可以不用去清理fmobarray
								
								for(int m=0;m<newfmobarray.size();m++){
									if(		!									
												(	inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
													inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)	
													)
									){
										fmobarray.add(newfmobarray.get(m));
									}																	
								}															
							}							
							else{								
								List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
								fmobarray.clear();
								// 这里是否可以不用去清理fmobarray
								for(int m=0;m<newfmobarray.size();m++){
									if(		!									
												(	(inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
													inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)) &&
													newfmobarray.get(m).fmend<(acc_result.get(acc_index+1)[0]-searchrange)
													)
									){
										fmobarray.add(newfmobarray.get(m));
									}																	
								}																
							}
							
							break;
						}
						
					}
					
					fmtag = true;
					
					List<int[]> ucdetect_result_cp = new ArrayList<int[]>(ucdetect_result);	
					ucdetect_result.clear();
					for(int n=0;n<ucdetect_result_cp.size();n++){
						
						if(! 
								inRange(ucdetect_result_cp.get(n)[0],cmprange_start,cmprange_end)
								)
						{
							ucdetect_result.add(ucdetect_result_cp.get(n));
						}
					}
					//#位置
					//如果是小加速
					if(fmtag && acc_result.get(ti)[3] == 0){
						
						fmposi = accstart;
						fmyes_array.add(fmposi);						
						fmtag = false;						
					}
					break;
				}
			}
			
			
			for(int tj=0;tj<fmobarray.size();tj++ ){				
				FMData afmele = fmobarray.get(tj);				
				if( inRange(afmele.fmstart,cmprange_start,cmprange_end) || inRange(accstart,afmele.fmstart,afmele.fmend)){
					
					fmposi = accstart;
					fmtag = true;
					
					if(acc_index>=(acc_nums-1)){
						
						List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
						fmobarray.clear();
						//0 这里是否可以不用去清理fmobarray
						for(int m=0;m<newfmobarray.size();m++){
							if(		!									
										(	inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
											inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)	
											)
							){
								fmobarray.add(newfmobarray.get(m));
							}	
						}												
					}
					else{
						List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
						
						fmobarray.clear();
						//TODO 这里是否可以不用去清理fmobarray
						for(int m=0;m<newfmobarray.size();m++){
							if(		!									
										(	(inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
											inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)) &&
											newfmobarray.get(m).fmend<(acc_result.get(acc_index+1)[0]-searchrange)
											)
							){
								fmobarray.add(newfmobarray.get(m));
							}																	
						}	
					}	
					//小加速
					if(fmtag && acc_result.get(ti)[3] == 0){
						
						fmposi = accstart;
						fmyes_array.add(fmposi);						
						fmtag = false;						
					}
					break;
				}								
			}
			
			//if(fmtag)
			//如果是真加速，添加到胎动List里边,100%
			if(acc_result.get(ti)[3] == 1){		
				//fmyes_array.add(fmposi);	
				fmyes_array.add(accstart);
				//System.out.println("fmobarray size:" + fmobarray.size()+ " "+accstart);
				fmtag = false;
				
			}
			
			acc_index++;
		}
		
		int lastfm_pos = 0;
		
		//FM有情况
		for(int tk=0;tk<fmobarray.size();tk++){
			
			FMData fm = fmobarray.get(tk);
			//fmposi = fm.maxdif_index;
			fmposi = fm.fmstart; //适应实时
			//#保证胎动点不过多标记，与一个相隔一分钟以上
			if(fmposi - lastfm_pos > (int)(75 * fix_change) || lastfm_pos == 0){
				//#足够大的时候
				
				//# 注释 in 20171127 没有宫缩特征和加速特征的情况下 比较复杂，先不进行判断了，看看效果再作修改
				if(fm.tempv > 1.2 * fm.basevalue && 6<(fm.fmend-fm.fmstart) && (fm.fmend-fm.fmstart)< (int)(150 * fix_change)
				&& fm.tempv>=globaldifthreshold){
					
					fmtag = true;
					for(int tm=0;tm<fmyes_array.size();tm++){
						
						int fmy = fmyes_array.get(tm);
						
						if(Math.abs(fmposi-fmy)<=(int)(38*fix_change)){ //半分钟
							fmtag = false;
							break;
						}						
					}					
					if (fmtag){					
						fmyes_array.add(fmposi);				
						lastfm_pos = fmposi;
					}					
				}				
				
				//一般强	，如果有TOCO棘波
				else if  ( fm.basevalue < fm.tempv && 3<(fm.fmend-fm.fmstart)){
				//else if  (1.2 * fm.basevalue < fm.tempv && fm.tempv < 1.5 * fm.basevalue){
				//if  (fm.means < fm.tempv && fm.tempv < 1.2 * fm.means){				
					//#有UC peak
					for(int tf=0;tf<ucdetect_result.size();tf++){
						//#uc peak在胎动范围内 !!!
						int[] ucdetect = ucdetect_result.get(tf);
						if ((Math.abs(fm.fmstart-ucdetect[0])<(int)(5*fix_change) || Math.abs(ucdetect[0]-fm.fmend)<(int)(5*fix_change)) && Math.abs(ucdetect[0]-fmposi)<(int)(20*fix_change)){
							fmtag = true;						
							fmposi = ucdetect[0];							
							for(int tm=0;tm<fmyes_array.size();tm++){
								int fmy = fmyes_array.get(tm);
								if(Math.abs(fmposi - fmy)<=(int)(38*fix_change)){
									fmtag = false;
									break;
								}						
							}					
							if(fmtag){
								fmyes_array.add(fmposi);		
								System.out.println("ddd  fmposi:" + fmposi);
							}
							break;													
						}						
					}					
				}
				
				//强度较弱，如果有连续棘波
				else if((int)(3*fix_change)<(fm.fmend-fm.fmstart)){
					
					for(int tf=0;tf<ucdetect_result.size()-1;tf++){
						//#uc peak在胎动范围内 !!!
						int[] ucdetect1 = ucdetect_result.get(tf);
						
						
							
						if ((Math.abs(fm.fmstart-ucdetect1[0])<(int)(5*fix_change) || Math.abs(ucdetect1[0]-fm.fmend)<(int)(5*fix_change)) && Math.abs(ucdetect1[0]-fmposi)<(int)(20*fix_change)){
							
							int[] ucdetect2 = ucdetect_result.get(tf+1);
							int[] ucdetect3 = null;
							//如果 不满足连续棘波的条件，
							int x_dif_1 = Math.abs(ucdetect2[0] - ucdetect1[0]);
							int x_dif_2 = (int)(-30*fix_change);
							if(tf+2<ucdetect_result.size()){
								ucdetect3 = ucdetect_result.get(tf+2);
								x_dif_2 = Math.abs(ucdetect3[0]-ucdetect1[0]);
							}
							if(x_dif_1>(int)(25*fix_change) || (x_dif_1<(int)(2*fix_change) && x_dif_2>(int)(25*fix_change))){
								continue;
							}
							
							

							fmtag = true;						
							fmposi = ucdetect1[0];							
							for(int tm=0;tm<fmyes_array.size();tm++){
								int fmy = fmyes_array.get(tm);
								if(Math.abs(fmposi - fmy)<=(int)(38*fix_change)){
									fmtag = false;
									break;
								}						
							}					
							if(fmtag){
								fmyes_array.add(fmposi);		
								System.out.println("ddd  fmposi ！！:" + fmposi);
							}
							break;													
						}						
					}		
					
				}								
				//																		
			}			
		}
				
		//没有ACC,FM，如果有大幅度的TOCO连续棘波
		
		for(int tf=0;tf<ucdetect_result.size()-1;tf++){
			//#uc peak在胎动范围内 !!!
			int[] ucdetect1 = ucdetect_result.get(tf);
			//TODO			
			//如果TOCO棘波大于10，这个值需要确定
			if (ucdetect1[0]>10){
				
				int[] ucdetect2 = ucdetect_result.get(tf+1);
				int[] ucdetect3 = null;
				
				
				//如果 不满足连续棘波的条件，
				int x_dif_1 = Math.abs(ucdetect2[0] - ucdetect1[0]);
				int x_dif_2 = (int)(-30*fix_change);
				if(tf+2<ucdetect_result.size()){
					ucdetect3 = ucdetect_result.get(tf+2);
					x_dif_2 = Math.abs(ucdetect3[0]-ucdetect1[0]);
				}
				if(x_dif_1>(int)(25*fix_change) || (x_dif_1<(int)(2*fix_change) && x_dif_2>(int)(25*fix_change))){
					continue;
				}

				fmtag = true;						
				//fmposi = ucdetect1[0];	
				fmposi = ucdetect2[0];	//适应实时需要
				for(int tm=0;tm<fmyes_array.size();tm++){
					int fmy = fmyes_array.get(tm);
					if(Math.abs(fmposi - fmy)<=(int)(38*fix_change)){
						fmtag = false;
						break;
					}						
				}					
				if(fmtag){
					fmyes_array.add(fmposi);		
					System.out.println("ddd  fmposi ！！:" + fmposi);
				}
				break;													
			}						
		}	
		
		
		
		
		
		Collections.sort(fmyes_array);
		analyseResult.fmresult = fmyes_array;
		return fmyes_array;
		
	}
	
	/**
	 * 根据Toco的振幅和波形计算相应得分
	 * @param ucele
	 * @param fix_change
	 * @return
	 */
	public float getTocoFmScore(UcPeak ucele,float fix_change){	
		
		if(ucele.amplitude >7 && ucele.peaknums >3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(8*fix_change)){
			return 1.0f;
		}		
		else if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>=(int)(4*fix_change)){
			return 0.5f;
		}
		//多个，小幅度
		else if(ucele.peaknums >=3){			
			return 0.4f;			
		}
		//单个，大幅度
		else if(ucele.amplitude >8 && ucele.peaknums <3){							
			return 0.4f;
		}
		else{
			return 0.1f;
		}
	}
	/**
	 * 根据Fmm的振幅和波形计算相应得分
	 * @param afmele
	 * @param fix_change
	 * @param globaldifthreshold
	 * @return
	 */
	public float getFmmFmScore(FMData afmele, float fix_change, float globaldifthreshold){
		
		//幅度大，时间足够
		if(afmele.tempv > 1.2 * afmele.basevalue && 6<(afmele.fmend-afmele.fmstart) && 
		  (afmele.fmend-afmele.fmstart)< (int)(150 * fix_change )&& afmele.tempv>=globaldifthreshold){
			return 0.8f;
		}
		
		//幅度大，时间不够
		else if(afmele.tempv > 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)<(int)(6 * fix_change)){
			return 0.4f;
		}
		//幅度不大，时间够
		else if(afmele.tempv < 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)>(int)(6 * fix_change)){
			return 0.4f;
		}				
		return 0.1f;
	}
	
	//FHR一分钟有85%的信号值，保证胎心信号良好再做判断
	public boolean isStable(List<Integer> fhrlist,int pos,float fix_change){
		
		boolean result = false;	
		int deallength = (int)(37 *fix_change);
		
		int calstart = (pos-deallength)>=0?(pos-deallength):0;
		int calend = (pos+deallength)<=fhrlist.size()?(pos+deallength):(fhrlist.size());
		
		int normalcounts = 0;
		
		for(int i=calstart;i<calend;i++){
			if(isNormalFhr(fhrlist.get(i))){
				normalcounts ++;
			}
		}	
		float normalradio = (float)normalcounts/(calend - calstart);
		if(normalradio > 0.8f){
			result = true;
		}
		return result;		
	}
	
	public boolean isNormalFhr(int fhr){
		if(fhr>20 && fhr < 210){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * tyl: FHR曲线实时更新的结果计算, 0.25秒一个点，一分钟有240个点，这边假设在前5分钟（也就是前1200个点）时候
	 * FHR曲线的基线模糊计算不准确，则采用FMM曲线和TOCO曲线来评估是否是胎动点,当5分钟过后可计算基线值的时候,则采
	 * 用FHR曲线,FMM曲线,TOCO曲线三者相结合进行计算
	 *    
	 * @param fhrarray
	 * @param ucarray
	 * @param fmarray
	 * @param analyseResult
	 * @param fix_change
	 * @return
	 */
	public List<Integer> calFMSysn_RTbaseLine(List<Integer> fhrarray,List<Integer> ucarray,List<Integer> fmarray,
			AnalyseResult analyseResult,float fix_change, int realTimeCell, List<Integer> prevResult, ArrayList<Integer> fhrBaseLineArrayList){
				
		int accept_mistake_len = 32;//5s
		
		int left_mistake_len = 150;
		
		// 开始判断胎动点的范围
		int startIndex = fhrarray.size() - realTimeCell - left_mistake_len; // 240是1分钟
		if(startIndex < 0)
			startIndex = 0;
		
		//计算矛刺
		List<UcPeak> ucdetect_list = UcAnalyse.detectShortPeakCluster(ucarray);
		
		System.out.println("宫缩:" + ucdetect_list.size());
		for (UcPeak ucPeak : ucdetect_list) {
			System.out.print(ucPeak.u_start +" ");
		}

		
		// 计算胎儿活动图
		List<Integer> fmbaseline = Fm_process.getFMBaseline(fmarray);

		// 根据胎儿活动图和胎儿活动图基线求出可疑的标记点
		AfmTool afmtool = new AfmTool();
		List<FMData> fmobarray = afmtool.getFM(fmarray, fmbaseline);
		
		// 设置一个峰值的阈值
		float globaldifthreshold = 0;
		if (fmobarray.size()==0){
			globaldifthreshold = 20;
		}else{
			globaldifthreshold = globalDifThreshold(fmobarray);
		}
		
		// 存储胎动标记个数
		ArrayList<Integer> fmyes_array = new ArrayList<Integer>();
		
		//设置一个当发现可疑点时扩散查找的范围这个值是可以进行调整的
		int searchChange = (int)(12 * fix_change);
		
		//可疑胎动起点下标
		int fmposi = 0;
		
		List<int[]> acc_result = fhrAnalysetool.getAcc_RT(fhrarray,analyseResult,fix_change,
																fhrBaseLineArrayList, fmobarray,ucdetect_list);
		
		//System.out.println("每次fm的长度:" + fmobarray);	
		// 如果有真加速/小加速, 计算结果保存在fmyes_array
		haveTrueOrSmallAcc(acc_result, fhrarray, fix_change, searchChange, fmobarray,
				globaldifthreshold, ucdetect_list, accept_mistake_len, fmyes_array, fmposi,prevResult);
		
		// 当只用fm和 toco
		haveOnlyFMAndToco(fmobarray,fhrarray, fmposi, fix_change,
				globaldifthreshold, ucdetect_list, fmyes_array, accept_mistake_len,prevResult);
			
		
		Collections.sort(fmyes_array);
	
		System.out.println("fmresult_array" + fmyes_array + " " + startIndex);
		// 去除掉超过时间(已经打印出来了的点)而后的点计算出来
		ArrayList<Integer> fmresult_array = new ArrayList<Integer>();
		for (Integer fm : fmyes_array) {
			
			if((fm >= startIndex))
			{
				if(prevResult != null && prevResult.size() != 0){ // 防止计算过的胎动点再次计算
					if(fm - prevResult.get(prevResult.size() - 1) > 110)
						fmresult_array.add(fm);
					System.out.println("fm:" + fm);
						
				}
				else
				{
					fmresult_array.add(fm);
					System.out.println("fm:" + fm);
				}
			}
				
		}

		analyseResult.fmresult = fmresult_array;
		return fmresult_array;
	}
	
	private void haveOnlyFMAndToco(List<FMData> fmobarray, List<Integer> fhrarrayy, int fmposi, float fix_change,
			float globaldifthreshold, List<UcPeak> ucdetect_list, ArrayList<Integer> fmyes_array,
			int accept_mistake_len, List<Integer> preresult) {
		
		int lastfm_pos = 0; 
		int fmsearch = (int)(75 * fix_change);
		int searchChange = 10;
		boolean fmtag = false;
	
		float fmm_large_long = 0.8f;
		float fmm_large_short = 0.5f;
		float fmm_mid_long = 0.3f;
		
		float toco_large_long = 0.8f;
		float toco_large_short = 0.5f;
		float toco_mid_long = 0.3f;
		
		for(int tk=0;tk<fmobarray.size();tk++){
			
			float fmscore = 0f;
			FMData fm = fmobarray.get(tk);
			fmposi = fm.fmstart; //适应实时
/*			if(!isStable(fhrarrayy, fmposi,fix_change)){
				System.out.println("not stable");
				continue;
			}*/
						
			//#保证胎动点不过多标记，与一个相隔一分钟以上
			if(fmposi - lastfm_pos > fmsearch || lastfm_pos == 0){										
				//明显，强列持续
				if(fm.tempv > 1.2 * fm.basevalue && 6<(fm.fmend-fm.fmstart) && (fm.fmend-fm.fmstart)< (int)( 150* fix_change) 
				&& fm.tempv>=globaldifthreshold){
					fmscore =  fmscore + fmm_large_long;
			
				}				
				
				//幅度一般，时间长
				else if  ( fm.basevalue < fm.tempv && (int)(3*fix_change)<(fm.fmend-fm.fmstart)){
		
					//#有UC peak
					fmscore = fmscore + fmm_mid_long;
				
				}				
				//强度较强，时间短
				else if(1.2*fm.basevalue < fm.tempv && (int)(3* fix_change)>(fm.fmend-fm.fmstart)){				
					fmscore = fmscore + fmm_large_short;					
				}	
				
				for(int tu=0;tu<ucdetect_list.size();tu++){
					UcPeak ucele = ucdetect_list.get(tu);
					//如果有TOCO棘波
					if(inRange(ucele.u_start,fm.fmstart-searchChange,fm.fmend+searchChange) || 
							inRange(ucele.u_end,fm.fmstart,fm.fmend)
							){
						//如果是多个，大幅度
						if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(5*fix_change)){
							fmscore = fmscore + toco_large_long;
							break;
						}
						//多个，小幅度
						else if(ucele.peaknums >=3){
							fmscore = fmscore + toco_mid_long;
							break;							
						}
						//单个，大幅度
						else if(ucele.amplitude >8 && ucele.peaknums <3){							
							fmscore = fmscore + toco_large_short;
							break;
						}						
					}
				}	
				System.out.println("fmscore:" + fmscore + "坐标: " + fm.fmend);
				if(fmscore >= 1.0f){
					
					fmtag = true;	
					//适应实时
					fmposi = fm.fmend;
					System.out.println("来到这里:" + fmposi);
					if(fmposi < fhrarrayy.size()){
						
					//	System.out.println("当前的fmposi: " + fmposi + "fhr长度： " +fhrarrayy.size());
						
						// 判断在已有的胎动标记中是否有存在距离这个点过近的点，这个距离定义为38*fix_change
						for(int tm=0;tm<fmyes_array.size();tm++){
							int fmy = fmyes_array.get(tm);
							//TODO 38这个值也是需要再确定一下
							
							if(Math.abs(fmposi - fmy)<=(int)(50*fix_change) || fmposi > fhrarrayy.size()){
								fmtag = false;
								break;
							}						
						}
						
						if(fmtag){
							if((Math.abs(fmposi - fhrarrayy.size())<= accept_mistake_len) ){
								fmposi = fhrarrayy.size() - 1;
							}
							// 添加胎动点
							if(preresult != null && preresult.size() != 0)
							{
								if(fmposi > preresult.get(preresult.size() - 1))
								fmyes_array.add(fmposi);
							}
							else
								fmyes_array.add(fmposi);
							
							fmtag = false;
						
						}
					}
					
				}
																	
			}			
		}
		
	}

	private void haveTrueOrSmallAcc(List<int[]> acc_result, List<Integer> fhrarrayy, float fix_change, int searchChange,
			List<FMData> fmobarray, float globaldifthreshold, List<UcPeak> ucdetect_list, int accept_mistake_len,
			ArrayList<Integer> fmyes_array, int fmposi, List<Integer> preresult) {
		
		
		
		//可疑胎动起点下标
		for(int acc_index = 0; acc_index < acc_result.size(); acc_index++){

			int acctime = acc_result.get(acc_index)[2]; //加速的临界坐标
			
			if(!isStable(fhrarrayy, acctime,fix_change)){
				continue;
			}
			
			int accstart = acc_result.get(acc_index)[1];//加速开始时间
			int accend = acc_result.get(acc_index)[2];//加速结束时间
			System.out.print("accend" + accend);
			// 查找范围
			int cmprange_start = accstart-searchChange; 
			int cmprange_end = accend+searchChange;
			
			float accfmscore =  0f;	
			
			accfmscore = accfmscore + getAccScore(acc_result, acc_index);
			System.out.print(" " + accfmscore + " ");
			//先看FMM

			float maxfmmscore = 0;
			for(int tf=0;tf<fmobarray.size();tf++){
				FMData afmele = fmobarray.get(tf);	
			
				//有FM
				if(inRange(afmele.maxdif_index,cmprange_start,cmprange_end) || inRange(accstart,afmele.fmstart,afmele.fmend)){
					
					float fmmFmScore = getFmmFmScore(afmele, fix_change, globaldifthreshold);	
					
					if(fmmFmScore >= maxfmmscore)
						maxfmmscore = fmmFmScore;

				}				
			}
			accfmscore = accfmscore + maxfmmscore;
			System.out.print(accfmscore + " ");
			float maxtocoscore = 0;
			
			//再看TOCO		
			for(int tu=0;tu<ucdetect_list.size();tu++){
				
				UcPeak ucele = ucdetect_list.get(tu);
				//如果有TOCO棘波
				if(inRange(ucele.u_start,cmprange_start,cmprange_end) || 
						inRange(ucele.u_end,cmprange_start,cmprange_end)
						){
						
					float tocoFmScore = getTocoFmScore(ucele,fix_change);
					if(tocoFmScore >= maxtocoscore)
						maxtocoscore = tocoFmScore;
					break;
				}
			}	
			
			accfmscore = accfmscore + maxtocoscore;
			
			System.out.println(" " + accfmscore);
			
			//进行胎动标定
			if(accfmscore >= 1.0f){
				
				if(fmyes_array != null && fmyes_array.size() > 0)
				{
					if((accend - fmyes_array.get(fmyes_array.size() - 1) > 50))
					{
						fmyes_array.add(accend);

					}
					
				}
				else{
					
					fmyes_array.add(accend);
				}
				

			}else{
				continue;
			}
			
		}
		
		
	}

	private float getAccScore(List<int[]> acc_result, int ti) {
		
		//FHR加速分析
		float acc_large_long = 1.0f; // 真加速
		float acc_large_short = 0.8f; // 幅度大时间短
		float acc_mid_long = 0.5f; // 幅度一般，时间长
		float acc_mid_short = 0.3f; // 幅度一般，时间短

		if( acc_result.get(ti)[3] == 0 ){
			//System.out.println("true acc：" + accstart);
			return acc_large_long;
		}
		else if(acc_result.get(ti)[3] == 1){
			return acc_large_short;
		}
		else if(acc_result.get(ti)[3] == 2){
			return acc_mid_long;
		}else {
			return acc_mid_short;
		}

	}

	/**
	 * hmj 师兄版本
	 * @param fhrarrayy
	 * @param ucarray
	 * @param fmarray
	 * @param analyseResult
	 * @param fix_change
	 * @return
	 */
	public  List<Integer> calFMSysn_Fix_RT(List<Integer> fhrarrayy,List<Integer> ucarray,List<Integer> fmarray,AnalyseResult analyseResult,float fix_change){				

		int accpet_wucha_len = 32;//5s
		List<Integer> afmarray = new ArrayList<Integer>();
		List<Integer> returnresult = new ArrayList<Integer>();	
		// 获得加速结果
		List<int[]> acc_result = fhrAnalysetool.getAcc(fhrarrayy,analyseResult,fix_change);
		
		boolean debugtag = false;
		
		if(debugtag){
			for(int i=0;i<acc_result.size();i++){
				if(acc_result.get(i)[3] == 0)
					System.out.println("acc:"+acc_result.get(i)[1]);
			}
			
		}
		
		//#UC矛刺		
		//List<int[]> ucdetect_result =  UcAnalyse.detectShortPeak(ucarray);
		
		List<UcPeak> ucdetect_list = UcAnalyse.detectShortPeakCluster(ucarray);
		/*
		if(debugtag){
			for(int i=0;i<ucdetect_result.size();i++){
				System.out.println("ucpeak:"+ucdetect_result.get(i)[0]+" ");
			}
		}
		*/
		//List<int[]> 
		/*
		for(int ti=0;ti<ucdetect_result.size();ti++){
			
			
		}
		*/	
		List<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmarray);
				
		//ArrayList<Float> means = Fm_process.getMeans_hist(newafmarray);
		List<Integer> fmbaseline = Fm_process.getFMBaseline(newafmarray);
		
		AfmTool afmtool = new AfmTool();
		List<FMData> fmobarray = afmtool.getFM(newafmarray, fmbaseline);
		
		if(debugtag){
			for(int i=0;i<fmobarray.size();i++){
				System.out.println("fmbyfm:"+fmobarray.get(i).fmstart+" ");
			}
		}
		
		
		float globaldifthreshold = 0;
		if (fmobarray.size()==0){
			globaldifthreshold = 20;
		}else{
			globaldifthreshold = globalDifThreshold(fmobarray);
		}	
		
		ArrayList<Integer> fmyes_array = new ArrayList<Integer>();
		//tyl:搜索范围是在FHR加速的位置向其左右扩散某个指定值
		int searchrange = (int)(6 * fix_change); //#这两个搜索范围得看看有没有参考文献 ,本来是10
		
		boolean fmtag = false;
		
		int acc_nums = acc_result.size();
		
		int acc_index = 0;
		
		int fmposi = 0;
		
		//FHR加速分析
		float trueacc = 1.0f;
		float sacc = 0.5f;
		//FMM变化幅度score
		float fmm_large_long  = 0.8f;//振幅大持续时间长
		float fmm_large_short = 0.3f;//振幅大持续时间短
		float fmm_mid_long = 0.3f;//振幅一般持续时间长
		//TOCO变化幅度分数分配
		float toco_large_long = 0.8f;//振幅大持续时间长
		float toco_mid_long = 0.3f;//振幅一般持续时间长
		float toco_large_short = 0.2f;//振幅大持续时间短
		
//		toco_large_long = 0f;
//		toco_mid_long = 0f;
//		toco_large_short = 0f;
		
		
		//如果有加速/小加速
		for(int ti=0;ti<acc_result.size();ti++){
			//boolean 
			
			int acctime = acc_result.get(ti)[0];//获得加速持续时间
			
			if(!isStable(fhrarrayy, acctime,fix_change)){
				System.out.println("acctime:" + acctime + " not stable");
				continue;
			}
			
			int accstart = acc_result.get(ti)[1];//加速开始时间
			int accend = acc_result.get(ti)[2];//加速结束时间
			
			int cmprange_start = accstart-searchrange;
			int cmprange_end = accend+searchrange;
			
			float accfmscore =  0f;			
			//这里判断加速区间内胎动条件是否成立 
			//1、真加速
			if( acc_result.get(ti)[3] == 1 ){
				//System.out.println("true acc：" + accstart);
				accfmscore = accfmscore + trueacc;
			}
			//2、小加速
			else{				
				accfmscore = accfmscore + sacc;
				//先看FMM
				for(int tf=0;tf<fmobarray.size();tf++){
					FMData afmele = fmobarray.get(tf);	
					//有FM
					if(inRange(afmele.fmstart,cmprange_start,cmprange_end) || inRange(accstart,afmele.fmstart,afmele.fmend)){
						//幅度大，时间足够
						if(afmele.tempv > 1.2 * afmele.basevalue && 6<(afmele.fmend-afmele.fmstart) && (afmele.fmend-afmele.fmstart)< (int)(150 * fix_change )                   
								&& afmele.tempv>=globaldifthreshold){
							accfmscore = accfmscore + fmm_large_long;
							break;
						}
						//幅度大，时间不够
						else if(afmele.tempv > 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)<(int)(6 * fix_change)){
							accfmscore = accfmscore + fmm_large_short;
							break;
						}
						//幅度不大，时间够
						else if(afmele.tempv < 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)>(int)(6 * fix_change)){
							accfmscore = accfmscore + fmm_mid_long;
							break;
						}						
					}				
				}	
				
				//再看TOCO		
				for(int tu=0;tu<ucdetect_list.size();tu++){
					UcPeak ucele = ucdetect_list.get(tu);
					//如果有TOCO棘波
					if(inRange(ucele.u_start,cmprange_start,cmprange_end) || 
							inRange(ucele.u_end,cmprange_start,cmprange_end)
							){

						accfmscore = accfmscore + getTocoFmScore(ucele,fix_change);
						break;
					}
				}	
				
			}
			
			//进行胎动标定
			if(accfmscore >= 1.0f){
				//如果是接近实时尾部
//				if( Math.abs(acctime - fhrarrayy.size())<3){
//					fmyes_array.add(acctime);
//				}
				if(Math.abs(accend - fhrarrayy.size())<accpet_wucha_len){
					
					fmyes_array.add(fhrarrayy.size()-1);
				}
				else if(!isStable(fhrarrayy, accstart,fix_change)){
					fmyes_array.add(acctime);
				}	
				else{
					fmyes_array.add(accstart);
				}							
			}else{
				acc_index ++;
				continue;
			}
			
			
			//下边是对范围内的toco和Fm进行去重
			
			for(int tj=0;tj<ucdetect_list.size();tj++){	
				
				//boolean istureAcc = false;	
				//如果acc and toco矛刺:
				if(inRange(ucdetect_list.get(tj).u_start,cmprange_start,cmprange_end) || 
						inRange(ucdetect_list.get(tj).u_end,cmprange_start,cmprange_end)
						){
					
					//#更新fmobarray
					for(int k=0;k<fmobarray.size();k++){ 
						if(inRange(fmobarray.get(k).fmstart,cmprange_start,cmprange_end)
							|| inRange(accstart,fmobarray.get(k).fmstart,fmobarray.get(k).fmend) 
								){
							//#如果胎动位置在加速的范围内 或者 加速与
							if (acc_index>=(acc_nums-1)){
								List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
								fmobarray.clear();
								//TODO 这里是否可以不用去清理fmobarray
								
								for(int m=0;m<newfmobarray.size();m++){
									if(		!									
												(	inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
													inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)	
													)
									){
										fmobarray.add(newfmobarray.get(m));
									}																	
								}															
							}							
							else{								
								List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
								fmobarray.clear();
								//TODO 这里是否可以不用去清理fmobarray
								for(int m=0;m<newfmobarray.size();m++){
									if(		!									
												(	(inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
													inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)) &&
													newfmobarray.get(m).fmend<(acc_result.get(acc_index+1)[0]-searchrange)
													)
									){
										fmobarray.add(newfmobarray.get(m));
									}																	
								}																
							}
							
							break;
						}
						
					}
					
					fmtag = true;
					
					List<UcPeak> ucdetect_result_cp = new ArrayList<UcPeak>(ucdetect_list);	
					ucdetect_list.clear();
					for(int n=0;n<ucdetect_result_cp.size();n++){
						
						if(! 
								inRange(ucdetect_result_cp.get(n).u_start,cmprange_start,cmprange_end) || 
								inRange(ucdetect_result_cp.get(n).u_end,cmprange_start,cmprange_end)
								)
						{
							ucdetect_list.add(ucdetect_result_cp.get(n));
						}
					}
					//#位置
					//如果是小加速
					
					/*
					if(fmtag && acc_result.get(ti)[3] == 0){
						
						fmposi = accstart;
						fmyes_array.add(fmposi);						
						fmtag = false;						
					}
					*/
					break;
				}
			}
			
			
			for(int tj=0;tj<fmobarray.size();tj++ ){				
				FMData afmele = fmobarray.get(tj);				
				if( inRange(afmele.fmstart,cmprange_start,cmprange_end) || inRange(accstart,afmele.fmstart,afmele.fmend)){
					fmposi = accstart;
					fmtag = true;
					if(acc_index>=(acc_nums-1)){						
						List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
						fmobarray.clear();
						//TODO 这里是否可以不用去清理fmobarray
						for(int m=0;m<newfmobarray.size();m++){
							if(		!									
										(	inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
											inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)	
											)
							){
								fmobarray.add(newfmobarray.get(m));
							}	
						}												
					}
					else{
						List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);						
						fmobarray.clear();
						//TODO 这里是否可以不用去清理fmobarray
						for(int m=0;m<newfmobarray.size();m++){
							if(		!									
										(	(inRange(newfmobarray.get(m).fmstart,cmprange_start,cmprange_end) || 
											inRange(accstart,newfmobarray.get(m).fmstart-searchrange,newfmobarray.get(m).fmend+searchrange)) &&
											newfmobarray.get(m).fmend<(acc_result.get(acc_index+1)[0]-searchrange)
											)
							){
								fmobarray.add(newfmobarray.get(m));
							}																	
						}	
					}	
					/*
					//小加速
					if(fmtag && acc_result.get(ti)[3] == 0){
						
						fmposi = accstart;
						fmyes_array.add(fmposi);						
						fmtag = false;						
					}
					*/
					break;				
				}								
			}
			
			/*
			//如果是真加速，添加到胎动List里边,100%
			if(acc_result.get(ti)[3] == 1){		
				fmyes_array.add(accstart);
				fmtag = false;
				
			}
			*/
			acc_index++;
		}
		
		int lastfm_pos = 0;
		
		//TODO 这个距离也要再确认一下
		int fmsearch = (int)(75 * fix_change); 


		
		//FM有情况
		for(int tk=0;tk<fmobarray.size();tk++){
			float fmscore = 0f;
			FMData fm = fmobarray.get(tk);
			//fmposi = fm.maxdif_index;
			fmposi = fm.fmstart; //适应实时
			if(!isStable(fhrarrayy, fmposi,fix_change)){
				System.out.println("not stable");
				continue;
			}
						
			//#保证胎动点不过多标记，与一个相隔一分钟以上
			if(fmposi - lastfm_pos > fmsearch || lastfm_pos == 0){										
				//明显，强列持续
				if(fm.tempv > 1.2 * fm.basevalue && 6<(fm.fmend-fm.fmstart) && (fm.fmend-fm.fmstart)< (int)( 150* fix_change) 
				&& fm.tempv>=globaldifthreshold){
					fmscore =  fmscore + fmm_large_long;
					//fmtag = true;
					
					/*
					for(int tm=0;tm<fmyes_array.size();tm++){	
						int fmy = fmyes_array.get(tm);
						if(Math.abs(fmposi-fmy)<=38){ //半分钟
							fmtag = false;
							break;
						}						
					}	
					*/
					//TODO
					/*
					if (fmtag){					
						fmyes_array.add(fmposi);				
						lastfm_pos = fmposi;
					}	
					*/				
				}				
				
				//幅度一般，时间长
				else if  ( fm.basevalue < fm.tempv && (int)(3*fix_change)<(fm.fmend-fm.fmstart)){
				//else if  (1.2 * fm.basevalue < fm.tempv && fm.tempv < 1.5 * fm.basevalue){
				//if  (fm.means < fm.tempv && fm.tempv < 1.2 * fm.means){				
					//#有UC peak
					fmscore = fmscore + fmm_mid_long;
					/*
					for(int tf=0;tf<ucdetect_list.size();tf++){
						//#uc peak在胎动范围内 !!!
						UcPeak ucdetect = ucdetect_list.get(tf);
						if ((Math.abs(fm.fmstart-ucdetect[0])<5 || Math.abs(ucdetect[0]-fm.fmend)<5) && Math.abs(ucdetect[0]-fmposi)<20){
							fmtag = true;						
							fmposi = ucdetect[0];							
							for(int tm=0;tm<fmyes_array.size();tm++){
								int fmy = fmyes_array.get(tm);
								if(Math.abs(fmposi - fmy)<=38){
									fmtag = false;
									break;
								}						
							}					
							if(fmtag){
								fmyes_array.add(fmposi);		
								System.out.println("ddd  fmposi:" + fmposi);
							}
							break;													
						}						
					}
					*/					
				}				
				//强度较强，时间短
				else if(1.2*fm.basevalue < fm.tempv && (int)(3* fix_change)>(fm.fmend-fm.fmstart)){				
					fmscore = fmscore + fmm_large_short;					
				}	
				
				for(int tu=0;tu<ucdetect_list.size();tu++){
					UcPeak ucele = ucdetect_list.get(tu);
					//如果有TOCO棘波
					if(inRange(ucele.u_start,fm.fmstart,fm.fmend) || 
							inRange(ucele.u_end,fm.fmstart,fm.fmend)
							){
						//如果是多个，大幅度
						if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(5*fix_change)){
							fmscore = fmscore + toco_large_long;
							break;
						}
						//多个，小幅度
						else if(ucele.peaknums >=3){
							fmscore = fmscore + toco_mid_long;
							break;							
						}
						//单个，大幅度
						else if(ucele.amplitude >8 && ucele.peaknums <3){							
							fmscore = fmscore + toco_large_short;
							break;
						}						
					}
				}	
				
				if(fmscore >= 1.0f){
					fmtag = true;	
					//适应实时
					fmposi = fm.fmend;	
					
					for(int tm=0;tm<fmyes_array.size();tm++){
						int fmy = fmyes_array.get(tm);
						//TODO 38这个值也是需要再确定一下
						if(Math.abs(fmposi - fmy)<=(int)(38*fix_change)){
							fmtag = false;
							break;
						}						
					}
					
					if(fmtag){
						if(Math.abs(fmposi - fhrarrayy.size())<=accpet_wucha_len){
							fmposi = fhrarrayy.size() - 1;
						}
						fmyes_array.add(fmposi);
						fmtag = false;
						System.out.println("ddd  fmposi ！！:" + fmposi);
					}
					
				}
				
				/*
					for(int tf=0;tf<ucdetect_result.size()-1;tf++){
						//#uc peak在胎动范围内 !!!
						int[] ucdetect1 = ucdetect_result.get(tf);
						
						
							
						if ((Math.abs(fm.fmstart-ucdetect1[0])<5 || Math.abs(ucdetect1[0]-fm.fmend)<5) && Math.abs(ucdetect1[0]-fmposi)<20){
							
							int[] ucdetect2 = ucdetect_result.get(tf+1);
							int[] ucdetect3 = null;
							//如果 不满足连续棘波的条件，
							int x_dif_1 = Math.abs(ucdetect2[0] - ucdetect1[0]);
							int x_dif_2 = -30;
							if(tf+2<ucdetect_result.size()){
								ucdetect3 = ucdetect_result.get(tf+2);
								x_dif_2 = Math.abs(ucdetect3[0]-ucdetect1[0]);
							}
							if(x_dif_1>25 || (x_dif_1<2 && x_dif_2>25)){
								continue;
							}
							
							

							fmtag = true;						
							fmposi = ucdetect1[0];							
							for(int tm=0;tm<fmyes_array.size();tm++){
								int fmy = fmyes_array.get(tm);
								if(Math.abs(fmposi - fmy)<=38){
									fmtag = false;
									break;
								}						
							}					
							if(fmtag){
								fmyes_array.add(fmposi);		
								System.out.println("ddd  fmposi ！！:" + fmposi);
							}
							break;													
						}						
					}		
					*/
												
				//																		
			}			
		}
				
		//没有ACC,FM，如果有大幅度的TOCO连续棘波
		
//		for(int tu=0;tu<ucdetect_list.size();tu++){
//			UcPeak ucele = ucdetect_list.get(tu);
//			if(!isStable(fhrarrayy, ucele.u_start,fix_change)){
//				System.out.println("not stable");
//				continue;
//			}
//			//如果有TOCO棘波				
//			if(true){
//				//如果是多个，大幅度
//				if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(5*fix_change)){
//					fmtag = true;	
//					//适应实时
//					fmposi = ucele.u_end;					
//					for(int tm=0;tm<fmyes_array.size();tm++){
//						int fmy = fmyes_array.get(tm);
//						//TODO 38这个值也是需要再确定一下
//						if(Math.abs(fmposi - fmy)<=(int)(38*fix_change)){
//							fmtag = false;
//							break;
//						}						
//					}
//					if(fmtag){
//						if(Math.abs(fmposi - fhrarrayy.size())<=accpet_wucha_len){
//							fmposi = fhrarrayy.size() - 1;
//						}
//						fmyes_array.add(fmposi);	
//						fmtag = false;
//						System.out.println("ddd  fmposi ！！:" + fmposi);
//					}
//				}					
//			}
//		}		
		
		Collections.sort(fmyes_array);
		analyseResult.fmresult = fmyes_array;
		return fmyes_array;
		
	}
	public boolean inRange(int target,int start,int end){
		boolean result = false;
		if(target>=start && target<=end){
			result = true;
		}
		return result;
	}
	

}
