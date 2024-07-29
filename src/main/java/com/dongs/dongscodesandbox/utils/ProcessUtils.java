package com.dongs.dongscodesandbox.utils;


import cn.hutool.core.util.StrUtil;
import com.dongs.dongscodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 进程工具类
 *
 * @author dongs
 */
public class ProcessUtils {


    /**
     * 编译文件
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess,String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            // 开始计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待执行程序获取错误码
            int exitCode = runProcess.waitFor();
            executeMessage.setExitValue(exitCode);
            // 正常退出
            if (exitCode == 0){
                System.out.println(opName + "成功");
                // 运行正常输出流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((runProcess.getInputStream()), StandardCharsets.UTF_8));
                List<String> outputStrList = new ArrayList<>();
                // 进行逐行读取
                String compileOutLine;
                while((compileOutLine = bufferedReader.readLine()) != null){
                    outputStrList.add(compileOutLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList,"\n"));
            } else {
                // 异常退出
                System.out.println(opName + "失败,错误码:" + exitCode);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream(),StandardCharsets.UTF_8));
                List<String> outputStrList = new ArrayList<>();
                String compileOutLine;
                while ((compileOutLine = bufferedReader.readLine()) != null){
                    outputStrList.add(compileOutLine);
                }
                executeMessage.setMessage(StringUtils.join(compileOutLine,"\n"));
                // 分批获取错误输出
                BufferedReader bufferedReaderError = new BufferedReader(new InputStreamReader(runProcess.getErrorStream(),StandardCharsets.UTF_8));
                List<String> errorOutputStrList = new ArrayList<>();
                String compileOutLineError;
                while ((compileOutLineError = bufferedReaderError.readLine()) != null){
                    errorOutputStrList.add(compileOutLineError);
                }
                executeMessage.setErrorMessage(StringUtils.join(compileOutLineError,"\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }


    /**
     * 交互式进程并获取信息
     * @param runProcess
     * @param args
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess,String args){
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
        // 向控制台进行输入
        OutputStream outputStream = runProcess.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        String[] s = args.split(" ");
        String join = StrUtil.join("\n",s) + "\n";
        outputStreamWriter.write(join);
        // 回车
        outputStreamWriter.flush();
        // 分批获取进程的正常输出
        InputStream inputStream = runProcess.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder compileOutputStringBuilder = new StringBuilder();
        // 逐行读取
        String compileOutputLine;
        while((compileOutputLine = bufferedReader.readLine()) != null){
            compileOutputStringBuilder.append(compileOutputLine);
        }
        executeMessage.setMessage(compileOutputStringBuilder.toString());
        // 释放资源
        outputStreamWriter.close();
        outputStream.close();
        inputStream.close();
        runProcess.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }
}
