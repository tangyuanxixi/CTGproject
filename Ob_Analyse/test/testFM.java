import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lianmed.analyse.AfmTool;
import com.lianmed.analyse.Afm_detect_syn;
import com.lianmed.analyse.AnalyseResult;
import com.lianmed.analyse.FMData;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.analyse.Fm_process;
import com.lianmed.filetool.FileData;

public class testFM {

	public static void main(String[] args) throws IOException {
		
		String path = "";
		List<String> filePath = readFile("D:\\lianProject\\ctgfile\\filedir\\dir.txt");
		System.out.println(filePath.size());

		Map<String,List<Integer>> myFm =  new HashMap<>();
		Map<String, List<Integer>> hmjFm = new HashMap<>();
		
		for(int i = 0; i < filePath.size(); i++){
			
			String string = filePath.get(i);
			String movefilestr = "D:/lianProject/ctgfile/new_data"+ string +"_0.move"; // 胎儿活动图曲线从里面抽取
			
			String fetalfilestr = "D:/lianProject/ctgfile/new_data"+ string +".fetal"; // 宫缩曲线跟胎心率曲线从里面抽取
			
			
			// 数据预处理
			int[] testfmlist = FileData.readFmpBasedataToArrayfromFile(movefilestr); // 胎儿活动图
			
			int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr); // 宫缩曲线
			int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr); // fhr曲线
			
			ArrayList<Integer> fmlist = FileData.intArray_to_listArray(testfmlist); 
			
			
			//胎儿活动图曲线
			List<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmlist); 


			// 胎心率曲线
			ArrayList<Integer> fhrlistIntact = FileData.intArray_to_listArray(testfhrlist);
			
			/*String fileName = string.split("/")[1];

			File file = new File("D:\\lianProject\\testfile\\" + fileName + ".txt");
			
			
			 FileWriter fw = new FileWriter(file);
			for (Integer integer : fhrlistIntact) {
				
				fw.write(integer + " ");
			}
			fw.close();*/
			
			// 宫缩曲线
			ArrayList<Integer> tocolist = FileData.intArray_to_listArray(testtocolist);

	    	
	    	AnalyseResult analyseResult = new AnalyseResult();
	    	Afm_detect_syn afm_detect_syn = new Afm_detect_syn();
	    	
	    	/**
	    	 * 模拟实时胎心率曲线
	    	 */      
	    	
	    	List<Integer> fhrlist = new ArrayList<>();
	    	List<Integer> fmpartlist = new ArrayList<>();
	    	List<Integer> tocopartlist = new ArrayList<>();
	    	
	    	
	    	List<Integer> fmSysn_Fix_RTBL = new ArrayList<>();
	    	
	    	ArrayList<Integer> fhrbaseline_RT = new ArrayList<>();

	    	FhrAnalyse fhrAnalysetool = new FhrAnalyse();
	    	
	    	
	    	ArrayList<Integer> removeFhr = new ArrayList<>();
	    	int realTimeCell = 480;//每次出的点的个数
	    	int lastIndex = 0;
	    	List<Integer> partFMSysn_RT = new ArrayList<>();
	    	
	    	for(int fhrIndex = 0; fhrIndex < fhrlistIntact.size(); fhrIndex += realTimeCell,realTimeCell = 20)
	    	{

	    		int index = fhrIndex + realTimeCell;
	    		
	    		// 模拟部分fhr
	    		if(index>fhrlistIntact.size())
	    			index = fhrlistIntact.size();
	    		fhrlist = fhrlistIntact.subList(0, index);
	    		
	    		// 模拟部分fm
	    		int fmIndex = index;
	    		if(fmIndex > newafmarray.size())
	    			fmIndex = newafmarray.size();
	    		fmpartlist = newafmarray.subList(0, fmIndex);
	    		
	    		// 模拟部分toco
	    		int tocoIndex = index;
	    		if(tocoIndex > tocolist.size())
	    			tocoIndex = tocolist.size();
	    		tocopartlist = tocolist.subList(0,tocoIndex);
	    	
	    		List<Integer> fmbaselinepart = Fm_process.getFMBaseline(fmpartlist);
	    		AfmTool afmtoolpart = new AfmTool();
	    		List<FMData> fmobarraypart = afmtoolpart.getFM(fmpartlist, fmbaselinepart);
	    		for (FMData fmData : fmobarraypart) {
	    			System.out.println("棘波：" + fmData.fmstart);
	    		}

	    		
	    		// 包含去掉加减速的基线
	        	ArrayList<Integer> fhrbaseline = fhrAnalysetool.getFhrBaseline_RT_removeAccAndDec(fhrlist,lastIndex, 3.2f); 
	        	
	        	fhrbaseline_RT.addAll(fhrbaseline);



	        	ArrayList<Integer> removeAccOrDecFhr = fhrAnalysetool.getRemoveAccOrDecFhr(fhrlist, lastIndex, 3.2f);
	        	removeFhr.addAll(removeAccOrDecFhr);

	        	
	    		// 求胎动标记点
	        	partFMSysn_RT = fmSysn_Fix_RTBL;
	        	
	        	partFMSysn_RT = afm_detect_syn.calFMSysn_RTbaseLine(fhrlist, tocopartlist, fmpartlist,
	        													analyseResult, 3.2f, realTimeCell, partFMSysn_RT, fhrbaseline_RT);
	        	
	        	
	    		lastIndex = index;
	    			
	    		fmSysn_Fix_RTBL.addAll(partFMSysn_RT);
	    		
		    	
				
	    	}
	    	
	    	// 梅佳师兄的求胎动点和基线的方法
	    	List<Integer> calFMSysn_Fix_RT = afm_detect_syn.calFMSysn_Fix_RT(fhrlistIntact, tocolist, newafmarray, 
	    																		analyseResult, 3.2f);
	    	
	    	//去掉加减速FHR曲线
	    	FhrAnalyse fhrAnalyse = new FhrAnalyse();
	    	
	    	ArrayList<Integer> fhrbaseline_RT_old = fhrAnalyse.getRemoveAccOrDecFhr(fhrlistIntact, 0, 3.2f);
	    	
	    	//去掉加减速最后一次求出基线
	    	ArrayList<Integer> fhrBaseline_RT_removeAccAndDec = fhrAnalyse.getFhrBaseline_RT_removeAccAndDec(fhrlistIntact, 0, 3.2f);
	    	
	    	myFm.put(string, fmSysn_Fix_RTBL);
	    	hmjFm.put(string, calFMSysn_Fix_RT);
			String fileName = string.split("/")[1];
			
			// reaccfile,保存去掉加减速FHR曲线
			File file = new File("D:\\lianProject\\ctgfile\\reaccfile\\" + fileName + ".txt"); 
			
			 FileWriter fw = new FileWriter(file);
			for (Integer integer : fhrbaseline_RT_old) {
				
				fw.write(integer + " ");
			}
			fw.close();
			
			// fmmfile,保存胎儿活动图曲线
			File filefmm = new File("D:\\lianProject\\ctgfile\\fmmfile\\" + fileName + ".txt");
			
			FileWriter fwmm = new FileWriter(filefmm);
			for (Integer integer : newafmarray) {
				
				fwmm.write(integer + " ");
			}
			fwmm.close();
			
			// bsfile, 基线
			File filebs = new File("D:\\lianProject\\ctgfile\\bsfile\\" + fileName + ".txt");
			
			FileWriter bsfile = new FileWriter(filebs);
			for (Integer integer : fhrBaseline_RT_removeAccAndDec) {
				
				bsfile.write(integer + " ");
			}
			bsfile.close();
	    	// fmfile, 胎动点
			File filefm = new File("D:\\lianProject\\ctgfile\\fmfile\\" + fileName + ".txt");
			
			FileWriter fmfile = new FileWriter(filefm);
			for (Integer integer : fmSysn_Fix_RTBL) {
				
				fmfile.write(integer + " ");
			}
			fmfile.write("\n");
			for (Integer integer : calFMSysn_Fix_RT) {
				fmfile.write(integer + " ");
			}
			fmfile.close();
			
	    	// tocofile, 胎动点
			File filetoco = new File("D:\\lianProject\\ctgfile\\tocofile\\" + fileName + ".txt");
			
			FileWriter tocofile = new FileWriter(filetoco);
			for (Integer integer : tocolist) {
				
				tocofile.write(integer + " ");
			}
			tocofile.close();
		}
		
		
		
		
		for(int fmIndex = 0; fmIndex < filePath.size(); fmIndex++) {
			
			String fileIndex = filePath.get(fmIndex);
			
			List<Integer> myFmList = myFm.get(fileIndex);
			List<Integer> hmjFmList = hmjFm.get(fileIndex);
			
			System.out.print(fileIndex + " my:");
			for (Integer fm : myFmList) {
				System.out.print(fm + " ");
			}
			System.out.println("");
			System.out.print(fileIndex + " hmj:");
			for (Integer fm : hmjFmList) {
				System.out.print(fm + " ");
			}
			System.out.println("\n");
		}

	}

	private static List<String> readFile(String path) throws IOException {
		
		List<String> list = new ArrayList<String>();
		FileInputStream fis = new FileInputStream(path);
		
		InputStreamReader isr = new InputStreamReader(fis, "GBK");
		
		BufferedReader br = new BufferedReader(isr);
		try{
			
			String line = "";
			while((line=br.readLine()) != null)
				list.add(line);
		}finally{
			
			br.close();
			isr.close();
			fis.close();
		}
		return list;
	}
}
