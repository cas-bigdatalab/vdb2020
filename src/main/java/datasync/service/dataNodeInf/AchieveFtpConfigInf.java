package datasync.service.dataNodeInf;

import datasync.service.login.GetInfoService;
import datasync.utils.ConfigUtil;

public class AchieveFtpConfigInf {

    private String configPath="../../WEB-INF/config.properties";

    public String getConfigInf(String key){
        String configFilePath = GetInfoService.class.getClassLoader().getResource(configPath).getFile();
        String result= ConfigUtil.getConfigItem(configFilePath, key);

        return result;
    }

}
