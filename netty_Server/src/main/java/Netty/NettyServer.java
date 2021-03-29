package Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.File;

/**
 * Класс использует возможности Netty библиотеки
 * Пулл потоков auth для прослушивания подключений использует одну нить
 * В пулле потоков обработки клиентских запросов workers количество нитей задается динамически
 * pipeline содержит 3 хендлера
 * Включает стартовый метод
 */
public class NettyServer {

    public NettyServer(String mySqlUsername, String mySqlPassword) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup workers = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(auth, workers)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LengthFieldPrepender(4),   // каждое сообщение клиента содержит заголовок в 4 байта с его длиной
                                    new LengthFieldBasedFrameDecoder(1024*1024*128,0,4),   // от клиента сообщения тоже содержат такой заголовок
                                    new ClientHandler(mySqlUsername, mySqlPassword)   // обработчик тела сообщения от клиента
                            );
                        }
                    });
            ChannelFuture future = b.bind(3000).sync(); // указывается № порта
            System.out.println("Server started");
            future.channel().closeFuture().sync();
            System.out.println("Server finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }

    /**
     * Точка входа
     * @param args в качестве аргументов могут быть переданы логин и пароль для соединения с MySQL -pPassword -uUsername
     */
    public static void main(String[] args) {
        String userName = null;
        String password = null;

        for(String s : args) {
            if(s.startsWith("-u")) {
                userName = s.substring(2);
            } else if(s.startsWith("-p")) {
                password = s.substring(2);
            }
        }

        if (userName == null || password == null) {
            System.out.println("No username and/or password options specified.\n" +
                    "Default username and password will be used for MySql connection.");
        }


        File serverDir = new File("server");  // пред запуском сервера проверяем наличие корневой директории, куда будут помещаться клиентские папки
        if(!serverDir.exists()) {
            serverDir.mkdir();
        }
        if (!serverDir.isDirectory()) {
            System.out.println("Директория 'server' не может быть создана так как уже существует 'server' файл");
            return;
        }
        new NettyServer(userName, password);
    }
}
