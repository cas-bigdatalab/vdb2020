package datasync.mapper;


import datasync.entity.DataSrc;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by huangwei on 2018/9/30.
 */
public class DataSrcMapper implements RowMapper {
    public DataSrc mapRow(ResultSet rs, int rowNum) throws SQLException {
        DataSrc dataSrc = new DataSrc();
        dataSrc.setDataSourceId(rs.getInt("dataSourceId"));
        dataSrc.setDataSourceName(rs.getString("dataSourceName"));
        dataSrc.setDataSourceType(rs.getString("dataSourceType"));
        dataSrc.setDatabaseName(rs.getString("databaseName"));
        dataSrc.setDatabaseType(rs.getString("databaseType"));
        dataSrc.setFilePath(rs.getString("filePath"));
        dataSrc.setFileType(rs.getString("fileType"));
        dataSrc.setHost(rs.getString("host"));
        dataSrc.setPort(rs.getString("port"));
        dataSrc.setIsValid(rs.getString("isValid"));
        dataSrc.setUserName(rs.getString("userName"));
        dataSrc.setPassword(rs.getString("password"));
        dataSrc.setCreateTime(rs.getString("createTime"));
        dataSrc.setStat(rs.getInt("stat"));
        dataSrc.setSubjectCode(rs.getString("SubjectCode"));
        return dataSrc;
    }
}
