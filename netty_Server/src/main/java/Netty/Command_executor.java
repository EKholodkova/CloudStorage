package Netty;

import Netty.DB_Interface.DB_Handler;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

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

    protected String changePsw(String name, String password, String newPassword) throws SQLException {
        handler.changeUserData(name, password, newPassword);
        return "Password " + password + " changed to " + newPassword;
    }

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
        Path path = Path.of(currentDir.toString(), dir);
        if(!Files.exists(path)) {
            Path newDir = Files.createDirectory(path);
            return dir + " was created successfully";
        }
        return dir + " already exists";
    }

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
