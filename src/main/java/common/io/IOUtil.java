package common.io;

import lombok.extern.slf4j.Slf4j;
import utils.TextUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * @Author lizhen
 * @Date 2021年10月26日11:28 下午
 */
@Slf4j
public class IOUtil {
    /**
     * 检查本地文件是否存在
     *
     * @param filePath 文件路径
     * @return true 表示文件存在，false表示文件不存在
     */
    public static boolean isFileExisted(String filePath) {
        File file = new File(filePath);
        return file.isFile() && file.exists();
    }


    /**
     * 快速保存
     *
     * @param path    文本保存路径
     * @param content 保存的字符串内容
     * @return 返回是否保存成功
     */
    public static boolean saveText(String path, String content) {
        try {
            FileChannel fc = new FileOutputStream(path).getChannel();
            fc.write(ByteBuffer.wrap(content.getBytes()));
            fc.close();
        } catch (Exception e) {
            log.error("IOUtil saveText 到" + path + "失败" + TextUtils.exceptionToString(e));

            return false;
        }
        return true;
    }

    public static boolean saveText(String path, StringBuilder content) {
        return saveText(path, content.toString());
    }

    /**
     * 序列化对象
     *
     * @param object 序列化对象
     * @param path   保存对象到文件路径
     * @return true 对象持久化成功 false 对象持久化失败
     */
    public static boolean saveObjectTo(Object object, String path) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            log.error("在保存对象" + object + "到" + path + "时发生异常" + TextUtils.exceptionToString(e));
            return false;
        }

        return true;
    }

    public static boolean saveMapToTxt(Map<Object, Object> map, String path) {
        return saveMapToTxt(map, path, "=");
    }

    public static boolean saveMapToTxt(Map<Object, Object> map, String path, String separator) {
        map = new TreeMap<Object, Object>(map);
        return saveEntrySetToTxt(map.entrySet(), path, separator);
    }

    public static boolean saveEntrySetToTxt(Set<Map.Entry<Object, Object>> entrySet, String path, String separator) {
        StringBuilder sbOut = new StringBuilder();
        for (Map.Entry<Object, Object> entry : entrySet) {
            sbOut.append(entry.getKey());
            sbOut.append(separator);
            sbOut.append(entry.getValue());
            sbOut.append('\n');
        }
        return saveText(path, sbOut.toString());
    }

    /**
     * 反序列化对象
     *
     * @param path 对象读取路径
     * @return
     */
    public static Object readObjectFrom(String path) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            log.error("在从" + path + "读取对象时发生异常" + TextUtils.exceptionToString(e));
        }

        return null;
    }


    /**
     * 将整个文件读取为字节数组
     *
     * @param path 文件路径
     * @return 读取文件 以字节数组的形式存放数据
     */
    public static byte[] readBytes(String path) {
        if (!isFileExisted(path)) {
            log.error("文件不存在:" + path);
            return null;
        }
        try {
            return readBytesFromFileInputStream(new FileInputStream(path));
        } catch (IOException e) {
            log.error(TextUtils.exceptionToString(e));
        }
        return null;
    }

    /**
     * 以字符串的形式读取文本文件内容
     *
     * @param path 文件路径
     * @return 以字符串的形式返回文件内容
     */
    public static String readText(String path) {
        if (!isFileExisted(path)) {
            log.error("文件不存在:" + path);
            return null;
        }

        try {
            InputStream is = new FileInputStream(path);
            byte[] fileContent = new byte[is.available()];
            int read = readBytesFromInputStream(is, fileContent);
            is.close();
            // 处理 UTF-8 BOM
            if (read >= 3 && fileContent[0] == -17 && fileContent[1] == -69 && fileContent[2] == -65)
                return new String(fileContent, 3, fileContent.length - 3, StandardCharsets.UTF_8);
            return new String(fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(TextUtils.exceptionToString(e));
        }
        return null;

    }


    /**
     * @param path        文本文件路径
     * @param charsetName 文件字符编码格式
     * @return 以字符串的形式返回文件内容
     * @throws IOException IO异常
     */
    public static String readText(String path, String charsetName) throws IOException {
        InputStream is = new FileInputStream(path);
        byte[] targetArray = new byte[is.available()];
        int len;
        int off = 0;
        while ((len = is.read(targetArray, off, targetArray.length - off)) != -1 && off < targetArray.length) {
            off += len;
        }
        is.close();

        return new String(targetArray, charsetName);
    }


    /**
     * 读取csv文件，以字符串链表的形式返回读取结果，每个节点存储一行的信息
     *
     * @param path      csv 文件路径
     * @param separator CSV的分隔符
     * @return 字符串数组链表
     */
    public static LinkedList<String[]> readCsv(String path, String separator) {
        LinkedList<String[]> resultList = new LinkedList<>();
        LinkedList<String> lineList = readLineList(path);
        for (String line : lineList) {
            resultList.add(line.split(separator));
        }
        return resultList;
    }


    /**
     * 以字节数组的形式读取文件输入流中的内容
     *
     * @param fis 文件输入流
     * @return 字节数组
     * @throws IOException 向上抛出IO异常
     */
    private static byte[] readBytesFromFileInputStream(FileInputStream fis) throws IOException {
        FileChannel channel = fis.getChannel();
        long fileSize = channel.size();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileSize);
        channel.read(byteBuffer);
        byteBuffer.flip();
        byte[] bytes = byteBuffer.array();
        byteBuffer.clear();
        channel.close();
        fis.close();
        return bytes;

    }


    /**
     * 将InputStream中的全部数据读入到字节数组中
     *
     * @param is 字符流
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        int readBytes;
        byte[] buffer = new byte[Math.max(is.available(), 4096)]; // 最低4KB的缓冲区

        while ((readBytes = is.read(buffer, 0, buffer.length)) != -1) {
            data.write(buffer, 0, readBytes);
        }
        data.flush();
        return data.toByteArray();
    }


    /**
     * 从InputStream读取指定长度的字节出来
     *
     * @param is          流
     * @param targetArray output
     * @return 实际读取了多少字节，返回0表示遇到了文件尾部
     * @throws IOException
     */
    public static int readBytesFromInputStream(InputStream is, byte[] targetArray) throws IOException {
        assert targetArray != null;
        if (targetArray.length == 0) return 0;
        int len;
        int off = 0;
        while (off < targetArray.length && (len = is.read(targetArray, off, targetArray.length - off)) != -1) {
            off += len;
        }
        return off;
    }

    /**
     * 读取path，以链表的形式存储读取内容，每个节点存放一行文本
     *
     * @param path 文本文件路径
     * @return 字符串链表
     */
    public static LinkedList<String> readLineList(String path) {
        if (!isFileExisted(path)) {
            log.error("文件不存在:" + path);
            return null;
        }
        LinkedList<String> result = new LinkedList<String>();
        String txt = readText(path);
        if (txt == null) return result;
        StringTokenizer tokenizer = new StringTokenizer(txt, "\n");
        while (tokenizer.hasMoreTokens()) {
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
    public static LinkedList<String> readLineListWithLessMemory(String path) {
        LinkedList<String> result = new LinkedList<String>();
        String line = null;
        boolean first = true;
        try {
            BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            while ((line = bw.readLine()) != null) {
                if (first) {
                    first = false;
                    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') //首行出现的"\ufeff"叫BOM(“ByteOrder Mark”)用来声明该文件的编码信息.
                        line = line.substring(1);
                }
                result.add(line);
            }
            bw.close();
        } catch (Exception e) {
            log.error("加载" + path + "失败，" + TextUtils.exceptionToString(e));
        }

        return result;
    }

    public static LineIterator readLineIterator(String path) {
        return new LineIterator(path);
    }

    /**
     * 去除文件第一行中的UTF8 BOM<br>
     * 这是Java的bug，且官方不会修复。参考 https://stackoverflow.com/questions/4897876/reading-utf-8-bom-marker
     *
     * @param line 文件第一行
     * @return 去除BOM的部分
     */
    public static String removeUTF8BOM(String line) {
        if (line != null && line.startsWith("\uFEFF")) // UTF-8 byte order mark (EF BB BF)
        {
            line = line.substring(1);
        }
        return line;
    }

    /**
     * 获取文件所在目录的路径,如src/main/resources/200W数据.txt  --> src/main/resources/
     *
     * @param path 文件路径
     * @return 返回文件所在的目录路径
     */
    public static String dirName(String path) {
        int index = path.lastIndexOf('/');
        if (index == -1) return path;
        return path.substring(0, index + 1);
    }


    /**
     * 递归遍历目录
     *
     * @param folder   目录
     * @param fileList 储存文件
     */
    private static void recursiveDirectory(File folder, List<File> fileList) {
        File[] fileArray = folder.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isFile() && !file.getName().startsWith(".")) // 过滤隐藏文件
                {
                    fileList.add(file);
                } else {
                    recursiveDirectory(file, fileList);
                }
            }
        }
    }

    /**
     * 递归遍历获取目录下的所有文件
     *
     * @param path 根目录
     * @return 文件列表
     */
    public static List<File> getDirectoryFiles(String path) {
        List<File> fileList = new LinkedList<File>();
        File folder = new File(path);
        if (folder.isDirectory())
            recursiveDirectory(folder, fileList);
        else
            fileList.add(folder); // 兼容路径为文件的情况
        return fileList;
    }

    /**
     * 从文件路径中获取文件名称，如src/main/resources/200W数据.txt  -->  200W数据.txt
     *
     * @param path 文件路径
     * @return
     */

    public static String getFileBaseName(String path) {
        if (path == null || path.length() == 0)
            return "";
        path = path.replaceAll("[/\\\\]+", "/");
        int len = path.length(),
                upCount = 0;
        while (len > 0) {
            //remove trailing separator
            if (path.charAt(len - 1) == '/') {
                len--;
                if (len == 0)
                    return "";
            }
            int lastInd = path.lastIndexOf('/', len - 1);
            String fileName = path.substring(lastInd + 1, len);
            if (fileName.equals(".")) {
                len--;
            } else if (fileName.equals("..")) {
                len -= 2;
                upCount++;
            } else {
                if (upCount == 0)
                    return fileName;
                upCount--;
                len -= fileName.length();
            }
        }
        return "";
    }

    /**
     * 获取最后一个分隔符的后缀,比如文件后缀
     * @param name
     * @param delimiter
     * @return
     */
    public static String getFileSuffix(String name, String delimiter)
    {
        return name.substring(name.lastIndexOf(delimiter) + 1);
    }

    /**
     * 删除本地文件
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        return new File(path).delete();
    }

    /**
     * 创建一个BufferedWriter,以输出内容
     *
     * @param path  输出文件路径
     * @return BufferedWriter 对象
     * @throws FileNotFoundException 文件找不到异常
     */
    public static BufferedWriter newBufferedWriter(String path) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
    }

    /**
     * 创建一个BufferedWriter
     * @param path  输出文件路径
     * @param append 是否向文件追加内容
     * @return BufferedWriter 对象
     * @throws FileNotFoundException 文件找不到异常
     */
    public static BufferedWriter newBufferedWriter(String path, boolean append) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), StandardCharsets.UTF_8));
    }


    /**
     * 创建一个BufferedReader,以向内存中加载path文件中内容
     *
     * @param path 文件路径
     * @return
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static BufferedReader newBufferedReader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
    }

}


/**
 * 方便读取按行读取大文件
 */
@Slf4j
class LineIterator implements Iterator<String>, Iterable<String> {
    BufferedReader bw;
    String line;

    /**
     * 构造器
     *
     * @param bw 缓冲字符读取器
     */
    public LineIterator(BufferedReader bw) {
        this.bw = bw;
        try {
            line = bw.readLine();
            line = IOUtil.removeUTF8BOM(line);
        } catch (IOException e) {
            log.error("在读取过程中发生错误" + TextUtils.exceptionToString(e));
            bw = null;
        }
    }

    public LineIterator(String path) {
        try {
            bw = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            line = bw.readLine();
            line = IOUtil.removeUTF8BOM(line);
        } catch (FileNotFoundException e) {
            log.error("文件" + path + "不存在，接下来的调用会返回null\n" + TextUtils.exceptionToString(e));
            bw = null;
        } catch (IOException e) {
            log.error("在读取过程中发生错误" + TextUtils.exceptionToString(e));
            bw = null;
        }
    }

    public void close() {
        if (bw == null) return;
        try {
            bw.close();
            bw = null;
        } catch (IOException e) {
            log.error("关闭文件失败" + TextUtils.exceptionToString(e));
        }
    }

    @Override
    public boolean hasNext() {
        if (bw == null) return false;
        if (line == null) {
            try {
                bw.close();
                bw = null;
            } catch (IOException e) {
                log.error("关闭文件失败" + TextUtils.exceptionToString(e));
            }
            return false;
        }

        return true;
    }

    @Override
    public String next() {
        String preLine = line;
        try {
            if (bw != null) {
                line = bw.readLine();
                if (line == null && bw != null) {
                    try {
                        bw.close();
                        bw = null;
                    } catch (IOException e) {
                        log.error("关闭文件失败" + TextUtils.exceptionToString(e));
                    }
                }
            } else {
                line = null;
            }
        } catch (IOException e) {
            log.error("在读取过程中发生错误" + TextUtils.exceptionToString(e));
        }
        return preLine;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("只读，不可写！");
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

}
