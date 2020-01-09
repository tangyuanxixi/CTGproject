import java.util.ArrayList;

import com.lianmed.analyse.AnalyseResult;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.filetool.FileData;

public class CountAcc {

	public static void main(String[] args) {
		
		
    	
    	String movefilestr = "D:/lianProject/new_data/16/190110100053/190110100053_0.move";
		
		String fetalfilestr = "D:/lianProject/new_data/16/190110100053/190110100053.fetal";
		
		int [] testfmlist = FileData.readFmpBasedataToArrayfromFile(movefilestr);
		
		int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr);
		for(int ti=1500;ti<1750;ti++){
			testfhrlist[ti] = (int)(testfhrlist[ti]-0.4*(ti-1500));
		}
		for(int ti=1750;ti<2000;ti++){
			testfhrlist[ti] = (int)(testfhrlist[ti]-0.4*(2000-ti));
		}
		ArrayList<Integer> fhrlistIntact = FileData.intArray_to_listArray(testfhrlist);
		
		FhrAnalyse fhrAnalyse = new FhrAnalyse();
		AnalyseResult analyseResult = new AnalyseResult();;
		ArrayList<int[]> acc = fhrAnalyse.getAcc(fhrlistIntact, analyseResult , 3.2f);
		
		ArrayList<int[]> dec = fhrAnalyse.getDecNew(fhrlistIntact, analyseResult, 3.2f);
		for (int[] is : acc) {
			System.out.println("加速acctime "+is[0] +"开始时间: " + is[1] + "结束时间:  " + is[2]);
		}
		
		for (int[] is : dec) {
			System.out.println("减速acctime "+is[0] +"开始时间: " + is[1] + "结束时间:  " + is[2]);
		}
	}
}
