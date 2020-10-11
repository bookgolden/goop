package com.sso.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @dataTotal 数据量条
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoUrl;

    @GetMapping("emps")
    public String emps(Model model) {
        List<String> emps = Arrays.asList("张三", "李四", "王五");
        model.addAttribute("emps", emps);
        return "emps";
    }
}
