package brainbyte.util;

import java.util.HashMap;

public class ByteClassLoader extends ClassLoader {
    private HashMap<String, byte[]> byteDataMap = new HashMap<>();

    public ByteClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void loadDataInBytes(byte[] byteData, String resourcesName) {
        byteDataMap.put(resourcesName, byteData);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (byteDataMap.isEmpty())
            throw new ClassNotFoundException("byte data is empty");

        byte[] extractedBytes = byteDataMap.get(className);
        if (extractedBytes == null)
            throw new ClassNotFoundException("Cannot find " + className + " in bytes");

        return defineClass(className, extractedBytes, 0, extractedBytes.length);
    }
}
