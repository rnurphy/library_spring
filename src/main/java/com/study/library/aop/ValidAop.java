package com.study.library.aop;

import com.study.library.dto.OAuth2SignupReqDto;
import com.study.library.dto.SignupReqDto;
import com.study.library.exception.ValidException;
import com.study.library.repository.UserMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Aspect
@Component
public class ValidAop {

    @Autowired
    private UserMapper userMapper;

    @Pointcut("@annotation(com.study.library.aop.annotation.ValidAspect)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        String methodName = proceedingJoinPoint.getSignature().getName();

        Object[] args = proceedingJoinPoint.getArgs();  // dto랑 bindingresult(BeanPropertyBindingResult) 들어있음

        BeanPropertyBindingResult bindingResult = null;

        for(Object obj : args) {
            if(obj.getClass() == BeanPropertyBindingResult.class) {
                bindingResult = (BeanPropertyBindingResult) obj;
            }
        }

        if(methodName.equals("signup")) {
            SignupReqDto signupReqDto = null;

            for(Object obj : args) {
                if(obj.getClass() == SignupReqDto.class) {
                    signupReqDto = (SignupReqDto) obj;
                }
            }

            if(userMapper.findUserByUsername(signupReqDto.getUsername()) != null) {
                ObjectError objectError = new FieldError("username", "username", "이미 존재하는 사용자 이름입니다.");
                bindingResult.addError(objectError);
            }
        }

        if(methodName.equals("oAuth2Signup")) {
           OAuth2SignupReqDto oAuth2SignupReqDto = null;

            for(Object obj : args) {
                if(obj.getClass() == OAuth2SignupReqDto.class) {
                    oAuth2SignupReqDto = (OAuth2SignupReqDto) obj;
                }
            }

            if(userMapper.findUserByUsername(oAuth2SignupReqDto.getUsername()) != null) {
                ObjectError objectError = new FieldError("username", "username", "이미 존재하는 사용자 이름입니다.");
                bindingResult.addError(objectError);
            }
        }

        if(bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            Map<String, String> errorMap = new HashMap<>();

            for(FieldError fieldError : fieldErrors) {
                String fieldName = fieldError.getField();
                String message = fieldError.getDefaultMessage();

                errorMap.put(fieldName, message);
            }

            throw new ValidException(errorMap);
        }

        return proceedingJoinPoint.proceed();
    }
}
