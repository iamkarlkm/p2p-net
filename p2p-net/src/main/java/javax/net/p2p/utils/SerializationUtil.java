package com.q3lives.utils;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.net.p2p.model.SerializeWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * **************************************************
 * @description Protostuff 序列化/反序列化工具类
 * @author iamkarl@163.com
 * @version 2.0, 2018-08-07
 * @see HISTORY
 *      Date        Desc          Author      Operation
 *  	2017-4-7 创建文件 karl create Date Desc
 * Author Operation 2018-08-07 创建文件 karl Protostuff 不支持序列化/反序列化数组、集合等对象,特殊处理
 * @since 2017 Phyrose Science & Technology (Kunming) Co., Ltd.
 **************************************************/
public class SerializationUtil {

    private static final Log log = LogFactory.getLog(SerializationUtil.class);
    /**
     * 线程局部变量
     */
    private static final ThreadLocal<LinkedBuffer> BUFFERS = new ThreadLocal();

    /**
     * 序列化/反序列化包装类 Schema 对象
     */
    private static final Schema<SerializeWrapper> WRAPPER_SCHEMA = RuntimeSchema.getSchema(SerializeWrapper.class);


    /**
     * 序列化对象
     *
     * @param obj 需要序列化的对象
     * @return 序列化后的二进制数组
     */
    @SuppressWarnings("unchecked")
    public static byte[] serialize(Object obj) {
        Class<?> clazz = (Class<?>) obj.getClass();
        LinkedBuffer buffer = BUFFERS.get();
        if (buffer == null) {//存储buffer到线程局部变量中，避免每次序列化操作都分配内存提高序列化性能
            buffer = LinkedBuffer.allocate(512);
            BUFFERS.set(buffer);
        }
        try {
            Object serializeObject = obj;
            Schema schema = WRAPPER_SCHEMA;
            if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)
                    || Map.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz)) {//Protostuff 不支持序列化/反序列化数组、集合等对象,特殊处理
                serializeObject = SerializeWrapper.builder(obj);
            } else {
                schema = RuntimeSchema.getSchema(clazz);
            }
            return ProtostuffIOUtil.toByteArray(serializeObject, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化对象
     *
     * @param data 需要反序列化的二进制数组
     * @param clazz 反序列化后的对象class
     * @param <T> 反序列化后的对象类型
     * @return 反序列化后的实例对象
     */
    public static <T> T deserialize(Class<T> clazz, byte[] data) {
        if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)
                || Map.class.isAssignableFrom(clazz) || Set.class.isAssignableFrom(clazz)) {//Protostuff 不支持序列化/反序列化数组、集合等对象,特殊处理
            SerializeWrapper<T> wrapper = new SerializeWrapper<>();
            ProtostuffIOUtil.mergeFrom(data, wrapper, WRAPPER_SCHEMA);
            return wrapper.getData();
        } else {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T message = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        }
    }


    public static void main(String[] args) throws Exception {
        System.out.println("String[abcd]:" + SerializationUtil.serialize("abcd").length);
        byte[] array = "abcd".getBytes();
        Class c = String[].class;
        //System.out.println(new String(SerializationUtil.deserialize(byte[].class, SerializationUtil.serialize(array))));
        System.out.println("array:" + SerializationUtil.serialize(array).length);
        System.out.println(SerializationUtil.deserialize(byte[].class, SerializationUtil.serialize(array)).getClass());
        //System.out.println(SerializationUtil.deserialize(byte[].class, SerializationUtil.serialize(array)).getClass());

        //LinkedList<Object> list = new LinkedList<>();
        Set<Object> list = new HashSet<>();
        list.add("aa");
        list.add("bb");
        System.out.println(SerializationUtil.serialize(list).length);

        //System.out.println(SerializationUtil.deserialize(LinkedList.class, SerializationUtil.serialize(list)));
        //System.out.println(SerializationUtil.deserialize(LinkedList.class, SerializationUtil.serialize(list)).getClass());
        System.out.println(SerializationUtil.deserialize(HashSet.class, SerializationUtil.serialize(list)));
        System.out.println(SerializationUtil.deserialize(HashSet.class, SerializationUtil.serialize(list)).getClass());
    }

}  