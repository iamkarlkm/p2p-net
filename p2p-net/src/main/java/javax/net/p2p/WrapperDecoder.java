package javax.net.p2p;
import com.q3lives.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import javax.net.p2p.model.P2PWrapper;
import org.msgpack.MessageTypeException;

/**
 * **************************************************
 * @description 
 * @author   karl
 * @version  1.0, 2018-9-8
 * @see HISTORY
 *      Date        Desc          Author      Operation
 *  	2018-9-8   创建文件       karl        create
 * @since 2017 Phyrose Science & Technology (Kunming) Co., Ltd.
 **************************************************/
public class WrapperDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {//先获取可读字节数
            final int length = in.readableBytes();
            final byte[] array = new byte[length];

            in.getBytes(in.readerIndex(), array, 0, length);
            System.out.println("decoding:" + in);
            Object result = SerializationUtil.deserialize(P2PWrapper.class, array);
            out.add(result);
            in.clear();
        } catch (MessageTypeException e) {
            ctx.channel().pipeline().remove(this);
        }
}
}
