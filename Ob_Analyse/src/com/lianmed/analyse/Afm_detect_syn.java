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
		//python sortĬ������
		Collections.sort(peakvalues);
		int peaklen = peakvalues.size();
		
		float difthreshold = FhrAnalyse.getAvg(peakvalues.subList(peaklen/2, peakvalues.size()));
				
		return difthreshold;		
		
	}
	/**
	 * tyl�������ۺϺ��̥��λ��(fhr, uc, Ӧ�û���һ��fmm)
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
		
		//#UCì��		
		List<int[]> ucdetect_result =  UcAnalyse.detectShortPeak(ucarray,fix_change);
		
		List<UcPeak> ucdetect_list = UcAnalyse.detectShortPeakCluster(ucarray);
		
		if(debugtag){
			for(int i=0;i<ucdetect_result.size();i++){
				System.out.println("ucpeak:"+ucdetect_result.get(i)[0]+" ");
			}
		}

		// tyl:���� move �ļ���fmm����
		List<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmarray); 
				
		//ArrayList<Float> means = Fm_process.getMeans_hist(newafmarray);
		// tyl:����fmm������fmm����
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
		
		int searchrange = (int)(25 * fix_change); //#������������Χ�ÿ�����û�вο�����
		
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
				//���ac and ucì��:
				if(inRange(ucdetect_result.get(tj)[0],cmprange_start,cmprange_end)){
					for(int k=0;k<fmobarray.size();k++){ //#����tocoarray
						if(inRange(fmobarray.get(k).fmstart,cmprange_start,cmprange_end)
							|| inRange(accstart,fmobarray.get(k).fmstart,fmobarray.get(k).fmend) 
								){
							//#���̥��λ���ڼ��ٵķ�Χ�� 
							if (acc_index>=(acc_nums-1)){
								List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
								fmobarray.clear();
								//TODO �����Ƿ���Բ���ȥ����fmobarray
								
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
								// �����Ƿ���Բ���ȥ����fmobarray
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
					//#λ��
					//�����С����
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
						//0 �����Ƿ���Բ���ȥ����fmobarray
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
						//TODO �����Ƿ���Բ���ȥ����fmobarray
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
					//С����
					if(fmtag && acc_result.get(ti)[3] == 0){
						
						fmposi = accstart;
						fmyes_array.add(fmposi);						
						fmtag = false;						
					}
					break;
				}								
			}
			
			//if(fmtag)
			//���������٣���ӵ�̥��List���,100%
			if(acc_result.get(ti)[3] == 1){		
				//fmyes_array.add(fmposi);	
				fmyes_array.add(accstart);
				//System.out.println("fmobarray size:" + fmobarray.size()+ " "+accstart);
				fmtag = false;
				
			}
			
			acc_index++;
		}
		
		int lastfm_pos = 0;
		
		//FM�����
		for(int tk=0;tk<fmobarray.size();tk++){
			
			FMData fm = fmobarray.get(tk);
			//fmposi = fm.maxdif_index;
			fmposi = fm.fmstart; //��Ӧʵʱ
			//#��֤̥���㲻�����ǣ���һ�����һ��������
			if(fmposi - lastfm_pos > (int)(75 * fix_change) || lastfm_pos == 0){
				//#�㹻���ʱ��
				
				//# ע�� in 20171127 û�й��������ͼ�������������� �Ƚϸ��ӣ��Ȳ������ж��ˣ�����Ч�������޸�
				if(fm.tempv > 1.2 * fm.basevalue && 6<(fm.fmend-fm.fmstart) && (fm.fmend-fm.fmstart)< (int)(150 * fix_change)
				&& fm.tempv>=globaldifthreshold){
					
					fmtag = true;
					for(int tm=0;tm<fmyes_array.size();tm++){
						
						int fmy = fmyes_array.get(tm);
						
						if(Math.abs(fmposi-fmy)<=(int)(38*fix_change)){ //�����
							fmtag = false;
							break;
						}						
					}					
					if (fmtag){					
						fmyes_array.add(fmposi);				
						lastfm_pos = fmposi;
					}					
				}				
				
				//һ��ǿ	�������TOCO����
				else if  ( fm.basevalue < fm.tempv && 3<(fm.fmend-fm.fmstart)){
				//else if  (1.2 * fm.basevalue < fm.tempv && fm.tempv < 1.5 * fm.basevalue){
				//if  (fm.means < fm.tempv && fm.tempv < 1.2 * fm.means){				
					//#��UC peak
					for(int tf=0;tf<ucdetect_result.size();tf++){
						//#uc peak��̥����Χ�� !!!
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
				
				//ǿ�Ƚ������������������
				else if((int)(3*fix_change)<(fm.fmend-fm.fmstart)){
					
					for(int tf=0;tf<ucdetect_result.size()-1;tf++){
						//#uc peak��̥����Χ�� !!!
						int[] ucdetect1 = ucdetect_result.get(tf);
						
						
							
						if ((Math.abs(fm.fmstart-ucdetect1[0])<(int)(5*fix_change) || Math.abs(ucdetect1[0]-fm.fmend)<(int)(5*fix_change)) && Math.abs(ucdetect1[0]-fmposi)<(int)(20*fix_change)){
							
							int[] ucdetect2 = ucdetect_result.get(tf+1);
							int[] ucdetect3 = null;
							//��� ����������������������
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
								System.out.println("ddd  fmposi ����:" + fmposi);
							}
							break;													
						}						
					}		
					
				}								
				//																		
			}			
		}
				
		//û��ACC,FM������д���ȵ�TOCO��������
		
		for(int tf=0;tf<ucdetect_result.size()-1;tf++){
			//#uc peak��̥����Χ�� !!!
			int[] ucdetect1 = ucdetect_result.get(tf);
			//TODO			
			//���TOCO��������10�����ֵ��Ҫȷ��
			if (ucdetect1[0]>10){
				
				int[] ucdetect2 = ucdetect_result.get(tf+1);
				int[] ucdetect3 = null;
				
				
				//��� ����������������������
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
				fmposi = ucdetect2[0];	//��Ӧʵʱ��Ҫ
				for(int tm=0;tm<fmyes_array.size();tm++){
					int fmy = fmyes_array.get(tm);
					if(Math.abs(fmposi - fmy)<=(int)(38*fix_change)){
						fmtag = false;
						break;
					}						
				}					
				if(fmtag){
					fmyes_array.add(fmposi);		
					System.out.println("ddd  fmposi ����:" + fmposi);
				}
				break;													
			}						
		}	
		
		
		
		
		
		Collections.sort(fmyes_array);
		analyseResult.fmresult = fmyes_array;
		return fmyes_array;
		
	}
	
	/**
	 * ����Toco������Ͳ��μ�����Ӧ�÷�
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
		//�����С����
		else if(ucele.peaknums >=3){			
			return 0.4f;			
		}
		//�����������
		else if(ucele.amplitude >8 && ucele.peaknums <3){							
			return 0.4f;
		}
		else{
			return 0.1f;
		}
	}
	/**
	 * ����Fmm������Ͳ��μ�����Ӧ�÷�
	 * @param afmele
	 * @param fix_change
	 * @param globaldifthreshold
	 * @return
	 */
	public float getFmmFmScore(FMData afmele, float fix_change, float globaldifthreshold){
		
		//���ȴ�ʱ���㹻
		if(afmele.tempv > 1.2 * afmele.basevalue && 6<(afmele.fmend-afmele.fmstart) && 
		  (afmele.fmend-afmele.fmstart)< (int)(150 * fix_change )&& afmele.tempv>=globaldifthreshold){
			return 0.8f;
		}
		
		//���ȴ�ʱ�䲻��
		else if(afmele.tempv > 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)<(int)(6 * fix_change)){
			return 0.4f;
		}
		//���Ȳ���ʱ�乻
		else if(afmele.tempv < 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)>(int)(6 * fix_change)){
			return 0.4f;
		}				
		return 0.1f;
	}
	
	//FHRһ������85%���ź�ֵ����֤̥���ź����������ж�
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
	 * tyl: FHR����ʵʱ���µĽ������, 0.25��һ���㣬һ������240���㣬��߼�����ǰ5���ӣ�Ҳ����ǰ1200���㣩ʱ��
	 * FHR���ߵĻ���ģ�����㲻׼ȷ�������FMM���ߺ�TOCO�����������Ƿ���̥����,��5���ӹ���ɼ������ֵ��ʱ��,���
	 * ��FHR����,FMM����,TOCO�����������Ͻ��м���
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
		
		// ��ʼ�ж�̥����ķ�Χ
		int startIndex = fhrarray.size() - realTimeCell - left_mistake_len; // 240��1����
		if(startIndex < 0)
			startIndex = 0;
		
		//����ì��
		List<UcPeak> ucdetect_list = UcAnalyse.detectShortPeakCluster(ucarray);
		
		System.out.println("����:" + ucdetect_list.size());
		for (UcPeak ucPeak : ucdetect_list) {
			System.out.print(ucPeak.u_start +" ");
		}

		
		// ����̥���ͼ
		List<Integer> fmbaseline = Fm_process.getFMBaseline(fmarray);

		// ����̥���ͼ��̥���ͼ����������ɵı�ǵ�
		AfmTool afmtool = new AfmTool();
		List<FMData> fmobarray = afmtool.getFM(fmarray, fmbaseline);
		
		// ����һ����ֵ����ֵ
		float globaldifthreshold = 0;
		if (fmobarray.size()==0){
			globaldifthreshold = 20;
		}else{
			globaldifthreshold = globalDifThreshold(fmobarray);
		}
		
		// �洢̥����Ǹ���
		ArrayList<Integer> fmyes_array = new ArrayList<Integer>();
		
		//����һ�������ֿ��ɵ�ʱ��ɢ���ҵķ�Χ���ֵ�ǿ��Խ��е�����
		int searchChange = (int)(12 * fix_change);
		
		//����̥������±�
		int fmposi = 0;
		
		List<int[]> acc_result = fhrAnalysetool.getAcc_RT(fhrarray,analyseResult,fix_change,
																fhrBaseLineArrayList, fmobarray,ucdetect_list);
		
		//System.out.println("ÿ��fm�ĳ���:" + fmobarray);	
		// ����������/С����, ������������fmyes_array
		haveTrueOrSmallAcc(acc_result, fhrarray, fix_change, searchChange, fmobarray,
				globaldifthreshold, ucdetect_list, accept_mistake_len, fmyes_array, fmposi,prevResult);
		
		// ��ֻ��fm�� toco
		haveOnlyFMAndToco(fmobarray,fhrarray, fmposi, fix_change,
				globaldifthreshold, ucdetect_list, fmyes_array, accept_mistake_len,prevResult);
			
		
		Collections.sort(fmyes_array);
	
		System.out.println("fmresult_array" + fmyes_array + " " + startIndex);
		// ȥ��������ʱ��(�Ѿ���ӡ�����˵ĵ�)����ĵ�������
		ArrayList<Integer> fmresult_array = new ArrayList<Integer>();
		for (Integer fm : fmyes_array) {
			
			if((fm >= startIndex))
			{
				if(prevResult != null && prevResult.size() != 0){ // ��ֹ�������̥�����ٴμ���
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
			fmposi = fm.fmstart; //��Ӧʵʱ
/*			if(!isStable(fhrarrayy, fmposi,fix_change)){
				System.out.println("not stable");
				continue;
			}*/
						
			//#��֤̥���㲻�����ǣ���һ�����һ��������
			if(fmposi - lastfm_pos > fmsearch || lastfm_pos == 0){										
				//���ԣ�ǿ�г���
				if(fm.tempv > 1.2 * fm.basevalue && 6<(fm.fmend-fm.fmstart) && (fm.fmend-fm.fmstart)< (int)( 150* fix_change) 
				&& fm.tempv>=globaldifthreshold){
					fmscore =  fmscore + fmm_large_long;
			
				}				
				
				//����һ�㣬ʱ�䳤
				else if  ( fm.basevalue < fm.tempv && (int)(3*fix_change)<(fm.fmend-fm.fmstart)){
		
					//#��UC peak
					fmscore = fmscore + fmm_mid_long;
				
				}				
				//ǿ�Ƚ�ǿ��ʱ���
				else if(1.2*fm.basevalue < fm.tempv && (int)(3* fix_change)>(fm.fmend-fm.fmstart)){				
					fmscore = fmscore + fmm_large_short;					
				}	
				
				for(int tu=0;tu<ucdetect_list.size();tu++){
					UcPeak ucele = ucdetect_list.get(tu);
					//�����TOCO����
					if(inRange(ucele.u_start,fm.fmstart-searchChange,fm.fmend+searchChange) || 
							inRange(ucele.u_end,fm.fmstart,fm.fmend)
							){
						//����Ƕ���������
						if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(5*fix_change)){
							fmscore = fmscore + toco_large_long;
							break;
						}
						//�����С����
						else if(ucele.peaknums >=3){
							fmscore = fmscore + toco_mid_long;
							break;							
						}
						//�����������
						else if(ucele.amplitude >8 && ucele.peaknums <3){							
							fmscore = fmscore + toco_large_short;
							break;
						}						
					}
				}	
				System.out.println("fmscore:" + fmscore + "����: " + fm.fmend);
				if(fmscore >= 1.0f){
					
					fmtag = true;	
					//��Ӧʵʱ
					fmposi = fm.fmend;
					System.out.println("��������:" + fmposi);
					if(fmposi < fhrarrayy.size()){
						
					//	System.out.println("��ǰ��fmposi: " + fmposi + "fhr���ȣ� " +fhrarrayy.size());
						
						// �ж������е�̥��������Ƿ��д��ھ������������ĵ㣬������붨��Ϊ38*fix_change
						for(int tm=0;tm<fmyes_array.size();tm++){
							int fmy = fmyes_array.get(tm);
							//TODO 38���ֵҲ����Ҫ��ȷ��һ��
							
							if(Math.abs(fmposi - fmy)<=(int)(50*fix_change) || fmposi > fhrarrayy.size()){
								fmtag = false;
								break;
							}						
						}
						
						if(fmtag){
							if((Math.abs(fmposi - fhrarrayy.size())<= accept_mistake_len) ){
								fmposi = fhrarrayy.size() - 1;
							}
							// ���̥����
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
		
		
		
		//����̥������±�
		for(int acc_index = 0; acc_index < acc_result.size(); acc_index++){

			int acctime = acc_result.get(acc_index)[2]; //���ٵ��ٽ�����
			
			if(!isStable(fhrarrayy, acctime,fix_change)){
				continue;
			}
			
			int accstart = acc_result.get(acc_index)[1];//���ٿ�ʼʱ��
			int accend = acc_result.get(acc_index)[2];//���ٽ���ʱ��
			System.out.print("accend" + accend);
			// ���ҷ�Χ
			int cmprange_start = accstart-searchChange; 
			int cmprange_end = accend+searchChange;
			
			float accfmscore =  0f;	
			
			accfmscore = accfmscore + getAccScore(acc_result, acc_index);
			System.out.print(" " + accfmscore + " ");
			//�ȿ�FMM

			float maxfmmscore = 0;
			for(int tf=0;tf<fmobarray.size();tf++){
				FMData afmele = fmobarray.get(tf);	
			
				//��FM
				if(inRange(afmele.maxdif_index,cmprange_start,cmprange_end) || inRange(accstart,afmele.fmstart,afmele.fmend)){
					
					float fmmFmScore = getFmmFmScore(afmele, fix_change, globaldifthreshold);	
					
					if(fmmFmScore >= maxfmmscore)
						maxfmmscore = fmmFmScore;

				}				
			}
			accfmscore = accfmscore + maxfmmscore;
			System.out.print(accfmscore + " ");
			float maxtocoscore = 0;
			
			//�ٿ�TOCO		
			for(int tu=0;tu<ucdetect_list.size();tu++){
				
				UcPeak ucele = ucdetect_list.get(tu);
				//�����TOCO����
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
			
			//����̥���궨
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
		
		//FHR���ٷ���
		float acc_large_long = 1.0f; // �����
		float acc_large_short = 0.8f; // ���ȴ�ʱ���
		float acc_mid_long = 0.5f; // ����һ�㣬ʱ�䳤
		float acc_mid_short = 0.3f; // ����һ�㣬ʱ���

		if( acc_result.get(ti)[3] == 0 ){
			//System.out.println("true acc��" + accstart);
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
	 * hmj ʦ�ְ汾
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
		// ��ü��ٽ��
		List<int[]> acc_result = fhrAnalysetool.getAcc(fhrarrayy,analyseResult,fix_change);
		
		boolean debugtag = false;
		
		if(debugtag){
			for(int i=0;i<acc_result.size();i++){
				if(acc_result.get(i)[3] == 0)
					System.out.println("acc:"+acc_result.get(i)[1]);
			}
			
		}
		
		//#UCì��		
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
		//tyl:������Χ����FHR���ٵ�λ������������ɢĳ��ָ��ֵ
		int searchrange = (int)(6 * fix_change); //#������������Χ�ÿ�����û�вο����� ,������10
		
		boolean fmtag = false;
		
		int acc_nums = acc_result.size();
		
		int acc_index = 0;
		
		int fmposi = 0;
		
		//FHR���ٷ���
		float trueacc = 1.0f;
		float sacc = 0.5f;
		//FMM�仯����score
		float fmm_large_long  = 0.8f;//��������ʱ�䳤
		float fmm_large_short = 0.3f;//��������ʱ���
		float fmm_mid_long = 0.3f;//���һ�����ʱ�䳤
		//TOCO�仯���ȷ�������
		float toco_large_long = 0.8f;//��������ʱ�䳤
		float toco_mid_long = 0.3f;//���һ�����ʱ�䳤
		float toco_large_short = 0.2f;//��������ʱ���
		
//		toco_large_long = 0f;
//		toco_mid_long = 0f;
//		toco_large_short = 0f;
		
		
		//����м���/С����
		for(int ti=0;ti<acc_result.size();ti++){
			//boolean 
			
			int acctime = acc_result.get(ti)[0];//��ü��ٳ���ʱ��
			
			if(!isStable(fhrarrayy, acctime,fix_change)){
				System.out.println("acctime:" + acctime + " not stable");
				continue;
			}
			
			int accstart = acc_result.get(ti)[1];//���ٿ�ʼʱ��
			int accend = acc_result.get(ti)[2];//���ٽ���ʱ��
			
			int cmprange_start = accstart-searchrange;
			int cmprange_end = accend+searchrange;
			
			float accfmscore =  0f;			
			//�����жϼ���������̥�������Ƿ���� 
			//1�������
			if( acc_result.get(ti)[3] == 1 ){
				//System.out.println("true acc��" + accstart);
				accfmscore = accfmscore + trueacc;
			}
			//2��С����
			else{				
				accfmscore = accfmscore + sacc;
				//�ȿ�FMM
				for(int tf=0;tf<fmobarray.size();tf++){
					FMData afmele = fmobarray.get(tf);	
					//��FM
					if(inRange(afmele.fmstart,cmprange_start,cmprange_end) || inRange(accstart,afmele.fmstart,afmele.fmend)){
						//���ȴ�ʱ���㹻
						if(afmele.tempv > 1.2 * afmele.basevalue && 6<(afmele.fmend-afmele.fmstart) && (afmele.fmend-afmele.fmstart)< (int)(150 * fix_change )                   
								&& afmele.tempv>=globaldifthreshold){
							accfmscore = accfmscore + fmm_large_long;
							break;
						}
						//���ȴ�ʱ�䲻��
						else if(afmele.tempv > 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)<(int)(6 * fix_change)){
							accfmscore = accfmscore + fmm_large_short;
							break;
						}
						//���Ȳ���ʱ�乻
						else if(afmele.tempv < 1.2 * afmele.basevalue && (afmele.fmend-afmele.fmstart)>(int)(6 * fix_change)){
							accfmscore = accfmscore + fmm_mid_long;
							break;
						}						
					}				
				}	
				
				//�ٿ�TOCO		
				for(int tu=0;tu<ucdetect_list.size();tu++){
					UcPeak ucele = ucdetect_list.get(tu);
					//�����TOCO����
					if(inRange(ucele.u_start,cmprange_start,cmprange_end) || 
							inRange(ucele.u_end,cmprange_start,cmprange_end)
							){

						accfmscore = accfmscore + getTocoFmScore(ucele,fix_change);
						break;
					}
				}	
				
			}
			
			//����̥���궨
			if(accfmscore >= 1.0f){
				//����ǽӽ�ʵʱβ��
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
			
			
			//�±��ǶԷ�Χ�ڵ�toco��Fm����ȥ��
			
			for(int tj=0;tj<ucdetect_list.size();tj++){	
				
				//boolean istureAcc = false;	
				//���acc and tocoì��:
				if(inRange(ucdetect_list.get(tj).u_start,cmprange_start,cmprange_end) || 
						inRange(ucdetect_list.get(tj).u_end,cmprange_start,cmprange_end)
						){
					
					//#����fmobarray
					for(int k=0;k<fmobarray.size();k++){ 
						if(inRange(fmobarray.get(k).fmstart,cmprange_start,cmprange_end)
							|| inRange(accstart,fmobarray.get(k).fmstart,fmobarray.get(k).fmend) 
								){
							//#���̥��λ���ڼ��ٵķ�Χ�� ���� ������
							if (acc_index>=(acc_nums-1)){
								List<FMData> newfmobarray = new ArrayList<FMData>(fmobarray);
								fmobarray.clear();
								//TODO �����Ƿ���Բ���ȥ����fmobarray
								
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
								//TODO �����Ƿ���Բ���ȥ����fmobarray
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
					//#λ��
					//�����С����
					
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
						//TODO �����Ƿ���Բ���ȥ����fmobarray
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
						//TODO �����Ƿ���Բ���ȥ����fmobarray
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
					//С����
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
			//���������٣���ӵ�̥��List���,100%
			if(acc_result.get(ti)[3] == 1){		
				fmyes_array.add(accstart);
				fmtag = false;
				
			}
			*/
			acc_index++;
		}
		
		int lastfm_pos = 0;
		
		//TODO �������ҲҪ��ȷ��һ��
		int fmsearch = (int)(75 * fix_change); 


		
		//FM�����
		for(int tk=0;tk<fmobarray.size();tk++){
			float fmscore = 0f;
			FMData fm = fmobarray.get(tk);
			//fmposi = fm.maxdif_index;
			fmposi = fm.fmstart; //��Ӧʵʱ
			if(!isStable(fhrarrayy, fmposi,fix_change)){
				System.out.println("not stable");
				continue;
			}
						
			//#��֤̥���㲻�����ǣ���һ�����һ��������
			if(fmposi - lastfm_pos > fmsearch || lastfm_pos == 0){										
				//���ԣ�ǿ�г���
				if(fm.tempv > 1.2 * fm.basevalue && 6<(fm.fmend-fm.fmstart) && (fm.fmend-fm.fmstart)< (int)( 150* fix_change) 
				&& fm.tempv>=globaldifthreshold){
					fmscore =  fmscore + fmm_large_long;
					//fmtag = true;
					
					/*
					for(int tm=0;tm<fmyes_array.size();tm++){	
						int fmy = fmyes_array.get(tm);
						if(Math.abs(fmposi-fmy)<=38){ //�����
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
				
				//����һ�㣬ʱ�䳤
				else if  ( fm.basevalue < fm.tempv && (int)(3*fix_change)<(fm.fmend-fm.fmstart)){
				//else if  (1.2 * fm.basevalue < fm.tempv && fm.tempv < 1.5 * fm.basevalue){
				//if  (fm.means < fm.tempv && fm.tempv < 1.2 * fm.means){				
					//#��UC peak
					fmscore = fmscore + fmm_mid_long;
					/*
					for(int tf=0;tf<ucdetect_list.size();tf++){
						//#uc peak��̥����Χ�� !!!
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
				//ǿ�Ƚ�ǿ��ʱ���
				else if(1.2*fm.basevalue < fm.tempv && (int)(3* fix_change)>(fm.fmend-fm.fmstart)){				
					fmscore = fmscore + fmm_large_short;					
				}	
				
				for(int tu=0;tu<ucdetect_list.size();tu++){
					UcPeak ucele = ucdetect_list.get(tu);
					//�����TOCO����
					if(inRange(ucele.u_start,fm.fmstart,fm.fmend) || 
							inRange(ucele.u_end,fm.fmstart,fm.fmend)
							){
						//����Ƕ���������
						if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(5*fix_change)){
							fmscore = fmscore + toco_large_long;
							break;
						}
						//�����С����
						else if(ucele.peaknums >=3){
							fmscore = fmscore + toco_mid_long;
							break;							
						}
						//�����������
						else if(ucele.amplitude >8 && ucele.peaknums <3){							
							fmscore = fmscore + toco_large_short;
							break;
						}						
					}
				}	
				
				if(fmscore >= 1.0f){
					fmtag = true;	
					//��Ӧʵʱ
					fmposi = fm.fmend;	
					
					for(int tm=0;tm<fmyes_array.size();tm++){
						int fmy = fmyes_array.get(tm);
						//TODO 38���ֵҲ����Ҫ��ȷ��һ��
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
						System.out.println("ddd  fmposi ����:" + fmposi);
					}
					
				}
				
				/*
					for(int tf=0;tf<ucdetect_result.size()-1;tf++){
						//#uc peak��̥����Χ�� !!!
						int[] ucdetect1 = ucdetect_result.get(tf);
						
						
							
						if ((Math.abs(fm.fmstart-ucdetect1[0])<5 || Math.abs(ucdetect1[0]-fm.fmend)<5) && Math.abs(ucdetect1[0]-fmposi)<20){
							
							int[] ucdetect2 = ucdetect_result.get(tf+1);
							int[] ucdetect3 = null;
							//��� ����������������������
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
								System.out.println("ddd  fmposi ����:" + fmposi);
							}
							break;													
						}						
					}		
					*/
												
				//																		
			}			
		}
				
		//û��ACC,FM������д���ȵ�TOCO��������
		
//		for(int tu=0;tu<ucdetect_list.size();tu++){
//			UcPeak ucele = ucdetect_list.get(tu);
//			if(!isStable(fhrarrayy, ucele.u_start,fix_change)){
//				System.out.println("not stable");
//				continue;
//			}
//			//�����TOCO����				
//			if(true){
//				//����Ƕ���������
//				if(ucele.amplitude >7 && ucele.peaknums >=3 && Math.abs(ucele.u_end-ucele.u_start)>(int)(5*fix_change)){
//					fmtag = true;	
//					//��Ӧʵʱ
//					fmposi = ucele.u_end;					
//					for(int tm=0;tm<fmyes_array.size();tm++){
//						int fmy = fmyes_array.get(tm);
//						//TODO 38���ֵҲ����Ҫ��ȷ��һ��
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
//						System.out.println("ddd  fmposi ����:" + fmposi);
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
