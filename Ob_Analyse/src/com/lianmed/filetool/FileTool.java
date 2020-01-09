package com.lianmed.filetool;



import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class FileTool {
	
	public List<MyCTG> readDataFromFile(String filename){
		List<MyCTG> fetaldatalist = new ArrayList<MyCTG>();
		FileInputStream in = null;
		DataInputStream datain = null;
		try{
			in = new FileInputStream(filename);//fatalfilenameΪfatal�ļ���·��
			//BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
			datain = new DataInputStream(in);
			
			byte[] buf = new byte[8]; 
			datain.read(buf);//����ǰ��8���ֽ�
			//datain.readUnsignedByte();
			
		    while (true) { 
		    	byte[] ctg = new byte[5];//����ctgΪ5���ֽڵ����飬������ÿ��ʱ����̥����Ϣ
		        int len = datain.read(ctg);//����5���ֽڵ���Ϣ
		        
		        if (len < 0)  
		            break; 
		        
		        int fhr = ctg[0]&0x0FF;//תΪ�޷������ݣ����Ӧ�ò�Ӱ��ʲô 
		        int uc = ctg[2]&0x0FF;//תΪ�޷�������
		        int fhr2 = ctg[1]&0x0FF;//��ʵ������˫̥����²Ż��еģ�˫̥�Ļ����ڶ���̥����̥���ʴ��ڵ�2���ֽ���
		        
		        MyCTG myctg = new MyCTG();//myctg���Ҷ���Ľṹ�壬��һ��ÿ����Ϣ�������
		        myctg.fhr = fhr;
		        myctg.fhr2 = fhr2;
		        if(uc >= 128)
		        {
		        	myctg.toco = (uc-128);
		        	myctg.td = 1;
		        }
		        else
		        {
		        	myctg.toco = uc;
		        	myctg.td = 0;
		        }
		        fetaldatalist.add(myctg);//�����ʱ����̥����Ϣ�浽list���
		        
		    }  
		    
		   
		}catch(Exception e){
			
		}finally{
			
			 try {
				datain.close();
				in.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
			 
		}		
		return fetaldatalist;		
	}
	
	
	public ArrayList<Integer> readDataFromMove(String filename){	
		ArrayList<Integer> movedatas = new ArrayList<Integer>();	
		FileInputStream in = null;
		DataInputStream datain = null;
		try{
			in = new FileInputStream(filename);//fatalfilenameΪfatal�ļ���·��
			//BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
			datain = new DataInputStream(in);		
			//byte[] buf = new byte[1]; 
			//datain.read(buf);//����ǰ��8���ֽ�
			//datain.readUnsignedByte();
			
		    while (true) { 
		    	byte[] data = new byte[1];//����ctgΪ5���ֽڵ����飬������ÿ��ʱ����̥����Ϣ
		        int len = datain.read(data);//����5���ֽڵ���Ϣ	        
		        if (len < 0)  
		            break; 
		        movedatas.add(data[0]&0x0FF);//�����ʱ����̥����Ϣ�浽list���     
		    }  		    		   
		}catch(Exception e){			
		}finally{		
			 try {
				datain.close();
				in.close();
			} catch (IOException e) {

				e.printStackTrace();
			}		 
		}				
		return movedatas;				
	}

}