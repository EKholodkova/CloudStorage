package Netty;

import Netty.DB_Interface.DB_Handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLOutput;

public class Command_executor {
    private final DB_Handler handler = new DB_Handler();
    private Path currentDir = Path.of("server");
    private String userName;

    public Command_executor(String userName) throws IOException {
        this.userName = userName;
        currentDir = Paths.get(currentDir + File.separator + userName);
        if(!Files.exists(currentDir)) {
            Files.createDirectory(currentDir);
        }
    }

    protected boolean registerUser(String name, String password) throws SQLException {
        ResultSet rs = handler.getUserFromDb(name, password);
        if(!rs.isBeforeFirst()) {
            handler.addUserToDb(name, password);
            return true;
        }
        return false;
    }

    protected boolean loginUser(String name, String password) throws SQLException {
        ResultSet rs = handler.getUserFromDb(name, password);
        return rs.isBeforeFirst();
    }

    protected String deleteUser(String name, String password) throws SQLException, IOException {
        System.out.println("мы в deleteUser");
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
        System.out.println("юзер удален");
        return "User " + name + " was removed successfully";
    }

    protected String removeObj(String objName) throws IOException {
        Path path = Path.of( currentDir.toString() + File.separator + objName);
        if(Files.exists(path)) {
            Files.delete(path);
        } else {
            return "There is no such file or directory";
        }
        return "File was deleted";
    }

    protected String makeDir(String dir) throws IOException {
        System.out.println("мы в методе makeDir");
        System.out.println(currentDir.toString());
        Path path = Path.of(currentDir.toString(), dir);
        System.out.println("задаем название директории - " + path.toString());
        if(!Files.exists(path)) {
            System.out.println("если такой директории нет...");
            Path newDir = Files.createDirectory(path);
            System.out.println("создаем такую директорию");
            return dir + " was created successfully";
        }
        return dir + " already exists";
    }

    protected String copyFile(String src, String target) throws IOException {
        Path sourcePath = Path.of(currentDir.toString() + File.separator + src);
        if(!Files.exists(sourcePath)) {
            return "Source path does not exist";
        }
        Path targetPath = Path.of(currentDir.toString() + File.separator + target);
        if(Files.exists(targetPath)) {
            return "Destination path already exists";
        }
        Files.copy(sourcePath, targetPath);
        return "Copy was created";
    }

    protected String getFilesList() {
        return String.join("\t", new File(currentDir.toString()).list());
    }
}
