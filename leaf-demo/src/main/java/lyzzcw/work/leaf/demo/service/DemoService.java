package lyzzcw.work.leaf.demo.service;

import lyzzcw.work.leaf.client.LeafFeign;
import lyzzcw.work.leaf.core.IDGen;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lzy
 * @version 1.0
 * Date: 2024/11/20 15:48
 * Description: No Description
 */
@Service
public class DemoService {
    @Resource
    private IDGen idGen;
    @Resource
    private LeafFeign leafFeign;

    public String getId(){
        try {
            /**
             * 调用方这里可以选择 重试|本地core生成id|抛异常
             */
            return leafFeign.getSnowflakeId();
        }catch (Exception e) {
            return String.valueOf(idGen.get());
        }
    }
}
