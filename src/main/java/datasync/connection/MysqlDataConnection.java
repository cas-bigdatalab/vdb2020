package datasync.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDataConnection {

    /**
     *
     * @param dataConnParams
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection makeConn(String dataConnParams) throws ClassNotFoundException, SQLException {
        String [] dataArray=dataConnParams.replace("$",",").split(",");
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://"+dataArray[1]+":"+dataArray[2]+"/"+dataArray[6]+"?useUnicode=true&characterEncoding=UTF-8&createDatabaseIfNotExist=true";
        String user = dataArray[3];
        String password = dataArray[4];
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
}
