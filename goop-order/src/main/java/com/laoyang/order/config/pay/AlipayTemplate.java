package com.laoyang.order.config.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016092200568607";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC/KSzFp8+PjnW2voT2LTSCSuvQCclNGFUbVWFAojVhyJphcA7plWpwC1KY6/fzo4q2MDt9YbPwVLEVG4tZBipbKKtuBnHYNxKLPEmUayEJO0I5o0dvttW0J6D5jC8+2qXv+iGO8b9eAsYnxNyG5r71YgCDWhVmeEC31fQvintZxQGQRgpOAFY2EsHdwi0YnxKh2INw2iW0J5cfAlAYY4+xkVRve7xMYf9tXGKXTfhOx5VVDZFYfeCa/1hs5eE66VlwIZ1FBdb4NwA+OdY8ax8a9mwymN23Cy3UBAnKBc9WclVwiX21a8MJDvbTavnrAO7zUAzbEZhDml9OEuS7M777AgMBAAECggEAAKXFDw5Yd6YF5A6VKLhSezaBwfymf+pbL3KEl9F02NXzH+1fP49QAv0m1HnIJz0glSloqi4Qi/ndd7LWMAEDV7e1jPPcotzc5TfZD/Wk8QEQYl2ZOlmmM6wGsZG1/0KCWWPBhVhwS4M65JuYG6TgdLhWRBukAurZXUYOQ7L2ZAFzVGdVRswTjd9qOUgaChfjrCfqWXUIC9Rf1xorK6WaZghIfHl/qoU38WgmrqBzXLinLpxRSexb2/jp93dp8jAp58Z/HACjNcA6ctVRxePm6JnCkJ/RTKamRd2E66gqfKnBj6nR3liSSOUp1Zj7VvNoykEDSUA3M3ITwXwbV1/rIQKBgQDz47eEYdSy8JiS4kkZdwjn47eVCK6R44hTFNQ8V35ZU0qLMYWDFnWZguYnnC5qudPt1MROuEhUIBDQ/fVOb+bhnLXLYNH2R7XCJW2C6nAjldHMLQL5AIqVtq86YFrAz2vj0RYN0NiF9VQ9uDjLTyJrjqiKt3GnQHydE5laOKtj+QKBgQDIpy4G7Oqn8VUr32wFkYZK+pFbht28VeckWuP1+/ecRB77G2J1JxDDwNb+HrfIMcNVUryxDQMPjswBOpvq5EChaN4dbJEJwUv3rvnAoMUEKm0VM0VCuyyAdTXsCdtSwrV256kFnJKC4aCDLwL5pDsQd1UC/P36oMcKOqIaGUvPkwKBgCxu/Gxd9oZzVxjLqU2+Zhz5W+UHI+bocVnRnakwt5BGoHojl5N+cAZ71rq4taw0Fuxpqyo8YNES1n19B0g3EvVfu69GAHrCLdclQoJre+q/zO1J9hSviJAmLF6SV3ZxU9K5sS7rMceWHrbv7Uj9LeQypXpAWbCDCzEC9Rl+4ZrhAoGBAJVUhla1H3ZZjy+VmYkTYsSOcnuRhZhSvjE6S4mYOhFcSUpMeRwDNODataZuKgydrGflqXSC4JJFjeIkQkGwGcGGkDeJwu9loin1WP5ZKAcPpBsxCwy8zEPV94lxVH7lVbPmeM4qVod9b73x0N8FsMdyaBxPOJ275iXguR9/UAJtAoGBAIlQH4exdxzFGrhaox5Y5uOpf76QiocMaQYRHMH2oaMaRxaVz/P945j/wY8r+KyH10CjPSb+yJouscObqKihfJ3g1FDL+c5pEEeWpd0GiA/gu5yJBHLfrnAvijbBJxXNBXn2gZpoe4q7AIzWIA2euZS3f1Bf8Z3J/Cuc2+2LkNqN";    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    // 支付宝公钥
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqyM+nNTYpqJtteMPHbYtak+UocnYjIKWfOJNEkLuaqpxgcrzr2gSCn3CjYn/jp0SUDXuMnNaB5F+6u8Kqy9A0XnCdPWC0f4BgoNaJ1IXXhExMUivs5+GVXsY2cLeBI2qPGIz6hJT0Df3bf5MGWM8V1im4Qcd2FjM60sVBsTsvy44+KMPuCuEY4NMj4BFLyyw4++gx8KArECEUALHaGYmtukW/+cmbC57ZQ89gKg52oYyyMuwXMROS6U6YxytjR3PjGj7um3nv5du1iOcptO060JbFMdWUFUtrIkg1SZVA/CEk5vOf7dZLAnej5MpPRKpEzG0EGVqtnNEymyvlDsYVwIDAQAB";    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问

    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    private String timeout = "30m";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);
        return result;

    }
}
