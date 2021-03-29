package Netty;

import common_Components.Commands;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * Класс - обработчик тела клиентского сообщения. Является наследником ChannelInboundHandlerAdapter,
 * входящий хендлер конвеера Netty сервера.
 */

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final DataOutputStream dos = new DataOutputStream(bos);
    private Command_executor executor;
    private final String mySqlUsername;
    private final String mySqlPassword;

    ClientHandler(String username, String password) {
        super();
        mySqlUsername = username;
        mySqlPassword = password;
    }

    /**
     * В методе обрабатываются данные, пришедшие со стороны клиента, согласно установленному протоколу.
     * Порядок считываемых данных: (int) размер сообщения, (String) команда, (String) имя пользователя, (String) пароль пользователя.
     * Значение команды задает количество аргументов, считываемых после пароля.
     * Как только необходимые данные получены, обработка команд передается объекту класса Command_executor.
     * Результаты его работы отправляются клиенту.
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBufInputStream reader = new ByteBufInputStream(buf);
        int msgSize = reader.readInt();
        String command = reader.readUTF();
        String userName = reader.readUTF();
        String password = reader.readUTF();
        executor = new Command_executor(userName, mySqlUsername, mySqlPassword);
        if(command.equals(Commands.REGISTER)) {
            if(executor.registerUser(userName, password)) {
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
        if(command.equals(Commands.CHANGE_PASSWORD)) {
            if(executor.loginUser(userName, password)) {   // все команды, кроме register, требуют предварительной проверки на наличие пользователя в БД
                String newPassword = reader.readUTF();
                dos.writeUTF(executor.changePsw(userName, password, newPassword));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.DELETE_USER)) {
            if(executor.loginUser(userName, password)) {
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
        if(command.equals(Commands.DELETE_OBJ)) {
            if(executor.loginUser(userName, password)) {
                String objName = reader.readUTF();
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
                String dirName = reader.readUTF();
                dos.writeUTF(executor.getFilesList(dirName));
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
                int fileSize = reader.readInt();
                byte[] fileData = reader.readAllBytes();
                dos.writeUTF(executor.uploadFile(fileName, fileSize, fileData));
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
                byte[] fileData = executor.downloadFile(fileName);
                int fileSize = -1;
                if(fileData != null) {
                    fileSize = fileData.length;
                }
                dos.writeInt(fileSize);
                if(fileSize > 0) {
                    dos.write(fileData);
                }
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.MOVE)) {
            if(executor.loginUser(userName, password)) {
                String source = reader.readUTF();
                String target = reader.readUTF();
                dos.writeUTF(executor.moveFile(source, target));
            } else {
                dos.writeUTF("Account not found");
            }
            dos.flush();
            ctx.writeAndFlush(Unpooled.copiedBuffer(bos.toByteArray())).addListener(future -> {
                System.out.println("send");
                ctx.close();
            });
        }
        if(command.equals(Commands.SEARCH)) {
            if(executor.loginUser(userName, password)) {
                String fileName = reader.readUTF();
                dos.writeUTF(executor.findFile(fileName));
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

    /**
     * Обработка возможных исключений.
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
