import com.alibaba.fastjson.TypeReference;
import com.laoyang.common.util.R;
import org.junit.jupiter.api.Test;

import java.util.*;

public class RTest {

    public static void main(String[] args) {

    }
    @Test
    public void Rtest(){
        R<List> r= new R<>();
        r.put("data", Arrays.asList(1, 3, 2, 4,0));
        TypeReference<TreeSet<Integer>> type = new TypeReference<TreeSet<Integer>>() {
        };
        Set<Integer> date = r.getDate(type);
        System.out.println(date);
        System.out.println(r);
    }
}
