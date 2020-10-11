package com.laoyang.third;

import com.aliyun.oss.OSSClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author yyy
 * @Date 2020-07-03 8:53
 * @Email yangyouyuhd@163.com
 * @Note
 */
@SpringBootApplication
public class ThirdMain {

    public static void main(String[] args) {
        SpringApplication.run(ThirdMain.class,args);
        System.out.println("\n\n\n---THIRD PARTY SUCCESS---\n\n\n");
    }

    @Resource
    OSSClient ossClient;

    @GetMapping("/upload")
    public boolean fileUpload() throws FileNotFoundException {
        String path = "c:\\users\\yyy\\Pictures\\壁纸\\长江.jpeg";
        FileInputStream stream = new FileInputStream(path);
        ossClient.putObject("goop","长江.jpeg",stream);
        ossClient.shutdown();
        return true;
    }
}
