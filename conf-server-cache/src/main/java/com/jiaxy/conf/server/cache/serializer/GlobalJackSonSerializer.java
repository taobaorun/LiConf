package com.jiaxy.conf.server.cache.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Description: <br/>
 * <p/>
 * Company:
 *
 * @author: <a href=mailto:taobaorun@gmail.com>tom</a>
 * <br/>
 * @Date: 2017/09/15 17:15
 */
public class GlobalJackSonSerializer implements StreamSerializer<Object> {


    private ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void write(ObjectDataOutput out, Object object) throws IOException {
        if (object != null) {
            String clzName = object.getClass().getCanonicalName();
            if (clzName == null) {
                clzName = object.getClass().getName();
            }
            if (clzName == null || "".equals(clzName)) {
                return;
            }
            int len = clzName.length();
            byte[] clzNameArr = clzName.getBytes();
            byte[] objArr = objectMapper.writeValueAsBytes(object);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + clzNameArr.length + objArr.length);
            byteBuffer.putInt(len);
            byteBuffer.put(clzNameArr);
            byteBuffer.put(objArr);
            byteBuffer.flip();
            out.writeByteArray(byteBuffer.array());
        }
    }

    @Override
    public Object read(ObjectDataInput in) throws IOException {
        byte[] arr = in.readByteArray();
        if (arr != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
            int clzNameLen = byteBuffer.getInt();
            byte[] clzNameArr = new byte[clzNameLen];
            byteBuffer.get(clzNameArr);
            String clzName = new String(clzNameArr);
            Class clz = null;
            try {
                clz = Class.forName(clzName);
            } catch (ClassNotFoundException e) {
                return null;
            }
            byte[] objBytes = new byte[arr.length - clzNameLen - 4];
            byteBuffer.get(objBytes);
            return objectMapper.readValue(objBytes, clz);
        }
        return null;
    }

    @Override
    public int getTypeId() {
        return 20170915;
    }

    @Override
    public void destroy() {

    }
}
