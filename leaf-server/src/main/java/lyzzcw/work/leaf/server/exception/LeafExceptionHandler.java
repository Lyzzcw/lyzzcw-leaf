package lyzzcw.work.leaf.server.exception;


import lombok.extern.slf4j.Slf4j;
import lyzzcw.work.component.domain.common.entity.Result;
import lyzzcw.work.leaf.core.exception.LeafException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)//order拦截顺序
@Slf4j
public class LeafExceptionHandler {

    @ExceptionHandler(LeafException.class)
    public Result handleAllExceptions(LeafException demoException) {
        return Result.fail(demoException.getMsg(),demoException.getError());
    }


}
