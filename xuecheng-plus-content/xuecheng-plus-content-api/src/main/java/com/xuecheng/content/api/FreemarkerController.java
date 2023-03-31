package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Ke_han
 * @creat 2023-03-31 12:31
 * Freemarker入门
 */
@Controller
public class FreemarkerController {


    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        //指定模板
        modelAndView.setViewName("test");//添加配置文件中的后缀，.ftl找到test.ftl
        //指定模型
        modelAndView.addObject("name","小明");

        return modelAndView;
    }

}
