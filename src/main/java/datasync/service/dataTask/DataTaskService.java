package datasync.service.dataTask;

import datasync.entity.DataTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DataTaskService {

    public int insertDatatask(DataTask datatask,String connData,String dataSourceName) throws SQLException {
        return new DataTaskDao().insertDatatask(datatask,connData,dataSourceName);
    }

    //获取任务列表
    public List<DataTask> getDataTaskList(Map<Object,Object> params){
        return new DataTaskDao().getDataTaskList(params);
    }

    public DataTask getDataTaskInfById(String id){
        return new DataTaskDao().getDataTaskInfById(id);
    }

    public int deleteTaskById(String taskId){
        return new DataTaskDao().deleteTaskById(taskId);
    }

    public int updateDataTaskStatusById(String taskId,String status){
        return new DataTaskDao().updateDataTaskStatusById(taskId,status);
    }

    public int updateSqlFilePathById(DataTask dataTask){
        return new DataTaskDao().updateSqlFilePathById(dataTask);
    }

    public String updateSqlDataInfById(HttpServletRequest req, HttpServletResponse res) throws IOException {
        return  new DataTaskDao().updateSqlDataInfById(req, res);
    }

    public String uploadTask(HttpServletRequest req, HttpServletResponse res,String dataTaskId) throws IOException {
        return  new DataTaskDao().uploadTask(req, res,dataTaskId);
    }

    public int ftpLocalUpload(HttpServletRequest req, HttpServletResponse res,String taskId) throws IOException {
        return  new DataTaskDao().ftpLocalUpload(req, res,taskId);
    }
}
