package io.renren;

import net.bytebuddy.utility.RandomString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.IntFunction;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RenrenApplicationTests {

    @Test
    public void contextLoads() {
        System.out.println(RandomString.make());
        System.out.println("你好世界");
    }

    @Test
    public void sortString() {
        String s = "leetcode";//cdelotee
		s = "spo";		//ops
        int[] arr = new int[26];
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            arr[chars[i] - 97]++;
        }
		System.out.println(Arrays.toString(arr));
        //[4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        StringBuffer stringBuffer = new StringBuffer();
        boolean flag = false;
        while (!flag) {
        	flag = true;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] != 0) {
                    stringBuffer.append((char) (97 + i));
                    arr[i]--;
                	flag = false;
                }
            }

            for (int i = arr.length - 1; i > -1; i--) {
                if (arr[i] != 0) {
                    stringBuffer.append((char) (97 + i));
                    arr[i]--;
                    flag = false;
                }
            }
        }
		System.out.println(stringBuffer.toString());
    }

    public static void main(String[] args) {
        new RenrenApplicationTests().sortString();

    }

    @Test
	public void ss(){
    	int[] a = new int[]{1,2,2,1};
    	int[] b=  new int[]{2,2};
    	intersect(a,b);
	}

	public int[] intersect(int[] nums1, int[] nums2) {
    	ArrayList<Integer> list= new ArrayList<>();
		Arrays.sort(nums1);Arrays.sort(nums2);
		for (int i = 0,j=0; i < nums1.length; i++) {
			if(nums1[i] == nums2[j]){
				list.add(nums1[i]);
				j++;
			}
		}
		int[] res=  new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			res[i] = list.get(i);
		}
		return res;
	}

	@Test
	public int largestPerimeter(int[] A) {
		Arrays.sort(A);
		int len = A.length;
		for (int i = len-1; i >-1 ; i--) {
			int one = A[i];
			for (int j = i-1; j >-1 ; j--) {
				int two = A[j];
				for (int k = j-1; k >-1 ; k--) {
					if (A[k] + two> one) {
						return one+two+A[k];
					}
					break;
				}
			}
		}
    	return -1;
	}

    @Test
	public void average() {
		int[] salary = new int[]{48000,59000,99000,13000,78000,45000,31000,17000,39000,37000,93000,77000,33000,28000,4000,54000,67000,6000,1000,11000};
		double sum = 0;
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < salary.length; i++) {
			max = Math.max(max,salary[i]);
			min = Math.min(min,salary[i]);
			sum += salary[i];
		}
		sum -= max;
		sum -= min;
		double i = sum / (salary.length - 2);
		System.out.println(i);
	}


	public void hello(){
    	Collections.sort(null, new Comparator<HashMap<String, Object>>() {
			@Override
			public int compare(HashMap<String, Object> map1, HashMap<String, Object> map2) {
				return map1.get("key").hashCode() - map2.get("key").hashCode();
			}
		});


	}

}
