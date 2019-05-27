package datasync.mapper;

import datasync.entity.DataSrc;
import datasync.entity.DataTask;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by xiajl on 2018/9/30 .
 */
public class DataTaskMapperDsName implements RowMapper {
    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        DataTask dataTask = new DataTask();
        dataTask.setDataTaskId(resultSet.getString("DataTaskId"));
        dataTask.setDataTaskName(resultSet.getString("DataTaskName"));
        dataTask.setDataSourceId(resultSet.getInt("DataSourceId"));
        dataTask.setDataTaskType(resultSet.getString("DataTaskType"));
        dataTask.setTableName(resultSet.getString("TableName"));
        dataTask.setSqlString(resultSet.getString("SqlString"));
        dataTask.setSqlTableNameEn(resultSet.getString("SqlTableNameEn"));
        dataTask.setSqlFilePath(resultSet.getString("SqlFilePath"));
        dataTask.setFilePath(resultSet.getString("FilePath"));
        dataTask.setCreator(resultSet.getString("Creator"));
        dataTask.setCreateTime(resultSet.getTimestamp("CreateTime"));
        dataTask.setStatus(resultSet.getString("Status"));
        dataTask.setSubjectCode(resultSet.getString("SubjectCode"));
        dataTask.setLogPath(resultSet.getString("LogPath"));
        dataTask.setRemoteuploadpath(resultSet.getString("RemoteUploadPath"));
        DataSrc dataSrc=new DataSrc();
        dataSrc.setDataSourceName(resultSet.getString("DataSourceName"));
        dataSrc.setDatabaseType(resultSet.getString("DataSourceType"));
        dataSrc.setDataSourceId(resultSet.getInt("DataSourceId"));
        dataTask.setDataSrc(dataSrc);
        return dataTask;
    }
}
