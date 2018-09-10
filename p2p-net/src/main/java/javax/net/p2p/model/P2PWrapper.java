package javax.net.p2p.model;

import javax.net.p2p.api.P2PCommand;

/**
 * <p>
 * 序列化/反序列化对象包装类 专为基于 Protostuff 进行序列化/反序列化而定义。 Protostuff 是基于POJO进行序列化和反序列化操作。
 * 如果需要进行序列化/反序列化的对象不知道其类型，不能进行序列化/反序列化；
 * 比如Map、List、String、Enum等是不能进行正确的序列化/反序列化。
 * 因此需要映入一个包装类，把这些需要序列化/反序列化的对象放到这个包装类中。 这样每次 Protostuff
 * 都是对这个类进行序列化/反序列化,不会出现不能/不正常的操作出现
 * </p>
 *
 * @author iamkarl@163.com
 * @param <T>
 */
public class P2PWrapper<T> {

    private P2PCommand command;
    private T data;

    public static <T> P2PWrapper<T> builder(P2PCommand command, T data) {
        P2PWrapper<T> wrapper = new P2PWrapper<>();
        wrapper.setData(data);
        wrapper.setCommand(command);
        return wrapper;
    }

    public P2PCommand getCommand() {
        return command;
    }

    public void setCommand(P2PCommand command) {
        this.command = command;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "command:" + command + ",data:" + data;
    }

}
