package Netty;

import Netty.DB_Interface.DB_Handler;
import commonComponents.Commands;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;


public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final DataOutputStream dos = new DataOutputStream(bos);
    private Command_executor executor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBufInputStream reader = new ByteBufInputStream(buf);
        int msgSize = reader.readInt();
//        System.out.println("размер клиентского сообщения: " + msgSize);
        String command = reader.readUTF();
//        System.out.println("клиентская команда: " + command);
        String userName = reader.readUTF();
//        System.out.println("имя клиента: " + userName);
        String password = reader.readUTF();
//        System.out.println("пароль клиента: " + password);
        executor = new Command_executor(userName);
        if(command.equals(Commands.REGISTER)) {
            System.out.println("если команда register...");
            if(executor.registerUser(userName, password)) {
                System.out.println("если клиент прошел регистрацию");
                dos.writeUTF("Account was successfully registered");
            } else {
                dos.writeUTF("Something went wrong. Try again with new user name and password");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.DELETE_USER)) {
            System.out.println("если команда delete_user...");
            if(executor.loginUser(userName, password)) {
                System.out.println("если зарегистрировались");
                dos.writeUTF(executor.deleteUser(userName, password));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.DELETE_OBJ)) { // currentDir is not working!
            if(executor.loginUser(userName, password)) {
                String objName = reader.readUTF();
                System.out.println("имя папки или файла - " + objName);
                dos.writeUTF(executor.removeObj(objName));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.LIST)) {
            if(executor.loginUser(userName, password)) {
                dos.writeUTF(executor.getFilesList());
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.MAKE_DIR)) {
            if(executor.loginUser(userName, password)) {
                String dir = reader.readUTF();
                System.out.println("имя папки - " + dir);
                dos.writeUTF(executor.makeDir(dir));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.UPLOAD)) {
            if(executor.loginUser(userName, password)) {
                String fileName = reader.readUTF();
                System.out.println("имя загружаемого файла - " + fileName);
 //               dos.writeUTF(executor.uploadFile(fileName));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.DOWNLOAD)) {
            if(executor.loginUser(userName, password)) {
                String fileName = reader.readUTF();
                System.out.println("имя скачиваемого файла - " + fileName);
 //               dos.writeUTF(executor.downloadFile(fileName));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.COPY)) {
            if(executor.loginUser(userName, password)) {
                String source = reader.readUTF();
                String target = reader.readUTF();
                dos.writeUTF(executor.copyFile(source, target));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        buf.release();
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
