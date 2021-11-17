package utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * 文本工具类
 *
 * @Author lizhen
 * @Date 2021年11月02日1:27 下午
 */
public class TextUtils {


    /**
     * 将异常转为字符串
     * ①将异常转换成String，以便log日志写入
     *
     * @param e 异常
     * @return
     */
    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace();
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 是否全是中文
     *
     * @param str 输入字符串
     * @return true 字符串全部为中文，false 字符串不全部为中文
     */
    public static boolean isAllChinese(String str) {
        byte[] b = str.getBytes();
        return str.matches("[\\u4E00-\\u9FA5]+");
    }

    /**
     * 是否全部不是中文
     *
     * @param str 输入的文本字符串
     * @return true 字符串中没有中文字符，false 字符串中有中文字符
     */
    public static boolean isAllNonChinese(String str) {
        byte[] sString = str.getBytes();
        int nLen = sString.length;
        int i = 0;

        while (i < nLen) {
            if (getUnsigned(sString[i]) < 248 && getUnsigned(sString[i]) > 175)
                return false;
            if (sString[i] < 0)
                i += 2;
            else
                i += 1;
        }
        return true;
    }

    /**
     * 是否全是单字节
     * 基础知识：
     * 单字节指只占一个字，是英文字符。双字是占两个字节的，中文字符都占两个字节
     * 计算机中的数据都是以0和1来表示的，其中一个0或者一个1称之为一位，8位称为一个字节（Byte）
     * 一般比较好理解的就是：英文字母属于单字节字符，而汉字则属于双字节字符。
     * 因为英文字母、数字、符号等完全可以用128种不同的数值来表示，而汉字太多则不能，所以才需要扩展到双字节。
     * @param str 输入字符串
     * @return true：不包含汉字
     */
    public static boolean isAllSingleByte(String str)
    {
        assert str != null;
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) >128)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 字符串是否全是数字
     * @param str 输入字符串
     * @return  true 字符串全部为数字， false 不全为数字
     */
    public static boolean isAllNum(String str)
    {

        if (str == null)
            return false;

        int i = 0;
        /* 判断开头是否是+-之类的符号 */
        if ("±+-＋－—".indexOf(str.charAt(0)) != -1)
            i++;
        /** 如果是全角的０１２３４５６７８９ 字符* */
        while (i < str.length() && "０１２３４５６７８９".indexOf(str.charAt(i)) != -1)
            i++;
        // Get middle delimiter such as .
        if (i > 0 && i < str.length())
        {
            char ch = str.charAt(i);
            if ("·∶:，,．.／/".indexOf(ch) != -1)
            {// 98．1％
                i++;
                while (i < str.length() && "０１２３４５６７８９".indexOf(str.charAt(i)) != -1)
                    i++;
            }
        }
        if (i >= str.length())
            return true;

        /** 如果是半角的0123456789字符* */
        while (i < str.length() && "0123456789".indexOf(str.charAt(i)) != -1)
            i++;
        // Get middle delimiter such as .
        if (i > 0 && i < str.length())
        {
            char ch = str.charAt(i);
            if (',' == ch || '.' == ch || '/' == ch  || ':' == ch || "∶·，．／".indexOf(ch) != -1)
            {// 98．1％
                i++;
                while (i < str.length() && "0123456789".indexOf(str.charAt(i)) != -1)
                    i++;
            }
        }

        if (i < str.length())
        {
            if ("百千万亿佰仟%％‰".indexOf(str.charAt(i)) != -1)
                i++;
        }
        if (i >= str.length())
            return true;

        return false;
    }


    /**
     * 获取字节对应的无符号整型数
     *
     * @param b
     * @return
     */
    public static int getUnsigned(byte b)
    {
        if (b > 0)
            return (int) b;
        else
            return (b & 0x7F + 128);
    }


    /**
     * 判断字符串是否是年份
     *
     * @param snum
     * @return
     */
//    public static boolean isYearTime(String snum)
//    {
//        if (snum != null)
//        {
//            int len = snum.length();
//            String first = snum.substring(0, 1);
//
//            // 1992年, 98年,06年
//            if (isAllSingleByte(snum)
//                    && (len == 4 || len == 2 && (cint(first) > 4 || cint(first) == 0)))
//                return true;
//            if (isAllNum(snum) && (len >= 3 || len == 2 && "０５６７８９".indexOf(first) != -1))
//                return true;
//            if (getCharCount("零○一二三四五六七八九壹贰叁肆伍陆柒捌玖", snum) == len && len >= 2)
//                return true;
//            if (len == 4 && getCharCount("千仟零○", snum) == 2)// 二仟零二年
//                return true;
//            if (len == 1 && getCharCount("千仟", snum) == 1)
//                return true;
//            if (len == 2 && getCharCount("甲乙丙丁戊己庚辛壬癸", snum) == 1
//                    && getCharCount("子丑寅卯辰巳午未申酉戌亥", snum.substring(1)) == 1)
//                return true;
//        }
//        return false;
//    }




    public static void main(String[] args) {
        System.out.println(isAllNum("21"));
    }

}
