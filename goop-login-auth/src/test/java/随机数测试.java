import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.laoyang.common.util.R;

/**
 * @author yyy
 * @Date 2020-07-03 11:06
 * @Email yangyouyuhd@163.com
 * @Note
 */
public class 随机数测试 {


    public static void main(String[] args) {
//        System.out.println(RandomUtil.randomInt(6));

        String code = RandomUtil.randomNumbers(6);
        System.out.println(code);

        System.out.println(RandomUtil.randomStringUpper(6));


        R r = new R();
        System.out.println(JSON.toJSONString(r));
    }
}
