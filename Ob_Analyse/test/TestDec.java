import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lianmed.analyse.Afm_detect_syn;
import com.lianmed.analyse.AnalyseResult;
import com.lianmed.analyse.Assessment;
import com.lianmed.analyse.DecData;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.analyse.FisherRst;
import com.lianmed.analyse.Fm_process;
import com.lianmed.analyse.KerbsRst;
import com.lianmed.analyse.NstRst;
import com.lianmed.analyse.UcAnalyse;
import com.lianmed.filetool.FileData;
import com.lianmed.filetool.FileTool;
public class TestDec {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String movefilestr = "D:/lianProject/new_data/190107154356/190107154356.move";
		String fetalfilestr = "D:/lianProject/new_data/190107154356/190107154356.fetal";
		
		//从文件中读出胎动原始信号
		int [] testfmlist = FileData.readFmpBasedataToArrayfromFile(movefilestr);
		
		//将胎动原始信号转为ArrayList<Integer> 类型，因为后边方法的参数用的是ArrayList<Integer> 类型
		ArrayList<Integer> fmlist = FileData.intArray_to_listArray(testfmlist);
		
		//把原始胎动信号进行预处理
		ArrayList<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmlist);
		
		//从文件中读出
		int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr);
//		int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr);
		
		//这里只是在testfhrlist中间人为做了一个减速的模拟，让信号下降，为了测试减速的方法能否检测出减速，可以把这个代码注释掉的
		for(int ti=1500;ti<1750;ti++){
			testfhrlist[ti] = (int)(testfhrlist[ti]-0.4*(ti-1500));
		}
		for(int ti=1750;ti<2000;ti++){
			testfhrlist[ti] = (int)(testfhrlist[ti]-0.4*(2000-ti));
		}
		
		//同样，将fhrlist转为ArrayList<Integer>类型
		ArrayList<Integer> fhrlist = FileData.intArray_to_listArray(testfhrlist);
		
		//从文件中读出宫缩信号
		int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr);
		for (int i : testtocolist) {
			System.out.println(i);
		}
		//将宫缩信号转为ArrayList<Integer>类型
		ArrayList<Integer> tocolist = FileData.intArray_to_listArray(testtocolist);
		
		//胎心率的分析方法在这个类中： FhrAnalyse
    	FhrAnalyse fhrAnalysetool = new FhrAnalyse();
    	
    	//宫缩信号的分析方法在这个类中： UcAnalyse
    	UcAnalyse ucAnalysetool = new UcAnalyse();
    	
    	//AnalyseResult 类是用来 存储当前胎监数据的分析结果，具体存储内容见类中的变量和注释 
    	AnalyseResult analyseResult = new AnalyseResult();
    	//调用宫缩检测方法
    	ucAnalysetool.detectUc(tocolist, analyseResult, 3.2f);
    	
    	//fix_change这个参数，是因为，java代码参考的是C代码的工程，原来的分析，都是0.8S一个点，现在改为0.25S一个点，因为在方法中进行了对应比例的处理，具体见方法。
    	float fix_change = 3.2f;
    	
    	//调用 胎心加速检测的方法获取加速的位置等信息，具体见方法
    	analyseResult.accresult = fhrAnalysetool.getAcc(fhrlist,analyseResult,fix_change);
    	
//    	ArrayList<Integer> fhrbaseline = fhrAnalysetool.getFhrBaseline(fhrlist, 3.2f);
    	
    	//调用 胎心减速速检测的方法获取减速的位置等信息，具体见方法
    	ArrayList<int[]> decresult = fhrAnalysetool.getDec(fhrlist,analyseResult, 3.2f);
    	
    	//检测胎动的方法在Afm_detect_syn类中
    	Afm_detect_syn afm_detect_syn_tool = new Afm_detect_syn();	
    	
    	//调用方法中的胎动检测方法，胎动结果存在了analyseResult中
    	//tyl:fhr,toco,fmm三者相结合计算fm的方法为calFMSysn_Fix_RT
    	afm_detect_syn_tool.calFMSysn_Fix_RT(fhrlist, tocolist, fmlist,analyseResult,3.2f);
    	
    	//调用  获取长短变异的方法，结果存储在analyseResult中 ，这两个方法有返回结果的，也可以用变量来接收方法返回的结果
    	fhrAnalysetool.getLtv(fhrlist, analyseResult);
    	fhrAnalysetool.getStv(fhrlist, analyseResult);
    	
    	//评分方法
    	Assessment assessmenttool = new Assessment();
    	
    	//kerbs评分法
    	KerbsRst kerbsrst =  assessmenttool.KerbsAssessment(analyseResult);
    	
    	//Fisher评分法
    	FisherRst fisherRst = assessmenttool.FisherAssessment(analyseResult);
    	
    	//Nst评分法
    	NstRst nstRst = assessmenttool.NstAssessment(analyseResult);
    	
    	
    	
    	
    	
//    	
//    	System.out.println(Arrays.toString(decresult.get(0)));
    	
    	
//    	public ArrayList<DecData> getDec(List<Integer> srcarrfhr,List<Integer> fhrbaseline,List<Integer> fittedfhrSeg,float fix_change)
		
		
		

	}

}
