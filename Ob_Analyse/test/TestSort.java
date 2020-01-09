
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.lianmed.analyse.FMData;;

class MyComparator implements Comparator<Integer[]> {		
	@Override
	public int compare(Integer[] o1, Integer[] o2) {
	
		if (o1[1] < o2[1]) {
			return 1;
		}
		else if (o1[1] > o2[1]) {
			return -1;
		} else {
			return 0;
		}
	}
}

public class TestSort {	
	//public void sort(int[][] ob,final)	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		Integer[] l1 = {1,3};
		Integer[] l2 = {2,4};
		Integer[] l3 = {4,6};
		List<Integer[]> testlist = new ArrayList<Integer[]>();
		
		testlist.add(l1);
		testlist.add(l2);
		testlist.add(l3);
		Integer[][] test1 = new Integer[3][2];
		test1[0] = l3;
		test1[1] = l1;
		test1[2] = l2;
		Comparator<Integer[]> c = new MyComparator();
		Arrays.sort(test1,c );	
		int a = 3;
		a = 4;	
		
		
		
		List<Integer> testl = new ArrayList<Integer>();
		testl.add(1);
		testl.add(3);
		testl.add(9);
		testl.add(6);
		
		int maxtest = Collections.max(testl);
		System.out.println(testl.indexOf(maxtest));
		float a = (float)40/16;
		System.out.println(a);
				
		*/
		/*
		List<Integer> testlist = new ArrayList<Integer>();
		testlist.add(2);
		testlist.add(4);
		testlist.add(6);
		
		List<Integer> testlist1 =  new ArrayList<Integer>(testlist);
		
		//int[] testarray = new int[3]{3,4,5};
		
		testlist.set(0, 10);
		testlist1.set(0, 55);
		
		//System.out.println(testlist.get(0));
		//System.out.println(testlist1.get(0));
		
		List<ArrayList> mytestlist = new ArrayList<ArrayList>();
		
		mytestlist.add(new ArrayList<Integer>());
		
		//mytestlist.get(0).a
		ArrayList[] mylist = new ArrayList[5];
		mylist[0] = new ArrayList<Integer>();
		mylist[0].add(2);
		
		mylist[0].add(4);
		
		mylist[0].add(6);
		
		if(mylist[0].contains(2)){
			
			System.out.println("hahaha");
		}
		if(mylist[0].contains("a")){
			
			System.out.println("ddddd");
		}
		*/
		/*
		List<Integer> testm = new ArrayList<Integer>();
		for(int i=0;i<10;i++){
			testm.add(5);
		}
		
		List<Integer> mylist1 = testm.subList(0, 5);
		
		System.out.println(mylist1.size());
		
		
		List<Integer> testm1 = new ArrayList<Integer>();
		System.out.println(Collections.max(testm1));
		*/
		
		/*
		List<FMData> testlist = new ArrayList<FMData>();
		for(int i=0;i<10;i++){
			FMData fm = new FMData(i,i,i,(float)i,i);
			testlist.add(fm);
		}
		
		List<FMData> testlist1 = new ArrayList<FMData>(testlist);
		
		for(int i=0;i<10;i++){
			
			if(testlist.get(i).fmstart==5){
				testlist1.set(i, 55);
			}
			
		}
		
		System.out.println(testlist.size());*/
		
		List<Integer> l1 = new ArrayList<Integer>();
		for(int i=0;i<10;i++){
			
			l1.add(i);
		}
		
		List<Integer> l2 = l1.subList(0, 6);//new ArrayList<Integer>(l1);

		l2.set(2, 55);
		
		System.out.println(l1.get(2));
		System.out.println(l2.get(2));
		
	}

}
