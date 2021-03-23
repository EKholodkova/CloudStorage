package Netty.DB_Interface;

import java.sql.*;

public class DB_Handler {
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123";
    private static final String DB_NAME = "Cloud_Storage_db";

    private Connection dbConnection;

    public Connection getDbConnection() throws SQLException {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        dbConnection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        return dbConnection;
    }

    public void addUserToDb(String userName, String password) throws SQLException {
        String insert = "INSERT INTO " + Constants.USERS_TABLE + " (" +
                Constants.USER_NAME + ", " + Constants.USER_PASSWORD +
                ")" + " VALUES(?,?)";
        PreparedStatement ps = getDbConnection().prepareStatement(insert);
        ps.setString(1, userName);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    public void removeUserFromDb(String userName, String password) throws SQLException {
        String delete = "DELETE FROM " + Constants.USERS_TABLE + " WHERE " +
                Constants.USER_NAME + " = ? AND " + Constants.USER_PASSWORD +
                " = ?";
        PreparedStatement ps = getDbConnection().prepareStatement(delete);
        ps.setString(1, userName);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    public ResultSet getUserFromDb(String userName, String password) throws SQLException {
        ResultSet resSet = null;
        String select = "SELECT * FROM " + Constants.USERS_TABLE + " WHERE " +
                Constants.USER_NAME + " = ? AND " + Constants.USER_PASSWORD + " = ?";
        PreparedStatement ps = getDbConnection().prepareStatement(select);
        ps.setString(1, userName);
        ps.setString(2, password);
        resSet = ps.executeQuery();
        return resSet;
    }


    public static void main(String[] args) {
        String command;
        String username;
        String password;

        for (String s : args) {
            if (s.startsWith("-p")) {
                System.out.println("The password is: " + s.substring(2));
                password = s.substring(2);
            }
        }


        /*try {
            new DB_Handler().removeUserFromDb("vova", "1234");
            System.out.println("removed!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }*/
    }
}
