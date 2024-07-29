package com.dongs.dongscodesandbox.sandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.dongs.dongscodesandbox.model.ExecuteCodeRequest;
import com.dongs.dongscodesandbox.model.ExecuteCodeResponse;
import com.dongs.dongscodesandbox.model.ExecuteMessage;
import com.dongs.dongscodesandbox.model.JudgeInfo;
import com.dongs.dongscodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 执行代码沙箱模板方法
 *
 * @author dongs
 */
@Slf4j
public class JavaCodeSandBoxTemplate implements CodeSandBox {


    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";


    private static final Long EXCESS_TIME = 5000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();;
        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();
        // 1、将用户提交的代码保存为文件
        File userCodeFile = saveCodeToFile(code);
        // 2、编译用户的代码文件
        ExecuteMessage compileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileExecuteMessage);
        // 3、执行编译后的字节码文件
        List<ExecuteMessage> executeMessages = runFile(userCodeFile,inputList);
        // 4、保存输出信息，收集结果
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessages);
        // 5、删除用户的临时文件
        boolean b = deleteCodeFile(userCodeFile);
        if (!b){
            log.info("删除文件失败{}",userCodeFile);
        }
        return executeCodeResponse;
    }

    /**
     * 将用户提交的代码保存为文件
     * @param code 用户提交的代码
     * @return 保存的文件
     */
    public File saveCodeToFile(String code){
        // 创建文件夹目录
        String projectPath = System.getProperty("user.dir");
        String globalCodePathName = projectPath + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }
        // 将用户代码进行隔离
        String userCodeProjectPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeProjectPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        return FileUtil.writeUtf8String(code,userCodePath);
    }


    /**
     * 编译代码
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        // 执行编译代码
        String compileCmd = String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess,"编译");
            if (executeMessage.getExitValue() != 0){
                throw new RuntimeException(executeMessage.getMessage());
            }
            return executeMessage;
        } catch (IOException e) {
            // 返回错误相应
            throw new RuntimeException(e);
        }
    }


    /**
     * 运行用户的代码
     * @param userCodeFile 用户代码文件
     * @param inputList 输入用例
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile,List<String> inputList){
        // 执行编译好的class文件
        List<ExecuteMessage> executeMessages = new ArrayList<>();
        for (String input : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeFile.getAbsoluteFile(),input);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                new Thread(() -> {
                    try {
                        Thread.sleep(EXCESS_TIME);
                        runProcess.destroy();
                        System.out.println("超时");
                    } catch (InterruptedException e) {
                        System.out.println("结束");
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess,"运行");
                executeMessages.add(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException("运行错误");
            }
        }
        return executeMessages;
    }

    /**
     * 收集运行结果
     * @param executeMessages 执行结果列表
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessages){
        // 收集返回信息
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessages){
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)){
                outputList.add(executeMessage.getMessage());
                // 执行中出现错误
                executeCodeResponse.setStatus(3);
                break;
            }
            Long time = executeMessage.getTime();

            if (time != null){
                maxTime = Math.max(time,maxTime);
            }

            // 如果没有错误信息就正常添加
            outputList.add(executeMessage.getMessage());
        }
        // 没有错误
        if (outputList.size() == executeMessages.size()){
            executeCodeResponse.setStatus(1);
        }

        executeCodeResponse.setOutputList(outputList);
        // 正常运行
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }


    /**
     * 删除用户的临时文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteCodeFile(File userCodeFile){
        if (userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeFile.getParentFile().getAbsoluteFile());
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setMessage(e.getMessage());
        // 代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

}
