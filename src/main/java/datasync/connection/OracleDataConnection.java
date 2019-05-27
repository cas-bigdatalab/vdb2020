package datasync.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDataConnection {

    public static Connection makeConn(String dataConnParams) throws ClassNotFoundException, SQLException {
        String [] dataArray=dataConnParams.replace("$",",").split(",");
        String driver="oracle.jdbc.driver.OracleDriver";
        String url="jdbc:oracle:thin:@"+dataArray[1]+":1521:"+dataArray[6]+"";
        String user = dataArray[3];
        String password = dataArray[4];
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
}
