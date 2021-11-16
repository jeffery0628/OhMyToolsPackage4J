package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 文本工具类
 * @Author lizhen
 * @Date 2021年11月02日1:27 下午
 */
public class TextUtility {



    /**
     * 将异常转为字符串
     * ①将异常转换成String，以便log日志写入
     * @param e 异常
     * @return
     */
    public static String exceptionToString(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace();
        e.printStackTrace(pw);
        return sw.toString();
    }

}
