package org.armada.galileo.sample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2022/2/8 8:34 上午
 */
@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping(value = "t1", method = {RequestMethod.POST, RequestMethod.GET})
    public String doSomething() {
        return new Date().toString();
    }
}
