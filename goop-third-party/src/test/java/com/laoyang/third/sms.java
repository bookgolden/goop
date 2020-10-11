package com.laoyang.third;

import com.laoyang.third.component.SmsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yyy
 * @Date 2020-07-03 9:08
 * @Email yangyouyuhd@163.com
 * @Note
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class sms {

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void aaa() {
        try {
            smsComponent.smsSend("1853131321", "code");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
