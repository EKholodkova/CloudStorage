package consoleClient;

import commonComponents.Commands;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Console_client {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ByteArrayOutputStream bos;
    private final DataOutputStream dos;

    public Console_client() throws IOException {
        socket = new Socket("localhost", 3000);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);

    }

    private void doRegister(String name, String password) throws IOException {
        dos.writeUTF(Commands.REGISTER); // command
        System.out.println("записали команду в клиентский буфер");
        dos.writeUTF(name);              // username
        System.out.println("записали имя в клиентский буфер");
        dos.writeUTF(password);          // password
        System.out.println("записали пароль в клиентский буфер");
        dos.flush();
        System.out.println("слили в bos");
        int msgSize = bos.size();
        System.out.println("размер сообщения: " + msgSize);
        out.writeInt(msgSize);
        System.out.println("слили в out msgSize");
        bos.writeTo(out);
        System.out.println("слили клиентское сообщение в out");

        int answerSize = in.readInt();
        System.out.println(answerSize);
        String status = in.readUTF();
        System.out.println(status);
//        if(status.equals("Data added to database")) {
//            System.out.println("Registration successful!");
//        } else {
//            System.out.println("Registration failed. Try again");
//        }
    }

    private void deleteAccount(String name, String password) throws IOException {
        dos.writeUTF(Commands.DELETE_USER); // command
        dos.writeUTF(name);                 // username
        dos.writeUTF(password);             // password
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        System.out.println(answerSize);
        String status = in.readUTF();
        System.out.println(status);
    }

//    private boolean doLogin(String name, String password) throws SQLException{
//        DB_Handler handler = new DB_Handler();
//        ResultSet rs = handler.getUserFromDb(name, password);
//        return rs.isBeforeFirst();
//    }

    private void sendFile(String name, String password, List<String> args) throws IOException {
        String fileName = "";
        if(args.size() == 0) {
            System.out.println("Enter file name");
            return;
        } else {
            fileName = args.get(0);
        }
        File file = new File(fileName);
        if (file.exists()) {
            dos.writeUTF(Commands.UPLOAD);  //command
            dos.writeUTF(name);             //username
            dos.writeUTF(password);         //password
            dos.writeUTF(fileName);         //filename
            long length = file.length();
            dos.writeLong(length);          //fileSize
            FileInputStream fis = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[256];
            while ((read = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, read);  //data
            }
            dos.flush();
            int msgSize = bos.size();
            out.writeInt(msgSize);
            bos.writeTo(out);              //msgSize, command, username, password, filename, fileSize, fileData
            int answerSize = in.readInt();
            System.out.println(answerSize);
            String status = in.readUTF();
            System.out.println(status);
        } else {
            System.out.println("File not found");
        }
    }

    private void getFile(String name, String password, List<String> args) throws IOException {
        String fileName = "";
        if(args.size() == 0) {
            System.out.println("Enter file name");
            return;
        } else {
            fileName = args.get(0);
        }
        dos.writeUTF(Commands.DOWNLOAD);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(fileName);
        File file = new File(fileName);
        if (!file.exists()) {  // TODO: fix it
            file.createNewFile();
//            System.out.println("file created");
        }
        int responseCode = in.readInt();
        if(responseCode == 0) {
            long size = in.readLong();
            System.out.println("size:" + size);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (size + 255) / 256; i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
        } else {
            System.out.println("Unknown error");
        }
    }

    private void removeObj(String name, String password, List<String> args) throws IOException {
        String objName = "";
        if(args.size() == 0) {
            System.out.println("Enter file or directory name");
            return;
        } else {
            objName = args.get(0);
        }
        dos.writeUTF(Commands.DELETE_OBJ);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(objName);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        System.out.println(answerSize);
        String status = in.readUTF();
        System.out.println(status);
    }

    private void createDir(String name, String password, List<String> args) throws IOException {
        String pathName = "";
        if(args.size() == 0) {
            System.out.println("Enter directory name");
            return;
        } else {
            pathName = args.get(0);
        }
        dos.writeUTF(Commands.MAKE_DIR);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(pathName);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        System.out.println(answerSize);
        String status = in.readUTF();
        System.out.println(status);
    }

    private void getFilesList(String name, String password, List<String> args) throws IOException {
        String pathName = "";
        if(args.size() == 0) {
            pathName = "./";
        } else {
            pathName = args.get(0);
        }
        dos.writeUTF(Commands.LIST);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(pathName);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        System.out.println(answerSize);
        String filesList = in.readUTF();
        System.out.println(filesList);
    }

    private void copyFile(String name, String password, List<String> args) throws IOException {
        String source = "";
        String target = "";
        if(args.size() <= 1) {
            System.out.println("Enter target file name and destination file name");
            return;
        } else {
            source = args.get(0);
            target = args.get(1);
        }
        dos.writeUTF(Commands.COPY);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(source);
        dos.writeUTF(target);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        System.out.println(answerSize);
        String status = in.readUTF();
        System.out.println(status);
    }

    private void getHelp() {
        Map<String, String> helpList = new TreeMap<>();
        helpList.put(Commands.REGISTER, "create user_account");
        helpList.put(Commands.DELETE_USER, "delete user_account");
        helpList.put(Commands.UPLOAD, "upload file");
        helpList.put(Commands.DOWNLOAD, "download file");
        helpList.put(Commands.LIST, "view all files from current directory");
        helpList.put(Commands.MAKE_DIR, "create directory");
        helpList.put(Commands.DELETE_OBJ, "remove file or directory");
        helpList.put(Commands.COPY, "copy file");
        helpList.put(Commands.HELP, "show available commands");

        for(Map.Entry entry : helpList.entrySet()) {
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            System.out.println(key + " - " + val);
        }
    }

    private void close() throws IOException {
        in.close();
        out.close();
    }


    public static void main(String[] args) throws Exception {
        String userName = null;
        String password = null;
        String command = null;
        List<String> arguments = new ArrayList<>();

        for(String s : args) {
            if(s.startsWith("-u")) {
                userName = s.substring(2);
            } else if(s.startsWith("-p")) {
                password = s.substring(2);
            } else {
                if(command == null) {
                    command = s;
                } else {
                    arguments.add(s);
                }
            }
        }
//        System.out.println(userName);
//        System.out.println(password);
//        System.out.println(command);
//        System.out.println(arguments.toString());

        if(userName == null || password == null) {
            System.out.println("Login or password is not filled in");
            new Console_client().getHelp();
            return;
        }
        Console_client cc = new Console_client();

        switch (command) {
            case Commands.REGISTER -> cc.doRegister(userName, password);
            case Commands.UPLOAD -> cc.sendFile(userName, password, arguments);
            case Commands.DOWNLOAD -> cc.getFile(userName, password, arguments);
            case Commands.DELETE_OBJ -> cc.removeObj(userName, password, arguments);
            case Commands.DELETE_USER -> cc.deleteAccount(userName, password);
            case Commands.COPY -> cc.copyFile(userName, password, arguments);
            case Commands.LIST -> cc.getFilesList(userName, password, arguments);
            case Commands.MAKE_DIR -> cc.createDir(userName, password, arguments);
            default -> cc.getHelp();
        }
        cc.close();
    }
}
