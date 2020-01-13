import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.lianmed.analyse.AfmTool;
import com.lianmed.analyse.Afm_detect_syn;
import com.lianmed.analyse.AnalyseResult;
import com.lianmed.analyse.FMData;
import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.analyse.Fm_process;
import com.lianmed.filetool.FileData;



/**
 * 使用Graphics类绘图
 * 
 * @author
 *
 */
public class ShowCTG extends JFrame {
	

	private static final long serialVersionUID = 1L;
	
	public ShowCTG() {

    	
    	
    	String movefilestr = "D:/lianProject/ctgfile/new_data/15/190110093416/190110093416_0.move"; // 胎儿活动图曲线从里面抽取
		
		String fetalfilestr = "D:/lianProject/ctgfile/new_data/15/190110093416/190110093416.fetal"; // 宫缩曲线跟胎心率曲线从里面抽取
		
		
		// 数据预处理
		int[] testfmlist = FileData.readFmpBasedataToArrayfromFile(movefilestr); // 胎儿活动图
		
		int[] testtocolist = FileData.readTOCOfromFile(fetalfilestr); // 宫缩曲线
		int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr); // fhr曲线

		ArrayList<Integer> fmlist = FileData.intArray_to_listArray(testfmlist); 
		
		
		//胎儿活动图曲线
		List<Integer> newafmarray = Fm_process.getAFMDataFromMove(fmlist); 


		// 胎心率曲线
		ArrayList<Integer> fhrlistIntact = FileData.intArray_to_listArray(testfhrlist);


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
    	ArrayList<Integer> fhrbaseline_RT_old = new ArrayList<>();
    	
    	FhrAnalyse fhrAnalysetool = new FhrAnalyse();
    	
    	
    	ArrayList<Integer> removeFhr = new ArrayList<>();
    	int realTimeCell = 480;//每次出的点的个数
    	int lastIndex = 0;
    	List<Integer> partFMSysn_RT = new ArrayList<>();
    	int flag = 20;
    	
    	for(int i = 0; i < fhrlistIntact.size(); i += realTimeCell,realTimeCell = 20)
    	{

    		int index = i + realTimeCell;
    		
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
    	//去掉加减速最后一次求出基线
    	FhrAnalyse fhrAnalyse = new FhrAnalyse();
    	ArrayList<Integer> fhrBaseline_RT_removeAccAndDec = fhrAnalyse.getFhrBaseline_RT_removeAccAndDec(fhrlistIntact, 0, 3.2f);
    	
    	fhrbaseline_RT_old = fhrAnalyse.getRemoveAccOrDecFhr(fhrlistIntact, 0, 3.2f);
    	
    	List<Integer> newfmSysn_Fix_RT = new ArrayList<>(fmSysn_Fix_RTBL);
    	
    	// 梅佳师兄的求胎动点和基线的方法
    	List<Integer> calFMSysn_Fix_RT = afm_detect_syn.calFMSysn_Fix_RT(fhrlistIntact, tocolist, newafmarray, 
    																		analyseResult, 3.2f);
    	ArrayList<Integer> fittedfhrSeg = analyseResult.fittedfhrSeg;
    	System.out.println("fittedfhrseg" + fittedfhrSeg);
    	ArrayList<Integer> fhrBaseline = fhrAnalysetool.getFhrBaseline(fhrlistIntact, 3.2f);
    	
    	MyNewCTGPane myNewPane = new MyNewCTGPane(newafmarray, fhrlistIntact, tocolist, fhrbaseline_RT,newfmSysn_Fix_RT, 
    			removeFhr,calFMSysn_Fix_RT,fhrBaseline_RT_removeAccAndDec);

    	Container c = getContentPane();
    	setSize(1900, 800);
    	
    	JScrollPane sp1 = new JScrollPane(myNewPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
        		JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    	sp1.setSize(1800, 700);
    	

    	c.add(sp1);

    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	this.setLayout(null);
        setLocationRelativeTo(null);
    }
    
    /**
     * 提供的接口,返回胎动点
     * @param movefilestr
     * @param fetalfilestr
     */
    public List<Integer> CalFM(String movefilestr, String fetalfilestr){
    	
    	
    	List<Integer> fmResultList = new ArrayList<>();
    	
    	
    	return fmResultList;
    }
    public static void main(String[] args) {

    	new ShowCTG().setVisible(true);
    }
 
}
class MyNewCTGPane extends JPanel{
	

	private static final long serialVersionUID = 1L;
	
	public List<Integer> fmlist; //胎儿活动图曲线
	public List<Integer> fhrlist; // fhr曲线
	public ArrayList<Integer> tocolist; // 宫缩曲线
	public ArrayList<Integer> fhrbaseline; // fhr基线
	public List<Integer> fmSysn_Fix_RTbaseLine; // 胎动点实时
	public ArrayList<Integer> fhrbaselineold; // 没有去加减速
	public List<Integer> calFMSysn_Fix_RT; // 胎动点梅佳师兄
	public List<Integer> fhrBaselinehmj; //梅佳师兄版本
	
	public MyNewCTGPane(List<Integer> fmlist, List<Integer> fhrlist, ArrayList<Integer> tocolist,
			ArrayList<Integer> fhrbaseline, List<Integer> fmSysn_Fix_RTbaseLine, ArrayList<Integer> fhrbaselineold,
			List<Integer> calFMSysn_Fix_RT,List<Integer> fhrBaselinehmj) {
		super();
		this.fmlist = fmlist;
		this.fhrlist = fhrlist;
		this.tocolist = tocolist;
		this.fhrbaseline = fhrbaseline;
		this.fmSysn_Fix_RTbaseLine = fmSysn_Fix_RTbaseLine;
		this.fhrbaselineold = fhrbaselineold;
		this.calFMSysn_Fix_RT = calFMSysn_Fix_RT;
		this.fhrBaselinehmj = fhrBaselinehmj;
		this.setPreferredSize(new Dimension(8000,600));
	}
	@Override
	public void paint(Graphics g){
		
		boolean debugFlag = false;
		this.setVisible(false);
		int step = 80;
		
		//画网格
		// FHR 曲线面板

		for(int start = 0; start <= 200; start+=20)
		{

			g.drawString(180 - start/2+"", 0, start);
			g.drawLine(0, start, 9000, start);
		}

        for(int j = 0; j < 9000; j+=80){
			
			Graphics2D graphics2d = (Graphics2D) g;
			if((j-80)%240==0){
				graphics2d.setStroke(new BasicStroke(1f));

			}else{
				graphics2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1f,new float[]{10, 5},0f));
			}
			graphics2d.drawLine(j, 0, j, 210);
		}

		// 其他相关面板
		int instance = 300;
		
		for(int i = instance; i <= 200 + instance; i+=20)
		{

			g.drawString(300 - i/2 +"", 0, i);
			g.drawLine(0, i, 9000, i);
		}

        for(int j = 0; j < 9000; j+=80){
        	if((j-80)%240==0){
        		g.drawString((j-80)/240 + "分", j, 220);
        	}
		}
        
		for(int j = 0; j < 9000; j+=80){
			Graphics2D graphics2d = (Graphics2D) g;
			if((j-80)%240==0){
				graphics2d.setStroke(new BasicStroke(1f));

			}else{
				graphics2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1f,new float[]{10, 5},0f));
			}
			graphics2d.drawLine(j, 300, j, 510);

		}
        
		// 曲线相关说明
		g.setColor(Color.red);
		g.drawString("____  FHR", 10, 240);
		g.setColor(Color.ORANGE);
		g.drawString("____  去除加减速基线", 110, 240);	
		g.setColor(Color.green);
		g.drawString("____  基线", 260, 240);	
		g.setColor(Color.red);
		g.drawString("+  实时胎动点", 10, 270);	
		g.setColor(Color.BLACK);
		g.drawString("*  原始胎动点", 110, 270);
		
		g.setColor(Color.BLUE);
		g.drawString("____  TOCO", 10, 550);
		g.setColor(Color.GRAY);
		g.drawString("____  FMM", 110, 550);

		
        //画胎心率曲线
		 g.setColor(Color.RED);
        int movelong = 180;
        if(debugFlag)
        	System.out.println("fhrlist:" + fhrlist.size());
        
        for(int i=0;i<fhrlist.size()-1;i++){
        	
        	if(i == fhrlist.size() - 2)
        		g.drawString(i + "", i, 100);
        	if(fhrlist.get(i) == 0)
        		continue;
        	g.drawLine(i+step, (movelong - fhrlist.get(i))*2, i+step+1, (movelong - fhrlist.get(i+1))*2);
        	
        	if(i==0)
        	{
        		g.drawString("0", i+step, 100);
        	}
        	else if((i+step)%80==0){
        		g.drawString(i+"", i+step, 100);
        	}

        }
        
        
        // 画胎心率基线
        
        g.setColor(Color.ORANGE);

        for(int i=0;i<fhrbaseline.size()-1;i++){
        	g.drawLine(i+step, (movelong-fhrbaseline.get(i))*2, i+1+step, (movelong-fhrbaseline.get(i+1))*2);
        }

        g.setColor(Color.blue);
        
        for(int i=0;i<fhrbaselineold.size()-1;i++){
        	g.drawLine(i+step, (movelong-fhrbaselineold.get(i))*2, i+1+step, (movelong-fhrbaselineold.get(i+1))*2);
        }
        
        g.setColor(Color.GREEN);
        
        for(int i=0;i<fhrBaselinehmj.size()-1;i++){
        	g.drawLine(i+step, (movelong-fhrBaselinehmj.get(i))*2, i+1+step, (movelong-fhrBaselinehmj.get(i+1))*2);
        }
        
        // 画宫缩曲线
        g.setColor(Color.BLUE);
        
        movelong = 240;

        for(int i=0;i<tocolist.size()-1;i++){

        	g.drawLine(i+step, (movelong-tocolist.get(i))*2, i+1+step, (movelong-tocolist.get(i+1))*2);
        }
        
        // 画胎儿活动图曲线
        g.setColor(Color.GRAY);
        movelong = 240;
        
       for(int i=0;i < fmlist.size()-1;i++){
        	g.drawLine(i+step, (movelong-fmlist.get(i))*2, i+1+step, (movelong-fmlist.get(i+1))*2);
       }

        // 胎动点
        g.setColor(Color.red);
        g.setFont(new Font("楷体", Font.BOLD, 30));
        if(debugFlag)
        	System.out.println("实时胎动点个数: " + fmSysn_Fix_RTbaseLine.size());
        for(int i = 0; i < fmSysn_Fix_RTbaseLine.size(); i++){
            g.drawString("+", fmSysn_Fix_RTbaseLine.get(i)+step-10, 150);
        	
        }

       
        g.setColor(Color.black);
        g.setFont(new Font("楷体", Font.BOLD, 30));
        if(debugFlag)
        	System.out.println("胎动点个数: " + calFMSysn_Fix_RT.size());
        for(int i = 0; i < calFMSysn_Fix_RT.size(); i++){
            g.drawString("*", calFMSysn_Fix_RT.get(i)+step, 180);
        	
        }  




		
        this.setVisible(true);
	}
	

}


class MyCTGPane extends JPanel{
	public ArrayList<Integer> fmlist;
	public ArrayList<Integer> fhrlist;
	public ArrayList<Integer> tocolist;
	public ArrayList<Integer> fhrbaseline;
	
	
	public MyCTGPane(ArrayList<Integer> fhrlist,
			ArrayList<Integer> fmlist,
			ArrayList<Integer> tocolist,
			ArrayList<Integer> fhrbaseline
			){
		this.fhrlist = fhrlist;
		this.fmlist = fmlist;
		this.tocolist = tocolist;
		this.fhrbaseline = fhrbaseline;
		this.setPreferredSize(new Dimension(8000,600));
		//setSize(3000, 600);
	}
	
	public void paint(Graphics g) {
		this.setVisible(false);
        g.setColor(Color.RED);
        // 画线段
//        g.drawLine(0, 100, 3000, 100);
        
        int movelong = 200;
        for(int i=0;i<fhrlist.size()-1;i++){
        	g.drawLine(i, movelong-fhrlist.get(i), i+1, movelong - fhrlist.get(i+1));
        	
        	if(i%240==0){
        		g.drawString(i + ":" + i/240 + "分", i, 100);
        	}
        	if(i%120==0 && i%240!=0){
        		g.drawString(i+"", i, 100);
        	}
        	
        }
        
        g.setColor(Color.ORANGE);
        
        
        
        for(int i=0;i<fhrbaseline.size()-1;i++){
        	g.drawLine(i, movelong-fhrbaseline.get(i), i+1, movelong-fhrbaseline.get(i+1));
        }
        
        
        g.setColor(Color.BLUE);
        
        movelong = 250;
        for(int i=0;i<tocolist.size()-1;i++){
        	g.drawLine(i, movelong-tocolist.get(i), i+1, movelong-tocolist.get(i+1));
        }
        
        g.setColor(Color.GRAY);
        movelong = 400;
        for(int i=0;i<fmlist.size()-1;i++){
        	g.drawLine(i, movelong-fmlist.get(i), i+1, movelong-fmlist.get(i+1));
        }
        
        
        
        

        // 绘制字符串
//        g.setColor(Color.GREEN);
//        g.setFont(new Font("楷体", Font.BOLD, 20));
//        g.drawString("使用画笔绘制的字符串内容", 220, 345);
        this.setVisible(true);
       
    }
}