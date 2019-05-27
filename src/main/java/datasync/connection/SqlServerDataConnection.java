package datasync.connection;

import java.sql.*;

public class SqlServerDataConnection {
    public static Connection makeConn(String dataConnParams) throws ClassNotFoundException, SQLException {
        String [] dataArray=dataConnParams.replace("$",",").split(",");
        String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String url = "jdbc:sqlserver://"+dataArray[1]+":1433;DatabaseName="+dataArray[6]+";";
        String user = dataArray[3];
        String password = dataArray[4];
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
}
