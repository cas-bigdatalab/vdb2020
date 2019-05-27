package datasync.service.settingTask;

import datasync.connection.MysqlDataConnection;
import datasync.connection.OracleDataConnection;
import datasync.connection.SqlServerDataConnection;
import vdb.metacat.Repository;
import vdb.mydb.VdbManager;
import vdb.mydb.engine.VdbEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataConnDaoService {

    //oracle
     public List<Object>  getOracleTablesNameList(String dataConnParams) throws ClassNotFoundException, SQLException {
         String [] dataArray=dataConnParams.replace("$",",").split(",");
         Connection conn = new OracleDataConnection().makeConn(dataConnParams);
         // 获取所有表名
         Statement statement = conn.createStatement();
         List<Object> list=getTablesNames(conn,dataArray[3],"oracle");
         statement.close();
         conn.close();
         return list;
     }

    //mysql
    public List<Object>  getMysqlTablesNameList(String dataConnParams) throws ClassNotFoundException, SQLException {
        String [] dataArray=dataConnParams.replace("$",",").split(",");
        Connection conn = new MysqlDataConnection().makeConn(dataConnParams);
        // 获取所有表名
        Statement statement = conn.createStatement();
        List<Object> list=getTablesNames(conn,dataArray[3],"mysql");
        statement.close();
        conn.close();
        return list;
    }

    //sqlserver
    public List<Object>  getSqlServerTablesNameList(String dataConnParams) throws ClassNotFoundException, SQLException {
        String [] dataArray=dataConnParams.replace("$",",").split(",");
        Connection conn = new SqlServerDataConnection().makeConn(dataConnParams);
        // 获取所有表名
        Statement statement = conn.createStatement();
        List<Object> list=getTablesNames(conn,dataArray[6],"mssqlserver");
        statement.close();
        conn.close();
        return list;
    }

    //判断数据库类型
    public String judgeDataType(String dataConn) {
        String [] array=dataConn.replace("$","','").split(",");
        String result="mysql";
        if ("'oracle'".equals(array[5])) {
            result="oracle";
        }else if ("'mysql'".equals(array[5])) {
            //  jdbc:mysql://127.0.0.1:3306/mysql?useUnicode=true&characterEncoding=UTF-8&createDatabaseIfNotExist=true
              result="mysql";
        }else if ("'mssql'".equals(array[5])){//sqlserver数据库
            result="sqlServer";
        }
        return result;
    }

    //获取数据库表名
    public List<Object> getTablesNames(Connection conn,String dataName,String dataType) throws SQLException {
        List<Object> list= new ArrayList<Object>();//
        DatabaseMetaData dbMetData = conn.getMetaData();
        //获取数据库对象
        ResultSet rs = dbMetData.getTables(null,
                convertDatabaseCharsetType(dataName, dataType), null,new String[] { "TABLE", "VIEW" });
        while (rs.next()) {
            if (rs.getString(4) != null
                    && (rs.getString(4).equalsIgnoreCase("TABLE") )) {//|| rs.getString(4).equalsIgnoreCase("VIEW")
                String tableName = rs.getString(3).toLowerCase();
                list.add(tableName);
                System.out.print(" "+tableName);
            }
        }
        return list;
    }

    public static String convertDatabaseCharsetType(String in, String type) {
        String dbUser;
        if (in != null) {
            if (type.equals("oracle")) {
                dbUser = in.toUpperCase();
            } else if (type.equals("postgresql")) {
                dbUser = "public";
            } else if (type.equals("mysql")) {
                dbUser = null;
            } else if (type.equals("mssqlserver")) {
                dbUser = null;
            } else if (type.equals("db2")) {
                dbUser = in.toUpperCase();
            } else {
                dbUser = in;
            }
        } else {
            dbUser = "public";
        }
        return dbUser;
    }

    public  List<Object> searchDataListImp(HttpServletRequest req, HttpServletResponse res){
        List<Object> list=new ArrayList<Object>();
        VdbManager vdbManager=new VdbManager();
        VdbEngine vdbEngine=vdbManager.getInstance();
        int vdbLength=VdbManager.getInstance().getDomain().getDataSets().length;
//        List<FileRepository> repositories = new ArrayList<FileRepository>();
//        RepositoryManager rm = new RepositoryManager(); // rm.setRepositories();
//        RepositoriesService rs=new RepositoriesService();//getAllRepositories
//        List<FileRepository> fileRepository=rs.getAllRepositories("localhost.gem");
        String vdbEntryStr="";
        for (int i=0;i<vdbLength;i++){
            Repository repository=vdbEngine.getDomain().getDataSets()[i].getRepository();
            if(repository!=null && ("mysql".equals(String.valueOf(repository.getProductName())) || "oracle".equals(String.valueOf(repository.getProductName())))){
                String title=String.valueOf(vdbEngine.getDomain().getDataSets()[i].getTitle()).replaceAll("null","");
                String host=String.valueOf(repository.get("host")).replaceAll("null","");
                String port=String.valueOf(repository.get("port")).replaceAll("null","");
                String username=String.valueOf(repository.get("username")).replaceAll("null","");
                String password=String.valueOf(repository.get("userPass")).replaceAll("null","");
                String productName=String.valueOf(repository.get("productName")).replaceAll("null","");//数据库type
                String databaseName=String.valueOf(repository.get("databaseName")).replaceAll("null","");//数据库实例oracle/数据库名称mysql
                String dataStr=title+"$"+host+"$"+port+"$"+username+"$"+password+"$"+productName+"$"+databaseName;//以$隔断数据库信息
                list.add(dataStr);
            }else{

            }
        }
        return list;

    }

    public  List<Map<Object,Object>> searchDataBySql(String connData,String sql) throws SQLException, ClassNotFoundException {
        Statement statement = null;
        List<Map<Object,Object>> list=new ArrayList<Map<Object, Object>>();
        List<Map<Object,Object>> columnsList=new ArrayList<Map<Object, Object>>();
        List<Object> list2=new ArrayList<Object>();
        Map<Object,Object> map=new HashMap<Object, Object>();

        Map<Object,Object> columnsMap=new HashMap<Object, Object>();
        String dataType=judgeDataType(connData);//检查数据库类型
        Connection conn=null;
        if("mysql".equals(dataType)){
            conn=new MysqlDataConnection().makeConn(connData);
        }else if("oracle".equals(dataType)){
            conn=new OracleDataConnection().makeConn(connData);
        }else if ("sqlServer".equals(dataType)){
            conn=new SqlServerDataConnection().makeConn(connData);
        }
        statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        ResultSetMetaData ramd = rs.getMetaData();
        for(int i=1;i<=ramd.getColumnCount();i++){//获取列的数量
            map.put(i,ramd.getColumnName(i));//将列放到map中（1，columsName）
            list2.add("{field:\""+ramd.getColumnName(i)+"\"");
            list2.add("title:\""+ramd.getColumnName(i)+"\"}");
        }
       // System.out.println(list2.toString());
        while(rs.next()){
            Map<Object,Object> map2=new HashMap<Object, Object>();
            for(int j=1;j<=map.size();j++){
                map2.put("\""+map.get(j)+"\"","\""+rs.getString((String) map.get(j))+"\"");
            }
                list.add(map2);
        }
        columnsMap.put("columnsList",list2);
        list.add(columnsMap);
        conn.close();

        return  list;

    }

    public String checkSql(String sql,String connDataValue) {
        String dataType=judgeDataType(connDataValue);
        Statement statement = null;
        Connection conn = null;
         try {
             if("mysql".equals(dataType)){
                     conn=new MysqlDataConnection().makeConn(connDataValue);
             }
             if("oracle".equals(dataType)){
                 conn=new OracleDataConnection().makeConn(connDataValue);
             }
             if("sqlServer".equals(dataType)){
                 conn=new SqlServerDataConnection().makeConn(connDataValue);
             }
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             return  "数据库连接失败！";
          } catch (SQLException e) {
             e.printStackTrace();
             return  "数据库连接失败！";
         }

         String [] sqlArray=sql.split(";");

        try {
            statement = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库连接错误！";
        }
        for (int i=0;i<sqlArray.length;i++){
            try {
                ResultSet rs = statement.executeQuery(sqlArray[i]);
            } catch (SQLException e) {
                e.printStackTrace();
                return  i+"？sql语句错误！";//用？分割，定位那个sql出现问题
            }
         }
        return "success";
    }



}
