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
			in = new FileInputStream(filename);//fatalfilename为fatal文件的路径
			//BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
			datain = new DataInputStream(in);
			
			byte[] buf = new byte[8]; 
			datain.read(buf);//跳过前面8个字节
			//datain.readUnsignedByte();
			
		    while (true) { 
		    	byte[] ctg = new byte[5];//这里ctg为5个字节的数组，用来存每个时间点的胎监信息
		        int len = datain.read(ctg);//读入5个字节的信息
		        
		        if (len < 0)  
		            break; 
		        
		        int fhr = ctg[0]&0x0FF;//转为无符号数据，这个应该不影响什么 
		        int uc = ctg[2]&0x0FF;//转为无符号数据
		        int fhr2 = ctg[1]&0x0FF;//其实这里是双胎情况下才会有的，双胎的话，第二个胎儿的胎心率存在第2个字节里
		        
		        MyCTG myctg = new MyCTG();//myctg是我定义的结构体，存一下每个信息块的内容
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
		        fetaldatalist.add(myctg);//将这个时间点的胎儿信息存到list里边
		        
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
			in = new FileInputStream(filename);//fatalfilename为fatal文件的路径
			//BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
			datain = new DataInputStream(in);		
			//byte[] buf = new byte[1]; 
			//datain.read(buf);//跳过前面8个字节
			//datain.readUnsignedByte();
			
		    while (true) { 
		    	byte[] data = new byte[1];//这里ctg为5个字节的数组，用来存每个时间点的胎监信息
		        int len = datain.read(data);//读入5个字节的信息	        
		        if (len < 0)  
		            break; 
		        movedatas.add(data[0]&0x0FF);//将这个时间点的胎儿信息存到list里边     
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