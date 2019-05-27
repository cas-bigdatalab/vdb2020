package datasync.entity;

import java.util.Date;


public class DataTask {
    private String dataTaskId;
    private int dataSourceId;
    private String dataTaskName;
    private String dataTaskType;
    private String tableName;
    private String sqlString;
    private String sqlTableNameEn;
    private String sqlFilePath;
    private String filePath;
    private String subjectCode;
    private Date createTime;
    private String creator;
    private String status;
    private String LogPath;
    private DataSrc dataSrc;
    private String remoteuploadpath;

    public String getRemoteuploadpath() {
        return remoteuploadpath;
    }

    public void setRemoteuploadpath(String remoteuploadpath) {
        this.remoteuploadpath = remoteuploadpath;
    }



    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public DataSrc getDataSrc() {
        return dataSrc;
    }

    public void setDataSrc(DataSrc dataSrc) {
        this.dataSrc = dataSrc;
    }

    public String getDataTaskName() {
        return dataTaskName;
    }

    public void setDataTaskName(String dataTaskName) {
        this.dataTaskName = dataTaskName;
    }

    public String getDataTaskType() {
        return dataTaskType;
    }

    public void setDataTaskType(String dataTaskType) {
        this.dataTaskType = dataTaskType;
    }

    public String getDataTaskId() {
        return dataTaskId;
    }

    public void setDataTaskId(String dataTaskId) {
        this.dataTaskId = dataTaskId;
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public String getSqlTableNameEn() {
        return sqlTableNameEn;
    }

    public void setSqlTableNameEn(String sqlTableNameEn) {
        this.sqlTableNameEn = sqlTableNameEn;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSqlFilePath() {
        return sqlFilePath;
    }

    public void setSqlFilePath(String sqlFilePath) {
        this.sqlFilePath = sqlFilePath;
    }

    public String getLogPath() {
        return LogPath;
    }

    public void setLogPath(String logPath) {
        LogPath = logPath;
    }
}
