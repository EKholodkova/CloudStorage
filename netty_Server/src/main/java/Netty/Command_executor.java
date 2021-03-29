package Netty;

import Netty.DB_Interface.DB_Handler;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Объект этого класса выполняет обработку пользовательских команд.
 */
public class Command_executor {
    private final DB_Handler handler;
    private Path currentDir = Path.of("server");
    private String userName;

    /**
     * В конструктор передаются параметры для связи с БД, чтобы инициализировать объект DB_Handler,
     * а также имя клиентского пользователя для создания его корневой директории
     * @param userName - (String) логин пользователя клиента
     * @param mySqlUsername - (String) логин от БД
     * @param mySqlPassword - (String) пароль от БД
     * @throws IOException
     */
    public Command_executor(String userName, String mySqlUsername, String mySqlPassword) throws IOException {
        handler = new DB_Handler(mySqlUsername, mySqlPassword);
        currentDir = Paths.get(currentDir + File.separator + userName);
        this.userName = userName;
    }

    /**
     * Регистрация пользователя с проверкой на наличие его в БД.
     * Если таблица не содержит данных, указанных в аргументах метода, пользователь добавляетя в БД,
     * после чего создается корневая директория, соответствующая имени пользователя.
     * @param name - (String) имя пользователя
     * @param password - (String) пароль
     * @return (boolean) если новый пользователь успешно создан - true, в противном случае - false
     * @throws Exception
     */
    protected boolean registerUser(String name, String password) throws Exception {
        ResultSet rs = handler.getUserFromDb(name, password);
        if(!rs.isBeforeFirst()) {
            handler.addUserToDb(name, password);
            if (!Files.exists(currentDir)) {
                Files.createDirectory(currentDir);
            }
            return true;
        }
        return false;
    }

    /**
     * В методе происходит проверка, зарегистрирован ли пользователь
     * @param name - (String) имя пользователя
     * @param password - (String) пароль
     * @return (boolean) если зарегистрирован - true, если нет - false
     * @throws SQLException
     */
    protected boolean loginUser(String name, String password) throws SQLException {
        ResultSet rs = handler.getUserFromDb(name, password);
        return rs.isBeforeFirst();
    }

    /**
     * Замена существующего пароля клиентского пользователя на новый
     * @param name - (String) имя пользователя
     * @param password - (String) текущий пароль
     * @param newPassword - (String) новый пароль
     * @return - (String) отчет о смене пароля
     * @throws SQLException
     */
    protected String changePsw(String name, String password, String newPassword) throws SQLException {
        handler.changeUserData(name, password, newPassword);
        return "Password " + password + " changed to " + newPassword;
    }

    /**
     * Удаление пользователя из БД.
     * Удаление всех его директорий.
     * @param name - (String) имя пользователя
     * @param password - (String) пароль
     * @return - (String) отчет об удалении
     * @throws SQLException
     * @throws IOException
     */
    protected String deleteUser(String name, String password) throws SQLException, IOException {
        handler.removeUserFromDb(name, password);

        Path rootPath = Paths.get(currentDir.toString());
        Files.walkFileTree(rootPath,  new  SimpleFileVisitor<Path>() {
            @Override
            public  FileVisitResult  visitFile (Path file, BasicFileAttributes attrs) throws  IOException {
                System.out.println("delete file: " + file.toString());
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public  FileVisitResult  postVisitDirectory (Path dir, IOException exc)  throws IOException {
                Files.delete(dir);
                System.out.println( "delete dir: "  + dir.toString());
                return  FileVisitResult.CONTINUE;
            }
        });
        System.out.println("Is user home exists " + new File(currentDir.toString()).exists());
        return "User " + name + " was removed successfully";
    }

    /**
     * Поиск файла по всем директориям пользователя.
     * Если разные директории содержат заданный файл,
     * отображаются все места нахождения данного файла.
     * @param fileName - (String) имя искомого файла
     * @return - (String) путь/пути до файла или отчет об отсутствии файла, если файл не найден.
     * @throws IOException
     */
    protected String findFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.walkFileTree(currentDir,  new  SimpleFileVisitor<Path>() {
            @Override
            public  FileVisitResult  visitFile (Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals(fileName)) {
                    sb.append("file found at path: " + file.subpath(2, file.getNameCount()).toString() + "\n");
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if(sb.toString().isEmpty()) {
            return "File not found";
        }
        return sb.toString();
    }

    /**
     * Удаление файла или директории.
     * Удаление директории возможно в случае, если она пустая.
     * @param objName - (String) имя файла или директории
     * @return - (String) отчет об удалении обЪекта
     * @throws IOException
     */
    protected String removeObj(String objName) throws IOException {
        Path path = Path.of( currentDir.toString() + File.separator + objName);
        if(Files.exists(path)) {
            Files.delete(path);
        } else {
            return "There is no such file or directory";
        }
        return "File was deleted";
    }

    /**
     * Создание директории.
     * @param dir - (String) имя директории.
     * @return - (String) отчет о создании папки.
     * @throws IOException
     */
    protected String makeDir(String dir) throws IOException {
        Path path = Path.of(currentDir.toString(), dir);
        if(!Files.exists(path)) {
            Path newDir = Files.createDirectory(path);
            return dir + " was created successfully";
        }
        return dir + " already exists";
    }

    /**
     * Перемещение файла.
     * @param src - (String) исходный файл.
     * @param target - (String) путь и название файла, куда нужно переместить исходный файл.
     * @return - (String) отчет о перемещении.
     * @throws IOException
     */
    protected String moveFile(String src, String target) throws IOException {
        Path sourcePath = Path.of(currentDir.toString() + File.separator + src);
        if(!Files.exists(sourcePath)) {
            return "Source path does not exist";
        }
        Path targetPath = Path.of(currentDir.toString() + File.separator + target);
        if(Files.exists(targetPath)) {
            return "Destination path already exists";
        }
        Files.move(sourcePath, targetPath);
        return "File " + src + " moved to " + target;
    }

    /**
     * Копирование файла. Метод реализует непосредственно копирование файлов,
     * копирование директорий невозможно.
     * @param src - (String) исходный файл.
     * @param target - (String) путь и название файла, куда нужно скоировать исходный файл.
     * @return - (String) отчет о копировании.
     * @throws IOException
     */
    protected String copyFile(String src, String target) throws IOException {
        Path sourcePath = Path.of(currentDir.toString() + File.separator + src);
        if(!Files.exists(sourcePath)) {
            return "Source path does not exist";
        }
        if (Files.isDirectory(sourcePath)) {
            return "Directories copying not available";
        }
        Path targetPath = Path.of(currentDir.toString() + File.separator + target);
        if(Files.exists(targetPath)) {
            return "Destination path already exists";
        }
        Files.copy(sourcePath, targetPath);
        return "Copy was created";
    }

    /**
     * Создает отсортированный по алфавиту список файлов в указанной директории.
     * @param dirName - (String) название директории
     * @return - (String) список файлов. Если в директории файлов нет, строка пустая.
     */
    protected String getFilesList(String dirName) {
        String[] listOfFiles = new File(currentDir.toString() + File.separator + dirName).list();
        StringBuilder sb = new StringBuilder();
        if(listOfFiles != null) {
            Arrays.sort(listOfFiles);
            for(int i = 0; i < listOfFiles.length; i++) {
                if(new File(currentDir.toString() + File.separator + dirName + File.separator + listOfFiles[i]).isDirectory()) {
                    sb.append(listOfFiles[i] + "/" + "\n");
                } else {
                    sb.append(listOfFiles[i] + "\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Создает файл и загружает в него данные, отправленные пользователем.
     * @param fileName - (String) имя загружаемого файла
     * @param fileSize - (long) размер файла
     * @param fileData - (byte[]) данные, содержащиеся в файле
     * @return - (String) отчет о загрузке файла.
     * @throws IOException
     */
    protected String uploadFile(String fileName, long fileSize, byte[] fileData) throws IOException {
        File file = new File(currentDir + File.separator + fileName);
        if(!file.exists()) {
            file.createNewFile();
        } else {
            return "File " + fileName + " already exists";
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileData);
        fos.close();
        return "File " + fileName + ", size: " + fileSize + " was uploaded";
    }

    /**
     * Отправляет содержимое файла клиенту.
     * @param fileName - (String) имя скачиваемого файла
     * @return - (byte[]) файловые данные.
     * @throws IOException
     */
    protected byte[] downloadFile(String fileName) throws IOException {
        byte[] buffer = null;
        File file = new File(currentDir + File.separator + fileName);
        if(file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            buffer = fis.readAllBytes();
            fis.close();
        }
        return buffer;
    }
}
