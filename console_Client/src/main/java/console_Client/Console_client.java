package console_Client;

import common_Components.Commands;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Клиентское приложение.
 * Включает стартовый метод, но полноценное использование возможно только из командной строки.
 */
public class Console_client {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ByteArrayOutputStream bos;
    private final DataOutputStream dos;

    /**
     * В конструкторе запускается подключение к серверу
     * и инициализируются входящий и исходящий потоки.
     * Клиент завершает работу после выполнения одной введенной команды, поэтому создание
     * нити для взаимодействия с сервером после подключения к нему не требуется.
     * @throws IOException
     */
    public Console_client() throws IOException {
        socket = new Socket("localhost", 3000);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);
    }

    /* Все методы кроме статических реализуют протокол взаимодействия с сервером.
    Сообщения отправляются в следующем формате: (int) размер сообщения, (String) команда,
    (String) имя пользователя, (String) пароль пользователя, (String) параметры команды,
    если они есть (как правило, 1-2), а также (byte[]) данные содержащиеся в файле -
    в случае upload.
    От сервера клиент ожидает (int) размер ответного сообщения, (String) сообщение от сервера.
    Или  (int) размер ответного сообщения, (int) размер файла и (byte[]) данные - в случае
    download.
     */

    /**
     * Отправка команды о регистрации.
     * @param name имя пользователя
     * @param password пароль
     * @throws IOException
     */
    private void doRegister(String name, String password) throws IOException {
        dos.writeUTF(Commands.REGISTER);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды о смене пароля.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void changePsw(String name, String password, List<String> args) throws IOException {
        String newPsw = "";
        if(args.size() == 0) {
            System.out.println("Enter new password");
            return;
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
            return;
        } else {
            newPsw = args.get(0);
        }
        dos.writeUTF(Commands.CHANGE_PASSWORD);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(newPsw);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды об удалении пользователя.
     * @param name
     * @param password
     * @throws IOException
     */
    private void deleteAccount(String name, String password) throws IOException {
        dos.writeUTF(Commands.DELETE_USER);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды о загрузке файла на сервер, отправка содержимого файла.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void sendFile(String name, String password, List<String> args) throws IOException {
        String fileName = "";
        if(args.size() == 0) {
            System.out.println("Enter file name");
            return;
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
            return;
        } else {
            fileName = args.get(0);
        }
        File file = new File(fileName);
        if (file.exists()) {
            dos.writeUTF(Commands.UPLOAD);
            dos.writeUTF(name);
            dos.writeUTF(password);
            dos.writeUTF(file.getName());
            int length = (int)file.length();
            dos.writeInt(length);
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
            String status = in.readUTF();
            System.out.println(status);
        } else {
            System.out.println("File not found");
        }
    }

    /**
     * Отправка команды о скачивании файла из серверного репозитория.
     * Прием и обработка файловых данных.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void getFile(String name, String password, List<String> args) throws IOException {
        String fileName = "";
        if(args.size() == 0) {
            System.out.println("Enter file name");
            return;
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
            return;
        } else {
            fileName = args.get(0);
        }
        File file = new File(fileName);
        if(file.exists()) {
            System.out.println("Local file already exists");
            return;
        }
        Path fName = Path.of(fileName);
        dos.writeUTF(Commands.DOWNLOAD);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(fileName);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        int fileSize = in.readInt();
        if(fileSize > 0) {
            if(fName.getNameCount() > 1) {
                File parent = new File(String.valueOf(fName.getParent()));
                parent.mkdirs();
            }
            file.createNewFile();
            System.out.println("size:" + fileSize);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (fileSize + 255) / 256; i++) {
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
        } else if(fileSize == 0) {
            file.createNewFile();
            System.out.println("Downloaded file " + fileName + " is empty");
        } else {
            System.out.println("File not found");
        }
    }

    /**
     * Отправка команды об удалении объекта.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void removeObj(String name, String password, List<String> args) throws IOException {
        String objName = "";
        if(args.size() == 0) {
            System.out.println("Enter file or directory name");
            return;
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
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
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды о создании директории.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void createDir(String name, String password, List<String> args) throws IOException {
        String pathName = "";
        if(args.size() == 0) {
            System.out.println("Enter directory name");
            return;
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
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
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды об отображении файлов, содержащихся в указанной директории.
     * Если директория не указана, по умолчанию отображаются файлы из корневой папки.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void getFilesList(String name, String password, List<String> args) throws IOException {
        String pathName = "";
        if(args.size() == 0) {
            pathName = "./";
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
            return;
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
        String filesList = in.readUTF();
        System.out.println(filesList);
    }

    /**
     * Отправка команды о перемещении файла.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void moveFile(String name, String password, List<String> args) throws IOException {
        String source = "";
        String target = "";
        if(args.size() <= 1) {
            System.out.println("Enter source path and destination path");
            return;
        } else if(args.size() > 2) {
            System.out.println("Too many arguments");
            return;
        } else {
            source = args.get(0);
            target = args.get(1);
        }
        dos.writeUTF(Commands.MOVE);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(source);
        dos.writeUTF(target);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды о поиске заданного файла.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void findFile(String name, String password, List<String> args) throws IOException {
        String fileName = "";
        if(args.size() == 0) {
            System.out.println("Enter file name");
            return;
        } else if(args.size() > 1) {
            System.out.println("Too many arguments");
            return;
        } else {
            fileName = args.get(0);
        }
        dos.writeUTF(Commands.SEARCH);
        dos.writeUTF(name);
        dos.writeUTF(password);
        dos.writeUTF(fileName);
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
        int answerSize = in.readInt();
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отправка команды о копировании файла.
     * @param name
     * @param password
     * @param args
     * @throws IOException
     */
    private void copyFile(String name, String password, List<String> args) throws IOException {
        String source = "";
        String target = "";
        if(args.size() <= 1) {
            System.out.println("Enter source file and destination file");
            return;
        } else if(args.size() > 2) {
            System.out.println("Too many arguments");
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
        String status = in.readUTF();
        System.out.println(status);
    }

    /**
     * Отображение списка доступных команд.
     */
    static private void getHelp() {
        Map<String, String> helpList = new TreeMap<>();
        helpList.put(Commands.REGISTER, "       create user_account");
        helpList.put(Commands.DELETE_USER, "    delete user_account");
        helpList.put(Commands.UPLOAD, "         upload file");
        helpList.put(Commands.DOWNLOAD, "       download file");
        helpList.put(Commands.LIST, "             view all files from current directory");
        helpList.put(Commands.MAKE_DIR, "          create directory");
        helpList.put(Commands.DELETE_OBJ, "         remove file or directory");
        helpList.put(Commands.COPY, "           copy file");
        helpList.put(Commands.HELP, "           show available commands");
        helpList.put(Commands.CHANGE_PASSWORD, "     change current user's password");
        helpList.put(Commands.MOVE, "           move file from one directory to another");
        helpList.put(Commands.SEARCH, "         search for the specified file in user's directories");

        for(Map.Entry entry : helpList.entrySet()) {
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            System.out.println(key + val);
        }
    }

    /**
     * Закрывает входящий, исходящий потоки.
     * @throws IOException
     */
    private void close() throws IOException {
        in.close();
        out.close();
    }


    /**
     * В качестве аргументов передаются имя, пароль пользователя и команда с соответствующими ей параметрами.
     * В зависимости от введенной команды вызывается подходящий метод объекта Console_client.
     * static метод getHelp() выводится в случае, если аргументов не достаточно, или введена неизвестная команда
     * @param args агрументы командной строки
     * @throws Exception
     */
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

        if(userName == null || password == null || command == null) {
            System.out.println("Login or password is not filled in");
            getHelp();
            return;
        }
        Console_client cc = new Console_client();

        switch (command) {
            case Commands.REGISTER -> cc.doRegister(userName, password);
            case Commands.CHANGE_PASSWORD -> cc.changePsw(userName, password, arguments);
            case Commands.UPLOAD -> cc.sendFile(userName, password, arguments);
            case Commands.DOWNLOAD -> cc.getFile(userName, password, arguments);
            case Commands.DELETE_OBJ -> cc.removeObj(userName, password, arguments);
            case Commands.DELETE_USER -> cc.deleteAccount(userName, password);
            case Commands.COPY -> cc.copyFile(userName, password, arguments);
            case Commands.LIST -> cc.getFilesList(userName, password, arguments);
            case Commands.MAKE_DIR -> cc.createDir(userName, password, arguments);
            case Commands.MOVE -> cc.moveFile(userName, password, arguments);
            case Commands.SEARCH -> cc.findFile(userName, password, arguments);
            default -> getHelp();
        }
        cc.close();
    }
}
