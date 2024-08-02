package com.dongs.dongscodesandbox.controller;


import com.dongs.dongscodesandbox.model.ExecuteCodeRequest;
import com.dongs.dongscodesandbox.model.ExecuteCodeResponse;
import com.dongs.dongscodesandbox.sandbox.JavaNativeCodeSandbox;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 代码沙箱入口
 */
@RestController("/")
public class MainController {


    // 鉴权请求头
    private static final String AUTH_REQUEST_HEADER = "auth";

    // 鉴权密钥
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;


    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 从请求头获取认证字段
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)){
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
