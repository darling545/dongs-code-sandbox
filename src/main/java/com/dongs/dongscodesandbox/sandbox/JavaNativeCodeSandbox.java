package com.dongs.dongscodesandbox.sandbox;


import com.dongs.dongscodesandbox.model.ExecuteCodeRequest;
import com.dongs.dongscodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;


/**
 * Java原生代码沙箱实现（直接复用模板方法）
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandBoxTemplate{

    /**
     * 执行代码
     * @param executeCodeRequest 执行代码请求类
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
