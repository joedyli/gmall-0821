package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl", defaultValue = "http://www.gmall.com")String returnUrl, Model model){
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(
            @RequestParam("returnUrl")String returnUrl,
            @RequestParam("loginName")String loginName,
            @RequestParam("password")String password,
            HttpServletRequest request,
            HttpServletResponse response
        ){
        this.authService.login(loginName, password, request, response);
        return "redirect:" + returnUrl;
    }
}
