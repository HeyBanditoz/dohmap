//package io.banditoz.dohmap.controller;
//
//import io.banditoz.dohmap.model.BaseResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//public class RestAdvice {
//    @ExceptionHandler(value = {Exception.class})
//    public BaseResponse<?> base(Exception e, HttpServletRequest request, HttpServletResponse resp) {
//        return BaseResponse.error(HttpStatus.valueOf(resp.getStatus()).getReasonPhrase() + ", " + e.toString());
//    }
//}
