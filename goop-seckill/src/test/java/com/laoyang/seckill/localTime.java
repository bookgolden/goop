package com.laoyang.seckill;

import io.swagger.models.auth.In;
import org.junit.Test;
import org.springframework.web.servlet.DispatcherServlet;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author yyy
 * @Date 2020-07-22 20:15
 * @Email yangyouyuhd@163.com
 */
public class localTime {
    public static void main(String[] args) {
         LocalDate now = LocalDate.now();
         LocalDate plus = now.plusDays(2);
         LocalDateTime now1 = LocalDateTime.now();
         LocalTime now2 = LocalTime.now();

         LocalTime max = LocalTime.MAX;
         LocalTime min = LocalTime.MIN;

         LocalDateTime start = LocalDateTime.of(now, min);
         LocalDateTime end = LocalDateTime.of(plus, max);

        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

         System.out.println(now);
         System.out.println(now1);
         System.out.println(now2);
         System.out.println(plus);

         System.out.println(start);
         System.out.println(end);
        System.out.println(startStr);
        System.out.println(endStr);
    }


    @Test
    public void xx(){
        String str = "123456";
        System.out.println(str);
        System.out.println("\""+str+"\"");
    }


    /**
     *  初始空余瓶 == 0；
     *  初始能喝x瓶、
     *      产生x空瓶、
     *      并换x = (x+z)/y瓶
     *      余 z = (x+z)/y空瓶
     *  续喝x瓶、
     *      并换x = (x+z)/y瓶
     *      余z = (x+z)/y空瓶
     *  当 X == 0 时、
     *      结束交换、
//     * @param numBottles        初始瓶子数x
//     * @param numExchange       y个可换新瓶
     * @return
     */
    public int numWaterBottles() {
        int numBottles = 15, numExchange = 8;
        int total = 0;
        int z = 0;
        while (numBottles != 0){
            total += numBottles;
            int tmp = ( numBottles + z);
            numBottles = tmp/ numExchange;
            z = tmp %  numExchange;
        }
        System.out.println(total);

        if(numBottles >=numExchange){
            return (numBottles - numExchange) / ( numExchange - 1 ) + 1 + numBottles;
        }else {
          return   numBottles;
        }
//        return numBottles >= numExchange ? (numBottles - numExchange) / (numExchange - 1) + 1 + numBottles : numBottles;

        //        return ( numBottles * numExchange-1 )
//                  /(numExchange-1);
    }

    class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }


    public boolean isUnivalTree(TreeNode root) {
        int val = root.val;
        return isUnivalTree(root, val);
    }

    private boolean isUnivalTree(TreeNode root, int val) {
        if (root != null) {
            return root.val == val ? isUnivalTree(root.left, val) && isUnivalTree(root.right, val) : false;
        }else {
            return true;
        }
    }


    /**
     *  遍历数组、
     *      以 val-qty 存储
     *
     * @param arr
     * @return
     */
    public boolean uniqueOccurrences(int[] arr) {
        Map<Integer,Integer> map = new HashMap<>();
        for (Integer i : arr) {
            map.put(i,map.get(i) == null ? 1 : map.get(i) + 1);
        }
        List<Integer> list = map.values().stream().distinct().collect(Collectors.toList());
        boolean b = list.size() == map.keySet().size();
        System.out.println(b);
        return b;
    }

    @Test
    public void aa(){
        int[] arr  = new int[]{1,2};
//        uniqueOccurrences(arr);
        System.out.println('a'-0);
    }


    /**
     *  遍历words、
     *      word-->mos、
     *          遍历字符转换叠加
     *      mos存于set
     * @param words
     * @return
     */
    public int uniqueMorseRepresentations(String[] words) {
        String[] mos = new String[]{
                ".-","-...","-.-.","-..",".","..-.","--.","....",
                "..",".---","-.-",".-..","--","-.","---",".--.","--.-",".-.",
                "...","-","..-","...-",".--","-..-","-.--","--.."};
        Set<String> set = new HashSet<>();
        for (String word : words) {
            StringBuilder tmp = new StringBuilder();
            for (char c : word.toCharArray()) {
                tmp.append(mos[c -97]);
            }
            set.add(tmp.toString());
        }
        return set.size();
    }
    
}
