package javax.net.p2p;
import com.q3lives.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.net.p2p.model.P2PWrapper;
import javax.net.p2p.model.SerializeWrapper;

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
@ChannelHandler.Sharable
public class WrapperEncoder extends MessageToByteEncoder<P2PWrapper> {

    @Override
    protected void encode(ChannelHandlerContext ctx, P2PWrapper msg, ByteBuf out) throws Exception {
        System.out.println("encoding:" + msg);
        out.writeBytes(SerializationUtil.serialize(msg));
    }
}
