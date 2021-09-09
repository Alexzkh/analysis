package com.zqykj.advice;

import com.zqykj.infrastructure.common.dto.ErrorInfo;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@ControllerAdvice(annotations = {RestController.class})
@Slf4j
public class GlobalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ErrorInfo<String> errorHandler(HttpServletRequest request, Exception ex) throws Exception {

        ErrorInfo<String> info = new ErrorInfo<String>();

        info.setCode(ErrorInfo.ERROR);
        info.setMessage(ex.getMessage());
        info.setData("Do Not Have Return Data");
        info.setUrl(request.getRequestURL().toString());

        return info;
    }
}
