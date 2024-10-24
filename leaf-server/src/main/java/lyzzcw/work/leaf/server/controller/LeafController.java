package lyzzcw.work.leaf.server.controller;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.leaf.core.common.Result;
import lyzzcw.work.leaf.core.common.Status;
import lyzzcw.work.leaf.server.exception.LeafServerException;
import lyzzcw.work.leaf.server.exception.NoKeyException;
import lyzzcw.work.leaf.server.service.SnowflakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LeafController {

    @Autowired
    private SnowflakeService snowflakeService;

    @RequestMapping(value = "/api/snowflake/get/{key}")
    public String getSnowflakeId(@PathVariable("key") String key) {
        return get(key, snowflakeService.getId(key));
    }


    private String get(@PathVariable("key") String key, Result id) {
        Result result;
        if (key == null || key.isEmpty()) {
            throw new NoKeyException();
        }
        result = id;
        if (result.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(result.toString());
        }
        return String.valueOf(result.getId());
    }

    @RequestMapping(value = "/api/snowflake/get")
    public String getSnowflakeId() {
        Result result = snowflakeService.getId();
        if (result.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(result.toString());
        }
        return String.valueOf(result.getId());
    }


}
