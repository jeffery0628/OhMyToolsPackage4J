package common.io;

/**
 * @Author lizhen
 * @Date 2021年11月02日1:37 下午
 *
 * 对字节数组进行封装，提供方便的读取操作
 */
public class ByteArray {

    byte[] bytes;  // 当前字节数组，不一定是全部字节，可能只是一个片段
    int offset;    // 当前已读取的字节数，或下一个字节的指针

    /**
     * 字节数组构造器
     * @param bytes 传入直接数组
     */
    public ByteArray(byte[] bytes){
        this.bytes = bytes;
    }


    /**
     * 从文件读取一个字节数组
     *
     * @param path 文件路径
     * @return 读取文件内容，并构造一个字节数组
     */
    public static ByteArray createByteArray(String path)
    {
        byte[] bytes = IOUtil.readBytes(path);
        if (bytes == null) return null;
        return new ByteArray(bytes);
    }


}
