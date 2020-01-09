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
		
		//���ļ��ж���̥��ԭʼ�ź�
		int [] testfmlist = FileData.readFmpBasedataToArrayfromFile(movefilestr);
		
		//��̥��ԭʼ�ź�תΪArrayList<Integer> ���ͣ���Ϊ��߷����Ĳ����õ���ArrayList<Integer> ����
		ArrayList<Integer> fmlist = FileData.intArray_to_listArray(testfmlist);
		
		//��ԭʼ̥���źŽ���Ԥ����
		ArrayList<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmlist);
		
		//���ļ��ж���
		int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr);
//		int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr);
		
		//����ֻ����testfhrlist�м���Ϊ����һ�����ٵ�ģ�⣬���ź��½���Ϊ�˲��Լ��ٵķ����ܷ�������٣����԰��������ע�͵���
		for(int ti=1500;ti<1750;ti++){
			testfhrlist[ti] = (int)(testfhrlist[ti]-0.4*(ti-1500));
		}
		for(int ti=1750;ti<2000;ti++){
			testfhrlist[ti] = (int)(testfhrlist[ti]-0.4*(2000-ti));
		}
		
		//ͬ������fhrlistתΪArrayList<Integer>����
		ArrayList<Integer> fhrlist = FileData.intArray_to_listArray(testfhrlist);
		
		//���ļ��ж��������ź�
		int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr);
		for (int i : testtocolist) {
			System.out.println(i);
		}
		//�������ź�תΪArrayList<Integer>����
		ArrayList<Integer> tocolist = FileData.intArray_to_listArray(testtocolist);
		
		//̥���ʵķ���������������У� FhrAnalyse
    	FhrAnalyse fhrAnalysetool = new FhrAnalyse();
    	
    	//�����źŵķ���������������У� UcAnalyse
    	UcAnalyse ucAnalysetool = new UcAnalyse();
    	
    	//AnalyseResult �������� �洢��ǰ̥�����ݵķ������������洢���ݼ����еı�����ע�� 
    	AnalyseResult analyseResult = new AnalyseResult();
    	//���ù�����ⷽ��
    	ucAnalysetool.detectUc(tocolist, analyseResult, 3.2f);
    	
    	//fix_change�������������Ϊ��java����ο�����C����Ĺ��̣�ԭ���ķ���������0.8Sһ���㣬���ڸ�Ϊ0.25Sһ���㣬��Ϊ�ڷ����н����˶�Ӧ�����Ĵ��������������
    	float fix_change = 3.2f;
    	
    	//���� ̥�ļ��ټ��ķ�����ȡ���ٵ�λ�õ���Ϣ�����������
    	analyseResult.accresult = fhrAnalysetool.getAcc(fhrlist,analyseResult,fix_change);
    	
//    	ArrayList<Integer> fhrbaseline = fhrAnalysetool.getFhrBaseline(fhrlist, 3.2f);
    	
    	//���� ̥�ļ����ټ��ķ�����ȡ���ٵ�λ�õ���Ϣ�����������
    	ArrayList<int[]> decresult = fhrAnalysetool.getDec(fhrlist,analyseResult, 3.2f);
    	
    	//���̥���ķ�����Afm_detect_syn����
    	Afm_detect_syn afm_detect_syn_tool = new Afm_detect_syn();	
    	
    	//���÷����е�̥����ⷽ����̥�����������analyseResult��
    	//tyl:fhr,toco,fmm�������ϼ���fm�ķ���ΪcalFMSysn_Fix_RT
    	afm_detect_syn_tool.calFMSysn_Fix_RT(fhrlist, tocolist, fmlist,analyseResult,3.2f);
    	
    	//����  ��ȡ���̱���ķ���������洢��analyseResult�� �������������з��ؽ���ģ�Ҳ�����ñ��������շ������صĽ��
    	fhrAnalysetool.getLtv(fhrlist, analyseResult);
    	fhrAnalysetool.getStv(fhrlist, analyseResult);
    	
    	//���ַ���
    	Assessment assessmenttool = new Assessment();
    	
    	//kerbs���ַ�
    	KerbsRst kerbsrst =  assessmenttool.KerbsAssessment(analyseResult);
    	
    	//Fisher���ַ�
    	FisherRst fisherRst = assessmenttool.FisherAssessment(analyseResult);
    	
    	//Nst���ַ�
    	NstRst nstRst = assessmenttool.NstAssessment(analyseResult);
    	
    	
    	
    	
    	
//    	
//    	System.out.println(Arrays.toString(decresult.get(0)));
    	
    	
//    	public ArrayList<DecData> getDec(List<Integer> srcarrfhr,List<Integer> fhrbaseline,List<Integer> fittedfhrSeg,float fix_change)
		
		
		

	}

}
