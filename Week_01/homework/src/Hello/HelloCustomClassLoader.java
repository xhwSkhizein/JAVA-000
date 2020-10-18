package Hello;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 自定义classloader加载 {@link Hello} 类的加密字节码
 *
 * @author hongweixu
 * @since 2020-10-18 15:55
 */
public class HelloCustomClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) {
        String workDir = System.getProperty("user.dir");
        String encryptClassFile = workDir + "/Week_01/homework/src/Hello/Hello.xlass";
        System.out.println(encryptClassFile);

        File file = new File(encryptClassFile);
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[is.available()];
            int ignore = read(is, buffer);
            byte[] decodeResult = new byte[buffer.length];
            for (int i = 0; i < buffer.length; i++) {
                byte b = buffer[i];
                decodeResult[i] = decode(b);
            }

            return defineClass(name, decodeResult, 0, decodeResult.length);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        HelloCustomClassLoader hCL = new HelloCustomClassLoader();
        Class<?> clazz = hCL.findClass("Hello");
        Object obj = clazz.newInstance();
        Method helloMethod = clazz.getMethod("hello");
        helloMethod.invoke(obj);

        System.exit(0);
    }


    private byte decode(byte b) {
        return (byte) (255 - b);
    }

    public static int read(InputStream input, byte[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    public static int read(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        } else {
            int remaining;
            int count;
            for (remaining = length; remaining > 0; remaining -= count) {
                int location = length - remaining;
                count = input.read(buffer, offset + location, remaining);
                if (-1 == count) {
                    break;
                }
            }

            return length - remaining;
        }
    }
}
