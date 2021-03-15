package Netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;

public class FileHandler extends SimpleChannelInboundHandler<String> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        System.out.println("Message: " + msg);
        String command = msg
                .replace("\r", "")
                .replace("\n", "");
        if (command.equals("list-files")) {
            File file = new File("server");
            File[] files = file.listFiles();
            StringBuffer sb = new StringBuffer();
            for (File f : files) {
                sb.append(f.getName() + "\n");
            }
            sb.append("end");
            channelHandlerContext.writeAndFlush(sb.toString());
        } else {
            System.out.println("Channel closed");
            channelHandlerContext.channel().closeFuture();
            channelHandlerContext.channel().close();
        }


//        msg = msg.replaceAll("lol", "***");
//        channelHandlerContext.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }
}
