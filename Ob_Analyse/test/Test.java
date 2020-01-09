import java.util.ArrayList;

import com.lianmed.analyse.AnalyseResult;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.filetool.FileData;

public class Test {

	public static void main(String[] args) {
		
    	String movefilestr = "D:/lianProject/new_data/67/190118162619/190118162619_0.move";
		
		String fetalfilestr = "D:/lianProject/new_data/67/190118162619/190118162619.fetal";
		
		int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr);
		ArrayList<Integer> fhrlistIntact = FileData.intArray_to_listArray(testfhrlist);
		FhrAnalyse fhrAnalyse = new FhrAnalyse();
		AnalyseResult analyseResult = new AnalyseResult();
		fhrAnalyse.getStv(fhrlistIntact, analyseResult);
		fhrAnalyse.getLtv(fhrlistIntact, analyseResult);
		System.out.println(analyseResult.hightime);
		System.out.println(analyseResult.stv);
	}
}
