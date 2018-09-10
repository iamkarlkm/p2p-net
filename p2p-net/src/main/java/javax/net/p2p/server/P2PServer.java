package javax.net.p2p.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javax.net.p2p.WrapperDecoder;
import javax.net.p2p.WrapperEncoder;

public class P2PServer {
    private final int port;
    public P2PServer(int port) {
        this.port = port;  
    }  
    public void start(){  
        EventLoopGroup bossGroup = new NioEventLoopGroup();  
        EventLoopGroup workerGroup = new NioEventLoopGroup();  
          
        try {  
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
            		.channel(NioServerSocketChannel.class)  
                    .childHandler(new ChannelInitializer<SocketChannel>() {
  
                        @Override  
                        protected void initChannel(SocketChannel ch) throws Exception {  
                            ChannelPipeline pipeline = ch.pipeline();    
                            pipeline.addLast("encoder", new WrapperEncoder());
                            pipeline.addLast("decoder", new WrapperDecoder());
                            pipeline.addLast(new ServerMessageHandler());
                        }  
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)       
                    .childOption(ChannelOption.SO_KEEPALIVE, true);  
            ChannelFuture future = b.bind(port).sync();      
            System.out.println("P2P server start listen at " + port);
            future.channel().closeFuture().sync();    
        } catch (Exception e) {  
             bossGroup.shutdownGracefully();    
             workerGroup.shutdownGracefully();  
        }  
    }
    
    
    public static void main(String[] args) throws Exception {    
        new P2PServer(1987).start();
    }    
}  
