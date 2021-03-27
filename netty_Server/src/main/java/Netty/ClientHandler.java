package Netty;

import common_Components.Commands;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;


public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final DataOutputStream dos = new DataOutputStream(bos);
    private Command_executor executor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        ByteBufInputStream reader = new ByteBufInputStream(buf);
        int msgSize = reader.readInt();
        String command = reader.readUTF();
        String userName = reader.readUTF();
        String password = reader.readUTF();
        executor = new Command_executor(userName);
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
            if(executor.loginUser(userName, password)) {
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



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
