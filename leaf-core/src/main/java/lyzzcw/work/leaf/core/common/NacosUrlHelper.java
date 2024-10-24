package lyzzcw.work.leaf.core.common;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author lzy
 * @version 1.0
 * Date: 2023/8/11 13:36
 * Description: URL 解析
 */
public class NacosUrlHelper {

    public static final String NAMESPACE = "namespace";
    public static final String SERVER_ADDR = "serverAddr";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static Properties url2Properties (String url,String username, String password) {
        Properties properties = new Properties();
        properties.put(USERNAME,username);
        properties.put(PASSWORD,password);
        String[] parts = url.split("\\?");
        if(parts != null && parts.length > 0){
            properties.setProperty(SERVER_ADDR,parts[0]);
            if(parts.length > 1){
                String[] parameters = parts[1].split("&");
                Arrays.stream(parameters).forEach(item -> {
                    String[] values = item.split("=");
                    if(values[0].equals(NAMESPACE)){
                        properties.setProperty(NAMESPACE,values[1]);
                    }
                });
            }
        }else{
            throw new IllegalArgumentException("error setting properties url: " + url);
        }
        return properties;
    }

}
