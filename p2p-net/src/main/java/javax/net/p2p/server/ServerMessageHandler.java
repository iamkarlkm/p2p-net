package javax.net.p2p.server;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javax.net.p2p.api.P2PCommand;
import javax.net.p2p.model.P2PWrapper;

public class ServerMessageHandler extends ChannelInboundHandlerAdapter {
	
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String,Object>();
    
    private List<String> classCache = new ArrayList<>();
    
    public ServerMessageHandler() {
//    	scannerClass("javax.net.p2p.provider");
//    	doRegister();
    }
    
    
    @Override    
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        P2PWrapper request = (P2PWrapper) msg;
        System.out.println("server接收到client的消息:" + msg);
        P2PWrapper p2p = P2PWrapper.builder(P2PCommand.STD_RESPONSE, "test response");
        ctx.write(p2p);
        ctx.flush();    
        ctx.close();  
    }
    
    @Override    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {    
         cause.printStackTrace();    
         ctx.close();    
    }
    

	private void scannerClass(String packageName){
		URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			//如果是一个文件夹，继续递归
			if(file.isDirectory()){
				scannerClass(packageName + "." + file.getName());
			}else{
				classCache.add(packageName + "." + file.getName().replace(".class", "").trim());
			}
		}
	}

	
	private void doRegister(){
		if(classCache.size() == 0){ return; }
		
		for (String className : classCache) {
			try {
				Class<?> clazz = Class.forName(className);
				
				Class<?> interfaces = clazz.getInterfaces()[0];
				
				registryMap.put(interfaces.getName(), clazz.newInstance()); 
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
  
}
