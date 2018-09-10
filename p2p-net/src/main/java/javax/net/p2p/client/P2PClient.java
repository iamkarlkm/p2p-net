package javax.net.p2p.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.net.p2p.WrapperDecoder;
import javax.net.p2p.WrapperEncoder;
import javax.net.p2p.api.P2PCommand;
import javax.net.p2p.model.P2PWrapper;

public class P2PClient {

    private final InetAddress address;
    private final int port;

    public int getPort() {
        return port;
    }

    public P2PClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * 实现接口的核心方法
     *
     * @param request
     * @return
     */
    public Object excute(P2PWrapper request) {

        final ClientMessageHandler clientMessageHandler = new ClientMessageHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("encoder", new WrapperEncoder());
                            pipeline.addLast("decoder", new WrapperDecoder());
                            pipeline.addLast("handler", clientMessageHandler);
                        }
                    });

            ChannelFuture future = b.connect(address, port).sync();
            future.channel().writeAndFlush(request).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
        return clientMessageHandler.getResponse();
    }
	
    public static void main(String[] args) throws Exception {
        P2PClient client = new P2PClient(InetAddress.getLocalHost(), 1987);

        P2PWrapper p2p = P2PWrapper.builder(P2PCommand.GET_NODE, "test");
        client.excute(p2p);

    }
    
}
