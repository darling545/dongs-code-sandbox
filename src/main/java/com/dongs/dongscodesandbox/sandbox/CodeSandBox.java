package com.dongs.dongscodesandbox.sandbox;

import com.dongs.dongscodesandbox.model.ExecuteCodeRequest;
import com.dongs.dongscodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口
 *
 * @author dongs
 */
public interface CodeSandBox {


    /**
     * 执行代码
     * @param executeCodeRequest 执行代码请求类
     * @return 执行代码响应类
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
