package common.io;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeMap;


/**
 * @Author lizhen
 * @Date 2021年10月26日11:28 下午
 */
@Slf4j
public class IOUtil {


    /**
     * 序列化对象
     *
     * @param object  序列化对象
     * @param path 保存对象到文件路径
     * @return true 对象持久化成功 false 对象持久化失败
     */
    public static boolean saveObjectTo(Object object, String path)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(object);
            oos.close();
        }
        catch (IOException e)
        {
            log.error("在保存对象" + object + "到" + path + "时发生异常" + e);
            return false;
        }

        return true;
    }

    /**
     * 反序列化对象
     *
     * @param path  对象读取路径
     * @return
     */
    public static Object readObjectFrom(String path)
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new FileInputStream(path));
            Object o = ois.readObject();
            ois.close();
            return o;
        }
        catch (Exception e)
        {
            log.error("在从" + path + "读取对象时发生异常" + e);
        }

        return null;
    }



    /**
     * 检查本地文件是否存在
     * @param filePath 文件路径
     * @return true 表示文件存在，false表示文件不存在
     */
    public static boolean isFileExisted(String filePath){
        File file = new File(filePath);
        return file.isFile() && file.exists();
    }


    /**
     * 将整个文件读取为字节数组
     *
     * @param path 文件路径
     * @return 读取文件 以字节数组的形式存放数据
     */
    public static byte[] readBytes(String path)
    {
        if (!isFileExisted(path)){
            log.error("文件不存在:"+path);
            return null;
        }
        try {
            return readBytesFromFileInputStream(new FileInputStream(path));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 以字符串的形式读取文本文件内容
     * @param path 文件路径
     * @return 以字符串的形式返回文件内容
     */
    public static String readText(String path){
        if (!isFileExisted(path)){
            log.error("文件不存在:"+path);
            return null;
        }

        try {
            InputStream is = new FileInputStream(path);
            byte[] fileContent = new byte[is.available()];
            int read = readBytesFromOtherInputStream(is, fileContent);
            is.close();
            // 处理 UTF-8 BOM
            if (read >= 3 && fileContent[0] == -17 && fileContent[1] == -69 && fileContent[2] == -65)
                return new String(fileContent, 3, fileContent.length - 3, StandardCharsets.UTF_8);
            return new String(fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;

    }


    /**
     * 读取csv文件，以字符串链表的形式返回读取结果，每个节点存储一行的信息
     * @param path csv 文件路径
     * @param separator CSV的分隔符
     * @return 字符串数组链表
     */
    public static LinkedList<String[]> readCsv(String path,String separator)
    {
        LinkedList<String[]> resultList = new LinkedList<>();
        LinkedList<String> lineList = readLineList(path);
        for (String line : lineList)
        {
            resultList.add(line.split(separator));
        }
        return resultList;
    }


    /**
     * 以字节数组的形式读取文件输入流中的内容
     * @param fis 文件输入流
     * @return 字节数组
     * @throws IOException  向上抛出IO异常
     */
    private static byte[] readBytesFromFileInputStream(FileInputStream fis) throws IOException
    {
        FileChannel channel = fis.getChannel();
        long fileSize = channel.size();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileSize);
        channel.read(byteBuffer);
        byteBuffer.flip();
        byte[] bytes = byteBuffer.array();
        byteBuffer.clear();
        channel.close();
        fis.close();
        return bytes;

    }

    /**
     * 从InputStream读取指定长度的字节出来
     * @param is 流
     * @param targetArray output
     * @return 实际读取了多少字节，返回0表示遇到了文件尾部
     * @throws IOException
     */
    public static int readBytesFromOtherInputStream(InputStream is, byte[] targetArray) throws IOException
    {
        assert targetArray != null;
        if (targetArray.length == 0) return 0;
        int len;
        int off = 0;
        while (off < targetArray.length && (len = is.read(targetArray, off, targetArray.length - off)) != -1)
        {
            off += len;
        }
        return off;
    }

    /**
     * 读取path，以链表的形式存储读取内容，每个节点存放一行文本
     * @param path 文本文件路径
     * @return 字符串链表
     */
    public static LinkedList<String> readLineList(String path)
    {
        if (!isFileExisted(path)){
            log.error("文件不存在:"+path);
            return null;
        }
        LinkedList<String> result = new LinkedList<String>();
        String txt = readText(path);
        if (txt == null) return result;
        StringTokenizer tokenizer = new StringTokenizer(txt, "\n");
        while (tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken());
        }

        return result;
    }


    /**
     * 用省内存的方式读取大文件
     *
     * @param path
     * @return
     */
    public static LinkedList<String> readLineListWithLessMemory(String path)
    {
        LinkedList<String> result = new LinkedList<String>();
        String line = null;
        boolean first = true;
        try
        {
            BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            while ((line = bw.readLine()) != null)
            {
                if (first)
                {
                    first = false;
                    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') //首行出现的"\ufeff"叫BOM(“ByteOrder Mark”)用来声明该文件的编码信息.
                        line = line.substring(1);
                }
                result.add(line);
            }
            bw.close();
        }
        catch (Exception e)
        {
            log.error("加载" + path + "失败，" + e);
        }

        return result;
    }



    public static void main(String[] args) {
        String path = "src/main/resources/200W商品数据.txt";
//        String s = readText("src/main/resources/test.txt");
        LinkedList<String> s =  readLineListWithLessMemory(path);
        System.out.println(s);
    }

}
