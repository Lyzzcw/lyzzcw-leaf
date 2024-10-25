package lyzzcw.work.leaf.core.common;

import lombok.AllArgsConstructor;
import lyzzcw.work.component.domain.common.constant.IStatusCode;

@AllArgsConstructor
public enum Status implements IStatusCode {
    SUCCESS(200, "success"),
    FAIL(999, "fail"),
    ERR_1001(1001, "clock go back"),
    ERR_1002(1002, "method is not support"),
    ERR_1003(1003, "init error"),
    ;

    // 状态码
    private Integer status;

    // 状态码描述
    private String msg;

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
