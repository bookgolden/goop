package com.laoyang.product;

import io.swagger.models.auth.In;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@Data
public class vo {
    private List<Object> list;

    public void setList(List<Object> list) {
        this.list = list;
    }

    @Test
    void name() {
        vo v = new vo();
        List<vo> attrs = new ArrayList<>();
//        v.setList(attrs);
    }


    @Test
    public void sum() {
        System.out.println(s(10));
        System.out.println(aaa(10));
    }

    private int aaa(int n) {
        return (n + 1) * (n / 2);
    }

    private int s(int n) {
        return ss(n, 0);
    }

    private int ss(int n, int sum) {
        if (n == 1) {
            return sum + n;
        }
        return ss(n - 1, sum + n);
    }


    /**
     * 数组的每个索引作为一个阶梯，第 i个阶梯对应着一个非负数的体力花费值 cost[i](索引从0开始)。
     * 每当你爬上一个阶梯你都要花费对应的体力花费值，然后你可以选择继续爬一个阶梯或者爬两个阶梯。
     * 您需要找到达到楼层顶部的最低花费。在开始时，你可以选择从索引为 0 或 1 的元素作为初始阶梯。
     *
     * @param cost
     * @return
     */
    public int minCostClimbingStairs(int[] cost) {
        /**
         *  能爬 1 or 2
         *  i >= len 结束
         */
        int len = cost.length;
        int[] arr = new int[len];
        //min = Main.main(f[len-1],f[len-2]);
        arr[0] = cost[0];
        arr[1] = cost[1];
        for (int i = 2; i < len; i++) {
            arr[i] = Math.min(arr[i - 1], arr[i - 2]) + cost[i];
        }
        return Math.min(arr[len - 1], arr[len - 2]);
    }


    @Test
    public void min() {
        int[] arr = new int[]{0, 1, 1, 0};
        System.out.println(minCostClimbingStairs(arr));
        arr = new int[]{-2, -3};
        maxSubArray(arr);
    }

    /**
     * 输入一个整型数组，数组里有正数也有负数。
     * 数组中的一个或连续多个整数组成一个子数组。求所有子数组的和的最大值。
     *
     * @param nums
     * @return
     */
    public int maxSubArray(int[] nums) {
        int len = nums.length;
        int[] arr = new int[len];
        arr[0] = nums[0];
        int max = nums[0];
        for (int i = 1; i < len; i++) {
            arr[i] = Math.max(nums[i], arr[i - 1] + nums[i]);
            max = Math.max(max, arr[i]);
        }
        return max;
    }




    /**
     * 不能连续偷、要跳着
     * 有10家、 最多偷5家
     *
     * @param nums
     * @return
     */
    public int rob(int[] nums) {
        int len = nums.length;
        int[] arr = new int[len];
        //min = Main.main(f[len-1],f[len-2]);
        arr[0] = nums[0];
        arr[1] = Math.max(nums[1], nums[0]);

        for (int i = 2; i < len; i++) {
            arr[i] = Math.max(arr[i-2] + nums[i],arr[i-1]);
        }
        return Math.max(arr[len - 1], arr[len - 2]);
    }

    @Test
    public void max(){
        int[] arr= new int[]{2,7,9,3,1};
        System.out.println(rob(arr));
    }
}
