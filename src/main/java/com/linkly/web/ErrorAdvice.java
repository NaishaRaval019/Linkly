package com.linkly.web;

import com.linkly.model.LinkException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(LinkException.class)
    public ModelAndView handle(LinkException e) {
        ModelAndView mv = new ModelAndView("error");
        mv.setStatus(HttpStatusCode.valueOf(e.getStatus()));
        mv.addObject("status", e.getStatus());
        mv.addObject("message", e.getMessage());
        return mv;
    }
}
