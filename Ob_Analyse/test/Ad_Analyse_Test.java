
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.lianmed.analyse.Afm_detect_syn;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.filetool.FileData;
import com.lianmed.filetool.FileTool;
import com.lianmed.filetool.MyCTG;

public class Ad_Analyse_Test {

	@Test
	public void test() {

		
		
		FhrAnalyse fhranalysetool = new FhrAnalyse();	
		FileTool filetool = new FileTool();
		
		String filenumstr = "19831_161010113339";
		String filepathpart = filenumstr + "/"+ filenumstr;
		List<MyCTG> myctgdata = filetool.readDataFromFile("E:/ctg_toco_170828/completedoc/"
				+ filepathpart
				+ ".fatal");
		
		// 19686_160928145604   19598_160926083534 19507_160922085622 19413_160920093120 19019_160912164212
		
		/*
		List<Integer> fhrlist = new ArrayList<Integer>();
		List<Integer> tocolist = new ArrayList<Integer>();		
		
		for(int i=0;i<myctgdata.size();i++){
			fhrlist.add(myctgdata.get(i).fhr);
			tocolist.add(myctgdata.get(i).toco);
		}		
		*/
		Afm_detect_syn afm_detect_syn_tool = new Afm_detect_syn();	
		
		
		//E:\220project\20181028_temp\document\30\181026155657
		String filenum = "30";
		String documentid = "181026155657";
		/*
		List<Integer> fmlist = filetool.readDataFromMove("E:/220project/20181028_temp/document/"
				+ filenum + "/"
				+ documentid + "/"
				+ documentid + "_0.move");
				*/
		
		//int[] 
		int [] testfmlist = FileData.readFmpBasedataToArrayfromFile("E:/220project/20181028_temp/document/"
				+ filenum + "/"
				+ documentid + "/"
				+ documentid + "_0.move");
		
		ArrayList<Integer> fmlist = FileData.intArray_to_listArray(testfmlist);
		
		int[] testfhrlist = FileData.readFHRfromFile("E:/220project/20181028_temp/document/"
				+ filenum + "/"
				+ documentid + "/"
				+ documentid + ".fetal");
		
		ArrayList<Integer> fhrlist = FileData.intArray_to_listArray(testfhrlist);
		
		
		int[] testtocolist = FileData.readTOCOfromFile("E:/220project/20181028_temp/document/"
				+ filenum + "/"
				+ documentid + "/"
				+ documentid + ".fetal");
		
		ArrayList<Integer> tocolist = FileData.intArray_to_listArray(testtocolist);
		
		System.out.print(testfmlist.length);
//		List<Integer> mylist = afm_detect_syn_tool.calFMSysn_Fix(fhrlist, tocolist, fmlist,3.2f);
//		
//		for(int i=0;i<mylist.size();i++){			
//			System.out.print("acc" + mylist.get(i)+" ");						
//		}
		
		
	}
		
		
		
	

}
