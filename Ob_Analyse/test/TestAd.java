import java.util.ArrayList;
import java.util.List;

import com.lianmed.analyse.Afm_detect_syn;
import com.lianmed.analyse.AnalyseResult;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.filetool.FileData;
import com.lianmed.filetool.FileTool;
import com.lianmed.filetool.MyCTG;

/*
class Mytest{
	
	public int 
	
	
}
*/
public class TestAd {

	
	public static void main2(String[] args){
		
//		List<Integer> test1 = new ArrayList<Integer>();
//		test1.add(-129);
//		
//		List<Integer> test2 = new ArrayList<Integer>();
//		test2.add(-129);
		for(int i=0;i<0;i++){
			System.out.println("nana");
		}
		Integer i0 = 130;
		Integer i1 = 120;
		Integer i2 = 120;
		Integer i3 = 120;
		Integer i4 = 130;
		int i5 = 130;
		if(i0 == i5){	
			System.out.println("haha");
		} else {
			System.out.println("not");
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
						
		FhrAnalyse fhranalysetool = new FhrAnalyse();	
		FileTool filetool = new FileTool();
		
		String filenumstr = "19831_161010113339";
		String filepathpart = filenumstr + "/"+ filenumstr;
//		List<MyCTG> myctgdata = filetool.readDataFromFile("E:/ctg_toco_170828/completedoc/"
//				+ filepathpart
//				+ ".fatal");
		
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
		
		//181029102617
		//31/181026161108
		//45\181029155327
		//000\18110411034
		//44\181029153056
		
		//38\181029102617
		
		//37\181029095905
		
		//79\181106112107
		//70/181106082756
		//48\181029170610
		//72\181106093304
		//97/181108113610
		
		//96\181108111027
		//95\181108104817
		//94\181108102522
		//93\181108100123
		
	    //92\181108093700
		//100\181108161439
		String filenum = "94";
		String documentid = "181108102522";
		/*
		List<Integer> fmlist = filetool.readDataFromMove("E:/220project/20181028_temp/document/"
				+ filenum + "/"
				+ documentid + "/"
				+ documentid + "_0.move");
				*/
		
		//int[] 
		
//		String movefilestr = "E:/220project/20181028_temp/document/"
//				+ filenum + "/"
//				+ documentid + "/"
//				+ documentid + "_0.move";
		
		String movefilestr = "D:/lianmed/document/33/190112092651/190112092651_0.move";
		
//		String fetalfilestr = "E:/220project/20181028_temp/document/"
//				+ filenum + "/"
//				+ documentid + "/"
//				+ documentid + ".fetal";
		
		String fetalfilestr = "D:/lianmed/document/33/190112092651/190112092651.fetal";
		
		int [] testfmlist = FileData.readFmpBasedataToArrayfromFile(movefilestr);
		
		ArrayList<Integer> fmlist = FileData.intArray_to_listArray(testfmlist);
		
		int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr);
		
		ArrayList<Integer> fhrlist = FileData.intArray_to_listArray(testfhrlist);
		
		
		int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr);
		
		ArrayList<Integer> tocolist = FileData.intArray_to_listArray(testtocolist);
		
		//System.out.print("**************************************"+testfmlist.length);
		
		//boolean getend = false;
		int timetag = 100;
		int step = 20;
		//int time
		
		
		while(true){
			if(timetag>fhrlist.size()){
				timetag = fhrlist.size();
			}
			AnalyseResult analyseResult = new AnalyseResult();
			List<Integer> mylist = afm_detect_syn_tool.calFMSysn_Fix_RT(new ArrayList(fhrlist.subList(0, timetag)),
					new ArrayList(tocolist.subList(0, timetag)), 
					new ArrayList(fmlist.subList(0, timetag)),analyseResult,3.2f
					);
			//List<Integer> mylist = afm_detect_syn_tool.calFMSysn_Fix(fhrlist, tocolist, fmlist);
			if(timetag>500){
				System.out.println("start");
			}
			
			System.out.print("curtime:" + timetag +",");
			for(int i=0;i<mylist.size();i++){			
				System.out.print("output fm:" + mylist.get(i)+",");		
			}
			System.out.println("");
			
			if(timetag == fhrlist.size()){
				break;
			}
			
			timetag += step;
			
		}
		/*
		List<Integer> mylist = afm_detect_syn_tool.calFMSysn_Fix_RT(fhrlist, tocolist, fmlist);
		//List<Integer> mylist = afm_detect_syn_tool.calFMSysn_Fix(fhrlist, tocolist, fmlist);
		for(int i=0;i<mylist.size();i++){			
			System.out.print("output fm:" + mylist.get(i)+",");						
		}
		*/		
		
		
	}

}
