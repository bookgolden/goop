package io.renren;

import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Lists;
import io.swagger.models.auth.In;
import org.apache.commons.collections.ListUtils;

import java.util.*;

public class a {
    public static void main(String[] args) {
//        a w = new a();
//        System.out.println(w.isValid("()"));
        int[] height = new int[]{0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        height = new int[]{5, 2, 1, 2, 1, 5};

        /**
         * 栈空、从1开始遍历、直到当前数比上个数小、将上个数压栈、
         * 栈非空、遍历下一个数、直到比当前数小、弹栈与当前数处理
         */
        Stack<Integer> stack = new Stack<>();
        int val = 0;
        int left = 0;
        for (int i = 1; i < height.length - 1; i++) {
            int cur = height[i];
            if (stack.isEmpty()) {
                //栈空、
                if (cur < height[i - 1]) {
                    stack.push(height[i - 1]);
                    left = i - 1;
                }
            } else {
                if (cur > height[i + 1]) {
                    //TODO 计算面积
                    val += area(height, left, i);
                    stack.pop();
                    left = 0;
                }
            }
        }
        if (!stack.isEmpty() && left == 0) {
            val += area(height, left, height.length - 1);
        }
        System.out.println(val);
    }

    static int area(int[] arr, int left, int right) {
        int area = 0;
        int max = Math.min(arr[left], arr[right]);
        for (int i = left; i <= right; i++) {
            if (max > arr[i]) {
                area += max - arr[i];
            }
        }
        return area;
    }


    public boolean isValid(String s) {
        Stack<String> str = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char tmp = s.charAt(i);
            if (" ".equals(tmp)) {
                continue;
            }

            if ('(' == tmp || '[' == tmp || '{' == tmp) {
                str.push(tmp + "");
            } else {
                char vone = vone(tmp);
                if (vone == tmp) {
                    str.pop();
                }
            }
        }
        return str.isEmpty();
    }

    Stack<Character> s = new Stack<>();

    private char vone(char tmp) {
        switch (tmp) {
            case '}':
                return '{';
            case ']':
                return '[';
            case ')':
                return '(';
            default:
                return ' ';
        }
    }
}


class CQueue {

    Stack<Integer> stack1 = new Stack();     //做存储
    Stack<Integer> stack2 = new Stack();     //做转移

    public CQueue() {

    }

    public void appendTail(int value) {
        stack2.push(value);
    }

    public int deleteHead() {
        if (stack1.isEmpty()) {
            if (stack2.isEmpty()) {
                return -1;
            } else {
                while (!stack2.isEmpty()) {
                    stack1.push(stack2.pop());
                }
                return stack1.pop();
            }
        }
        return stack1.pop();
    }
}

class c {

    public static void main(String[] args) {
        System.out.println(new c().removeDuplicates("abbaca"));
    }

    public String removeDuplicates(String S) {
        boolean flag = true;
        char[] chars = S.toCharArray();
        while (flag) {
            flag = false;
            int j = 0;
            for (int i = 0; i < chars.length - 1 - j; ) {
                if (chars[i] == chars[i + 1] && chars[i] != ' ') {
                    if (i + 2 == chars.length) {
                        chars[i] = chars[i + 1] = ' ';
                        break;
                    } else {
                        System.arraycopy(chars, i + 2, chars, i, chars.length - i - 2);
                        chars[chars.length - 1] = chars[chars.length - 2] = ' ';
                        j += 2;
                    }
                    flag = true;
                } else {
                    i++;
                }
            }
        }
        return new String(chars).trim();
    }
}

class d {
    public static void main(String[] args) {
        int[] ints = new d().nextGreaterElement(new int[]{2, 4}, new int[]{1, 2, 3, 4});
        System.out.println(Arrays.toString(ints));
    }

    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        int[] val = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            int cur = nums1[i];

            int of = ArrayUtil.indexOf(nums2, cur);
            boolean flag = true;
            while (of < nums2.length) {
                if (nums2[of] > cur) {
                    val[i] = nums2[of];
                    flag = false;
                    break;
                }
                of++;
            }
            if (flag) {
                val[i] = -1;
            }
        }
        return val;
    }
}

class MinStack {

    Stack<Integer> stack = new Stack<>();
    Stack<Integer> midStack = new Stack<>();

    public MinStack() {

    }

    public void push(int x) {
        stack.push(x);
        if (!midStack.isEmpty() && x < midStack.peek()) {
            midStack.push(x);
        }
    }

    public void pop() {
        if (stack.pop() == midStack.peek()) {
            midStack.pop();
        }
    }

    public int top() {
        if (stack.isEmpty()) return -1;
        return stack.peek();
    }

    public int getMin() {
        return midStack.peek();
    }
}


class MyQueue {

    Stack<Integer> stack1 = new Stack<>();  //存储
    Stack<Integer> stack2 = new Stack<>();  //辅助

    /**
     * Initialize your data structure here.
     */
    public MyQueue() {

    }

    /**
     * Push element x to the back of queue.
     */
    public void push(int x) {
        while (!stack2.isEmpty()) {
            stack1.push(stack2.pop());
        }
        stack1.push(x);
    }

    /**
     * Removes the element from in front of queue and returns that element.
     */
    public int pop() {
        while (!stack1.isEmpty()) {
            stack2.push(stack1.pop());
        }
        return stack2.pop();
    }

    /**
     * Get the front element.
     */
    public int peek() {
        while (!stack1.isEmpty()) {
            stack2.push(stack1.pop());
        }
        return stack2.peek();
    }

    /**
     * Returns whether the queue is empty.
     */
    public boolean empty() {
        return stack1.isEmpty() && stack2.isEmpty();
    }
}


class q {

    //"y#fo##f"
    //"y#f#o##f"
    public static void main(String[] args) {
        new q().backspaceCompare("y#fo##f", "y#f#o##f");
        new q().spiralOrder(new int[][]{
//                {1, 2, 3, 4},
//                {5, 6, 7, 8},
//                {9, 10, 11, 12}
                {7},{9},{6}
        });
    }

    public boolean backspaceCompare(String S, String T) {
        Stack<Character> stack1 = new Stack<>();
        Stack<Character> stack2 = new Stack<>();

        for (char c : S.toCharArray()) {
            if (c == '#') {
                if (!stack1.isEmpty()) {
                    stack1.pop();
                }
            } else {
                stack1.push(c);
            }
        }

        for (char c : T.toCharArray()) {
            if (c == '#') {
                if (!stack2.isEmpty()) {
                    stack2.pop();
                }
            } else {
                stack2.push(c);
            }
        }
        return stack1.toString().equals(stack2.toString());
    }

    /**
     * 输入：matrix = [
     *      [1,2,3,4],  i=0,j+
     *      [5,6,7,8],  j=len,i++
     *      [9,10,11,12]
     *  ]
     * 输出：[1,2,3,4,8,12,11,10,9,5,6,7]
     * @param matrix
     * @return
     */
    /**
     * ilen = 3 jlen=4
     * i = 0, j++=len-1
     * j = len-1, i++=len-1
     * i = len-1, j--=0
     * j = 0, i--=0+1
     * <p>
     * ilen-2 =1、jlen-2=2
     * <p>
     * i = 0+1, j++=len-2
     * j = len-2, i++=len-2
     *
     * @param matrix
     * @return
     */
    public int[] spiralOrder(int[][] matrix) {
        if(ArrayUtil.isEmpty(matrix)){
            return new int[0];
        }
        int ilen = matrix.length;
        int jlen = matrix[0].length;

        if(ilen ==1){
            return matrix[0];
        }

        int[] array = new int[ilen * jlen];
        int ind = 0;
        List<Integer> list = new ArrayList<>();
        for (int index = 0, i = 0, j = 0; i < ilen && j < jlen; index++) {
            boolean flagL = true;
            int r = 0;
            //右
            i = j = index;
            while (j < jlen) {
                array[ind++] = matrix[i][j];
                j++;
                r++;
            }
            //下
            i = index + 1;
            j = jlen - 1;
            while (i < ilen) {
                array[ind++] =matrix[i][j];
                i++;
                flagL = false;
            }
            if (!flagL) {
                //左
                i = ilen - 1;
                j = jlen - 1 - 1;
                while (j > index - 1) {
                    array[ind++] =matrix[i][j];
                    j--;
                }
            }

            if(r > 1){
                //上
                i = ilen - 1 - 1;
                j = index;
                while (i > index) {
                    array[ind++] =matrix[i][j];
                    i--;
                }
            }

            ilen--;
            jlen--;
        }


        return array;
    }
}

