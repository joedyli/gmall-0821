package com.atguigu.gmall.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {

    @GetMapping("confirm")
    public String confirm(Model model){



        return "trade";
    }
}
