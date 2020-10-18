package demo.classloader;

import java.io.*;

/**
 * @author hongweixu
 * @since 2020-10-18 15:06
 */
public class HelloClassLoader extends ClassLoader {

    public static void main(String[] args) {

        HelloClassLoader hCL = new HelloClassLoader();

        try {
            Object hello = hCL.findClass("demo.classloader.Hello").newInstance();

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        System.exit(0);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        InputStream is;
        try {
            is = new FileInputStream(new File("/Users/hongweixu/hello_class.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            int available = is.available();
            byte[] buffer = new byte[available];
            read(is, buffer);
            return defineClass(name, buffer, 0, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
