import java.sql.*;

public class SQLHandler {

    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");// driver DB loading in memory
            connection = DriverManager.getConnection("jdbc:sqlite:server/database.db");//connection
            statement = connection.createStatement();//sql-request -> answer from DB

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPassword(String login, String password) {
        try {
            ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE login = '" + login + "' AND password = '" + password + "' ");
            if (rs.next()) {
                return rs.getString("nickname");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean tryToRegister(String login, String password, String nickname) {
        try {
            statement.executeUpdate("INSERT INTO users (login, password, nickname) VALUES ('" + login + "','" + password + "','" + nickname + "')");
            return true;

        } catch (SQLException e) {
            return false;
        }

    }


    public static boolean changeNick(String oldNick, String newNick) {
        try {
            statement.executeUpdate("UPDATE users SET nickname ='" + newNick + "'  WHERE nickname ='" + oldNick + "'");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
