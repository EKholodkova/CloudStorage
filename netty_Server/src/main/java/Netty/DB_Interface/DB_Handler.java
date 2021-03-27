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

    public void changeUserData(String userName, String password, String newPassword) throws SQLException {
        String update = "UPDATE " + Constants.USERS_TABLE + " SET " + Constants.USER_PASSWORD +
                " = ? WHERE " + Constants.USER_NAME + " = ? AND " + Constants.USER_PASSWORD + " = ?";
        PreparedStatement ps = getDbConnection().prepareStatement(update);
        ps.setString(1, newPassword);
        ps.setString(2, userName);
        ps.setString(3, password);
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

}
