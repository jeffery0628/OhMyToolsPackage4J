package common.io;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Author lizhen
 * @Date 2021年10月31日9:41 下午
 */
public class IOUtilTest {

    @Test
    public void testIsFileExists(){
//        Assert.assertTrue(IOUtil.isFileExisted("src/main/resources/test.txt"));
//        Assert.assertFalse(IOUtil.isFileExisted("src/main/resources/test1.txt"));
        Assert.assertArrayEquals(new byte[10],IOUtil.readBytes("src/main/resources/test1.txt"));

    }
}
