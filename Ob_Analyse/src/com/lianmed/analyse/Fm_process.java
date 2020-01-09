package com.lianmed.analyse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Fm_process {

	public static int get_data(int data) {
		int data1 = 0;
		if (data > 128) {
			data1 = data - 128;
		} else if(data <= 128 && data>0){
			data1 = 128 - data;
		}else{
			data1 = 0;
		}
		return data1;

	}

	public static ArrayList<Integer> getAFMDataFromMove(List<Integer> fmsrcdata) {

		// afmvalues

		int srcdatalen = fmsrcdata.size();

		//int meanlen = 80;
		int meanlen = 25;
		
		ArrayList<Integer> newfmdata = new ArrayList<Integer>();

		for (int ti = 0; ti < srcdatalen; ti += meanlen) {

			// if(ti)
			int start = ti;
			int end = ti + meanlen;
			end = end > fmsrcdata.size() ? fmsrcdata.size() : end;

			//List<Integer> e_afmdatas = new ArrayList<Integer>(fmsrcdata.subList(ti, ti + meanlen));
			List<Integer> e_afmdatas = new ArrayList<Integer>(fmsrcdata.subList(start, end));
			for (int tj = 0; tj < e_afmdatas.size(); tj++) {
				int tmpvalue = e_afmdatas.get(tj);
				e_afmdatas.set(tj, get_data(tmpvalue));
			}

			float maxvalue = Collections.max(e_afmdatas);
//			System.out.println(maxvalue + " ");
//			newfmdata.add(Math.round(means));
			newfmdata.add(Math.round(maxvalue));
		}
		return newfmdata;
	}

	public static List<Integer> getFMBaseline(List<Integer> oriSignals_src) {

		List<Integer> oriSignals = new ArrayList<Integer>(oriSignals_src);
		int baseline_time = 75; // # 1min,这里是不是应该根据胎动信号的强弱来更新
		ArrayList<Float> means = getMeans_hist(oriSignals);

		filterFM_byMeans(oriSignals, means, 2, baseline_time);
		filterFM(oriSignals, means);
		filterFM(oriSignals, means);
		filterFM(oriSignals, means);
		filterFM(oriSignals, means);

		return oriSignals;

	}

	public static ArrayList<Float> getMeans_hist(List<Integer> fmdatas) {

		ArrayList<Float> means_temp = new ArrayList<Float>();
		int baseline_time = 75;

		int datalen = fmdatas.size();

		//int baseline_counts = (int)Math.ceil((float)datalen / baseline_time);
		int baseline_counts = datalen / baseline_time;
		for (int i = 0; i < baseline_counts; i++) {
			int start = i * baseline_time;
			int end = (i + 1) * baseline_time;
			end = end>datalen?datalen:end;
			List<Integer> targetfmdata = new ArrayList<Integer>(fmdatas.subList(start, end));
			List<Integer> sortdata = new ArrayList<Integer>(fmdatas.subList(start, end));

			Collections.sort(sortdata);

			///System.out.println("baseline_time:"+baseline_time + " "+ sortdata.size());
			
			
			
			List<Integer> meandata = new ArrayList<Integer>(sortdata.subList(5, (baseline_time - 5)>sortdata.size()?sortdata.size():(baseline_time - 5)));
			
			
			float startmean = FhrAnalyse.getAvg(meandata);

			List<Integer> difarray = new ArrayList<Integer>();

			for (int ti = 0; ti < targetfmdata.size(); ti++) {
				float tmpvalue = targetfmdata.get(ti) - startmean;
				difarray.add(Math.round(tmpvalue));
			}

			int[] fre_counts_array = new int[5];

			ArrayList[] fre_indexs = new ArrayList[5];
			ArrayList[] fre_values = new ArrayList[5];

			for (int ti = 0; ti < 5; ti++) {
				fre_indexs[ti] = new ArrayList<Integer>();
				fre_values[ti] = new ArrayList<Integer>();
			}

			// int[][] fre_values = new int[5][2] ;

			int difindex = 0;

			for (int ti = 0; ti < difarray.size(); ti++) {
				int tmpdifvalue = difarray.get(ti);
				if (tmpdifvalue <= 1) {

					fre_counts_array[0] += 1;
					fre_indexs[0].add(difindex);
					fre_values[0].add(difarray.get(ti));
				}

				if (tmpdifvalue <= 2) {

					fre_counts_array[1] += 1;
					fre_indexs[1].add(difindex);
					fre_values[1].add(difarray.get(ti));

				}

				if (tmpdifvalue <= 3) {

					fre_counts_array[2] += 1;
					fre_indexs[2].add(difindex);
					fre_values[2].add(difarray.get(ti));

				}

				if (tmpdifvalue <= 4) {

					fre_counts_array[3] += 1;
					fre_indexs[3].add(difindex);
					fre_values[3].add(difarray.get(ti));

				}

				if (tmpdifvalue <= 5) {

					fre_counts_array[4] += 1;
					fre_indexs[4].add(difindex);
					fre_values[4].add(difarray.get(ti));

				}
				difindex += 1;
			}

			float newmean = startmean;

			for (int tj = 0; tj < 3; tj++) {
				if (((float) fre_counts_array[tj] / baseline_time) > 0.25) {
					ArrayList<Integer> newmeandata = new ArrayList<Integer>();
					for (int ti = 0; ti < baseline_time; ti++) {
						if (fre_indexs[tj].contains(ti)) {
							newmeandata.add(targetfmdata.get(ti));
							newmean = FhrAnalyse.getAvg(newmeandata);
							break;
						}
					}
				}
			}
			means_temp.add(newmean);
		}

		return means_temp;
	}

	public static void filterFM_byMeans(List<Integer> fmdata, List<Float> means, float threshold, int baseline_time) {

		int fmlen = fmdata.size();
		for (int ti = 0; ti < fmlen; ti++) {
			
			if((ti / baseline_time)>=means.size()){
				break;
			}
			if ((fmdata.get(ti) - means.get((ti / baseline_time)) > threshold)) {
				fmdata.set(ti, Math.round(means.get(ti / baseline_time)));
			}
		}
	}

	public static void filterFM(List<Integer> fmdatas, List<Float> means_temp) {

		float weight_f = 0.5f;
		float weight_a = 0.5f;
		int baseline_time = 75;// # 1min,这里是不是应该根据胎动信号的强弱来更新

		int datalen = fmdatas.size();

		for (int ti = 0; ti < datalen; ti++) {
			if (ti % baseline_time == 0) {
				
				if((ti / baseline_time)>=means_temp.size()){
					
					break;
				}
				
				int tmpvalue = Math.round(weight_f * fmdatas.get(ti) + weight_a * means_temp.get(ti / baseline_time));
				fmdatas.set(ti, tmpvalue);
				// fmdatas[ti] = weight_f * fmdatas[ti] + weight_a *
				// means_temp[int(ti/baseline_time)]
			} else {
				int tmpvalue = Math.round(weight_f * fmdatas.get(ti - 1) + weight_a * fmdatas.get(ti));
			}
		}

		for (int ti = datalen - 2; ti >= 0; ti--) {
			// fmdatas[ti] = weight_f * fmdatas[ti + 1] + weight_a * fmdatas[ti]
			int tmpvalue = Math.round(weight_f * fmdatas.get(ti + 1) + weight_a * fmdatas.get(ti));
			fmdatas.set(ti, tmpvalue);
		}

	}

}
