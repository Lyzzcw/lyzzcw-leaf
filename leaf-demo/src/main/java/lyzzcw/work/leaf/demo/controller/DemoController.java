package lyzzcw.work.leaf.demo.controller;

import lyzzcw.work.component.domain.common.entity.Result;
import lyzzcw.work.leaf.demo.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lzy
 * @version 1.0
 * Date: 2024/11/20 15:47
 * Description: No Description
 */
@RestController
public class DemoController {

    @Resource
    private DemoService demoService;

    @GetMapping("test")
    public Result<String> getId(){
        return Result.ok(demoService.getId());
    }
}
