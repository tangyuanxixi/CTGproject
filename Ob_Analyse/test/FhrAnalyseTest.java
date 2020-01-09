import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.lianmed.analyse.Afm_detect_syn;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.filetool.FileTool;
import com.lianmed.filetool.MyCTG;

public class FhrAnalyseTest {

	@Test
	public void test() {
		
		FhrAnalyse fhranalysetool = new FhrAnalyse();
		
		FileTool filetool = new FileTool();
		
		String filenumstr = "19826_161010104449";
		String filepathpart = filenumstr + "/"+ filenumstr;
/*		List<MyCTG> myctgdata = filetool.readDataFromFile("E:/ctg_toco_170828/completedoc/"
				+ filepathpart
				+ ".fatal");*/
		
		List<MyCTG> myctgdata = filetool.readDataFromFile("D:/lianProject/new_data/190107154356/190107154356.fetal");
		// 19686_160928145604   19598_160926083534 19507_160922085622 19413_160920093120 19019_160912164212
		
		List<Integer> fhrlist = new ArrayList<Integer>();
		List<Integer> tocolist = new ArrayList<Integer>();
		
		
		for(int i=0;i<myctgdata.size();i++){
			fhrlist.add(myctgdata.get(i).fhr);
			tocolist.add(myctgdata.get(i).toco);
		}
		
		
		Afm_detect_syn afm_detect_syn_tool = new Afm_detect_syn();
		
		
		
/*		ArrayList<Integer> fmlist = filetool.readDataFromMove("E:/ctg_toco_170828/completedoc/"
				+ filepathpart
				+ ".move");*/
		
		ArrayList<Integer> fmlist = filetool.readDataFromMove("D:/lianProject/new_data/190107154356/190107154356.move");
//		List<Integer> mylist = afm_detect_syn_tool.calFMSysn_Fix(fhrlist, tocolist, fmlist,3.2f);
//		
//		for(int i=0;i<mylist.size();i++){
//			
//			System.out.print("acc" + mylist.get(i)+" ");
//						
//		}
		//int a = mylist.size();
		
		//System.out.println(a);
		
		/*
		List<int[]> accresult = fhranalysetool.getAcc(fhrlist);
		int s = accresult.size();
		System.out.println(s);
		*/
		//fail("Not yet implemented");
	}

}
