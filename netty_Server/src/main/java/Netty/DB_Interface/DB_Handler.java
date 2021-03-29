package Netty.DB_Interface;

import java.sql.*;

/**
* Класс для установки соединения с базой данных MySQL.
* Содержит методы для необходимых манипуляций с данными пользователей.
*/

public class DB_Handler {
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private final String DB_USER;
    private final String DB_PASSWORD;
    private static final String DB_NAME = "Cloud_Storage_db";

    private Connection dbConnection;

    public DB_Handler(String username, String password) {
        if (username != null && password != null) {
            DB_USER = username;
            DB_PASSWORD = password;
        } else {
            DB_USER = "root";
            DB_PASSWORD = "123";
        }
    }

    /**
     * Метод устанавливает соединение с БД.
     * @return - возвращает объект Connection.
     * @throws SQLException
     */
    public Connection getDbConnection() throws SQLException {
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        dbConnection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        return dbConnection;
    }

    /**
     * Метод добавляет данные пользователя в таблицу БД.
     * @param userName - (String) имя пользователя
     * @param password - (String) пароль пользователя
     * @throws SQLException
     */
    public void addUserToDb(String userName, String password) throws SQLException {
        String insert = "INSERT INTO " + Constants.USERS_TABLE + " (" +
                Constants.USER_NAME + ", " + Constants.USER_PASSWORD +
                ")" + " VALUES(?,?)";
        PreparedStatement ps = getDbConnection().prepareStatement(insert);
        ps.setString(1, userName);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    /**
     * Метод удаляет данные пользователя из таблицы БД.
     * @param userName - (String) имя пользователя
     * @param password - (String) пароль пользователя
     * @throws SQLException
     */
    public void removeUserFromDb(String userName, String password) throws SQLException {
        String delete = "DELETE FROM " + Constants.USERS_TABLE + " WHERE " +
                Constants.USER_NAME + " = ? AND " + Constants.USER_PASSWORD +
                " = ?";
        PreparedStatement ps = getDbConnection().prepareStatement(delete);
        ps.setString(1, userName);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    /**
     * Метод устанавливает новое значение пароля пользователя в таблице БД.
     * @param userName - (String) имя пользователя
     * @param password - (String) текущий пароль пользователя
     * @param newPassword - (String) новый пароль пользователя
     * @throws SQLException
     */
    public void changeUserData(String userName, String password, String newPassword) throws SQLException {
        String update = "UPDATE " + Constants.USERS_TABLE + " SET " + Constants.USER_PASSWORD +
                " = ? WHERE " + Constants.USER_NAME + " = ? AND " + Constants.USER_PASSWORD + " = ?";
        PreparedStatement ps = getDbConnection().prepareStatement(update);
        ps.setString(1, newPassword);
        ps.setString(2, userName);
        ps.setString(3, password);
        ps.executeUpdate();
    }

    /**
     * Метод отображает список пользователей, соответствующих заданным параметрам - имя/пароль.
     * Если совпадений не нашлось, список будет пустым.
     * @param userName - (String) имя пользователя
     * @param password - (String) пароль пользователя
     * @return объект ResultSet.
     * @throws SQLException
     */
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
