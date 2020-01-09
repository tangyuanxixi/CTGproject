import java.lang.reflect.Array;
import java.util.Arrays;
public class TestArray {

	
	
	public static void sort(int[] A,int N){
		int j,t;
		int i;
		for(i=1;i<N;i++){
			
			t=A[i];
			j=i-1;
			while(j>=0 && A[j]>t){
				A[j+1] = A[j];
				j--;
			}
			A[j+1] =t;
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		int[] arr = {1,3,2,7,5,9};
//		arr[0] = 2;
//		arr[]
//		Arrays.sort(arr);
		
		sort(arr,4);
		
//		System.out.println(arr);
		for(int i=0;i<arr.length;i++){
			System.out.println(arr[i]);
		}
		
		float[] testa = new float[5];
		System.out.println(testa[0]);

	}

}
