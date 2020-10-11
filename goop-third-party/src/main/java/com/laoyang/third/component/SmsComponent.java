package com.laoyang.third.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * @author yyy
 * @Date 2020-07-03 8:58
 * @Email yangyouyuhd@163.com
 * @Note
 */

@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String sign;
    private String skin;
    private String appcode;

    public void smsSend(String phone, String code) {


        // 【1】请求地址 支持http 和 https 及 WEBSOCKET
//        String host = "https://smsmsgs.market.alicloudapi.com";
        // 【2】后缀
//        String path = "/sms/";
        // 【3】开通服务后 买家中心-查看AppCode
//        String appcode = "5acb3f67e8bd4411998985f2d5d7846c";
        // 【4】请求参数，详见文档描述
//        String phone = "18571658045";
//        String sign = "175622";
//        String skin = "1";


        // 【5】拼接请求链接
        String urlSend = host + path + "?code=" + code + "&phone=" + phone + "&sign=" + sign + "&skin=" + skin;
        if (urlSend != null) {
            System.out.println(urlSend);
            return;
        }
        try {
            URL url = new URL(urlSend);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Authorization", "APPCODE " + appcode);// 格式Authorization:APPCODE (中间是英文空格)
            int httpCode = httpURLConnection.getResponseCode();
            if (httpCode == 200) {
                String json = read(httpURLConnection.getInputStream());
                System.out.println("正常请求计费(其他均不计费)");
                System.out.println("获取返回的json");
                System.out.print(json);
            } else {
                Map<String, List<String>> map = httpURLConnection.getHeaderFields();
                String errorMessage = map.get("X-Ca-Error-Message").get(0);
                if (httpCode == 400 && errorMessage.equals("Invalid AppCode `not exists`")) {
                    System.out.println("AppCode错误 ");
                } else if (httpCode == 400 && errorMessage.equals("Invalid Url")) {
                    System.out.println("请求的 Method、Path 或者环境错误");
                } else if (httpCode == 400 && errorMessage.equals("Invalid Param Location")) {
                    System.out.println("参数错误");
                } else if (httpCode == 403 && errorMessage.equals("Unauthorized")) {
                    System.out.println("服务未被授权（或URL和Path不正确）");
                } else if (httpCode == 403 && errorMessage.equals("Quota Exhausted")) {
                    System.out.println("套餐包次数用完 ");
                } else {
                    System.out.println("参数名错误 或 其他错误");
                    System.out.println(errorMessage);
                }
            }

        } catch (MalformedURLException e) {
            System.out.println("URL格式错误");
        } catch (UnknownHostException e) {
            System.out.println("URL地址错误");
        } catch (Exception e) {
            // 打开注释查看详细报错异常信息
            // e.printStackTrace();
        }

    }

    /*
     * 读取返回结果
     */
    public String read(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = new String(line.getBytes(), "utf-8");
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
