package com.example.ecapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {
  @GetMapping("/test")
  public String showTask(Model model) {
    model.addAttribute("title", "Sample");
    model.addAttribute("message", "message内容");
      return "test";
  }
  @GetMapping("/hello")
  public String Hello(Model model) {
      model.addAttribute("title", "hello");
      return "hello";
  }
  
  
  
}
