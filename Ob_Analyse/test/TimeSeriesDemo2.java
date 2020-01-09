

/**
* =============================================================
* JFreeChart开发：利用JFreeChart开发实时曲线
* =============================================================
* Description:该例子演示了多条曲线的简单使用方法
* Original Author:xmf created by 2005-03-03
*
* Changes:
* -------------------------------------------------------------
* 在此处注明修改日期、修改点、修改人
* -------------------------------------------------------------
*/

//导入java2d包
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import com.lianmed.analyse.FhrAnalyse;
import com.lianmed.filetool.FileData;

public class TimeSeriesDemo2 extends JFrame implements Runnable,ActionListener{

   //申明实时曲线对象
   private TimeSeries timeseries1;
   private TimeSeries timeseries2;

   //Value坐标轴初始值
   private double lastValue1,lastValue2;
   private double originalValue1,originalValue2;

   static Class class$org$jfree$data$time$Millisecond;
   static Thread thread1;

   public static void main(String[] args){
     TimeSeriesDemo2 TimeSeriesDemo2 = new TimeSeriesDemo2();
     TimeSeriesDemo2.pack();
     RefineryUtilities.centerFrameOnScreen(TimeSeriesDemo2);
     TimeSeriesDemo2.setVisible(true);
     
     startThread();
   }

   public void run(){
	   
	   String fetalfilestr = "D:/lianProject/new_data/40/190114112829/190114112829.fetal";
	   
	   int[] testfhrlist = FileData.readFHRfromFile(fetalfilestr);
	   ArrayList<Integer> fhrlistIntact = FileData.intArray_to_listArray(testfhrlist);
	   
	   
	   FhrAnalyse fhrAnalysetool = new FhrAnalyse();
	   ArrayList<Integer> fhrbaseline = fhrAnalysetool.getFhrBaseline(fhrlistIntact, 3.2f);
	   // 实时可以在这里调用
	   for(int i = 0; i < fhrlistIntact.size(); i++){
		   
		   Millisecond millisecond1 = new Millisecond();
		   timeseries1.add(millisecond1, fhrlistIntact.get(i));
		  
		   Millisecond millisecond2 = new Millisecond();
		   timeseries2.add(millisecond2, fhrbaseline.get(i));
		   System.out.print(fhrlistIntact.get(i) + " ");
		   try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }

   }

   public static void startThread(){
     thread1.start();
   }

   public void actionPerformed(ActionEvent e){
     if(e.getActionCommand().equals("EXIT")){
    	 
       thread1.interrupt();
       System.exit(0);
     }
   }


   @SuppressWarnings("deprecation")
public TimeSeriesDemo2(){
     thread1 = new Thread(this);
     originalValue1 = 0D;
     originalValue2 = 0D;
     //创建时序图对象
     timeseries1 = new TimeSeries("胎心率曲线",TimeSeriesDemo2.class$org$jfree$data$time$Millisecond != null ? TimeSeriesDemo2.class$org$jfree$data$time$Millisecond : (TimeSeriesDemo2.class$org$jfree$data$time$Millisecond = TimeSeriesDemo2.getClass("org.jfree.data.time.Millisecond")));
     timeseries2 = new TimeSeries("基线",TimeSeriesDemo2.class$org$jfree$data$time$Millisecond != null ? TimeSeriesDemo2.class$org$jfree$data$time$Millisecond : (TimeSeriesDemo2.class$org$jfree$data$time$Millisecond = TimeSeriesDemo2.getClass("org.jfree.data.time.Millisecond")));
     
     TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
     timeSeriesCollection.addSeries(timeseries1);
     timeSeriesCollection.addSeries(timeseries2);
     //创建jfreechart对象
     JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("胎监图实时曲线","Time","Value",
    		 timeSeriesCollection,true,true,false);
     jfreechart.setBackgroundPaint(Color.WHITE);
     configFont(jfreechart);
     //设定显示风格
     XYPlot xyplot = jfreechart.getXYPlot();
        
     
     xyplot.setBackgroundPaint(Color.lightGray);
     xyplot.setDomainGridlinePaint(Color.white);
     xyplot.setRangeGridlinePaint(Color.white);

     ValueAxis valueaxis = xyplot.getDomainAxis();
     valueaxis.setAutoRange(true);
     valueaxis.setFixedAutoRange(60000D);
     //设定Value的范围
     valueaxis = xyplot.getRangeAxis();
     valueaxis.setRange(100D,200D);
   //  xyplot.setDataset(2, timeseriescollection1);
     xyplot.setRenderer(1,new DefaultXYItemRenderer());

     //创建图表面板
     ChartPanel chartpanel = new ChartPanel(jfreechart);
     getContentPane().setSize(300, 100);
     getContentPane().add(chartpanel);
     
     //根据需要添加操作按钮
     this.setTitle("胎心率实时曲线");
     JPanel jpanel = new JPanel();
     jpanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));//边距为4
     JButton jbutton = new JButton("退出");
     jbutton.setActionCommand("EXIT");
     jbutton.addActionListener(this);
     jpanel.add(jbutton);
     getContentPane().add(jpanel,"South");
     chartpanel.setPreferredSize(new Dimension(1500,300));
     
   }

   static Class getClass(String s){
     Class cls = null;
     try{
       cls = Class.forName(s);
     }catch(ClassNotFoundException cnfe){
       throw new NoClassDefFoundError(cnfe.getMessage());
     }
     return cls;
   }

   /**
    * 配置字体 
    * @param chart JFreeChart 对象
    */
   private void configFont(JFreeChart chart){
   	// 配置字体
   	Font xfont = new Font("宋体",Font.PLAIN,12) ;// X轴
   	Font yfont = new Font("宋体",Font.PLAIN,12) ;// Y轴
   	Font kfont = new Font("宋体",Font.PLAIN,12) ;// 底部
   	Font titleFont = new Font("隶书", Font.BOLD , 25) ; // 图片标题
   	XYPlot plot = chart.getXYPlot();// 图形的绘制结构对象
   	
   	// 图片标题
   	chart.setTitle(new TextTitle(chart.getTitle().getText(),titleFont));
   	
   	// 底部
   	chart.getLegend().setItemFont(kfont);
   	
   	// X 轴
   	ValueAxis domainAxis = plot.getDomainAxis();
       domainAxis.setLabelFont(xfont);// 轴标题
       domainAxis.setTickLabelFont(xfont);// 轴数值  
       domainAxis.setTickLabelPaint(Color.BLUE) ; // 字体颜色
//       domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // 横轴上的label斜显示 
       
   	// Y 轴
   	ValueAxis rangeAxis = plot.getRangeAxis();   
       rangeAxis.setLabelFont(yfont); 
       rangeAxis.setLabelPaint(Color.BLUE) ; // 字体颜色
       rangeAxis.setTickLabelFont(yfont);  
       
   }
}
