package lyzzcw.work.leaf.core.exception;

import lombok.Data;
import lyzzcw.work.component.domain.common.constant.BaseStatusCode;
import lyzzcw.work.component.domain.common.constant.HttpStatus;
import lyzzcw.work.component.domain.common.constant.IStatusCode;
import lyzzcw.work.component.domain.common.exception.BaseException;

/**
 * Created with IntelliJ IDEA.
 * @author: lzy
 * Date: 2021/12/9
 * Time: 16:07
 * Description: No Description
 */
@Data
public class LeafException extends RuntimeException {
    /**
     * 错误码
     */
    private Integer error;

    /**
     * 错误描述
     */
    private String msg;

    /**
     * 错误后响应的信息
     */
    private Object data;

    public LeafException() {
        super();
    }
    /**
     * 根据错误码实例化异常
     *
     * @param statusCode 自定义错误码
     */
    public LeafException(IStatusCode statusCode) {
        // 校验是否传递了异常码
        if (null == statusCode) {
            // 如果没有统一设置为未知错误
            setInfo(BaseStatusCode.ERR_9999);
        } else {
            setInfo(statusCode);
        }
    }

    /**
     * 根据http状态码抛出异常
     *
     * @param httpStatus http状态码
     */
    public LeafException(HttpStatus httpStatus) {
        if (null == httpStatus) {
            // 没有传递默认使用 未知异常
            setInfo(BaseStatusCode.ERR_9999);
        } else {
            setInfo(httpStatus);
        }
    }

    /**
     * 根据错误码实例化异常 并返回数据
     *
     * @param statusCode 自定义错误码
     * @param data       数据
     */
    public LeafException(IStatusCode statusCode, Object data) {
        // 校验是否传递了异常码
        if (null == statusCode) {
            // 如果没有统一设置为未知错误
            setInfo(BaseStatusCode.ERR_9999);
        } else {
            setInfo(statusCode);
        }
        // 校验数据是否为null
        if (null != data) {
            this.data = data;
        }
    }

    /**
     * 根据http的状态码实例化异常 并返回数据
     *
     * @param httpStatus http状态码
     * @param data       数据
     */
    public LeafException(HttpStatus httpStatus, Object data) {
        // 校验是否传递了异常码
        if (null == httpStatus) {
            // 如果没有统一设置为未知错误
            setInfo(BaseStatusCode.ERR_9999);
        } else {
            setInfo(httpStatus);
        }
        // 校验数据是否为null
        if (null != data) {
            this.data = data;
        }
    }

    public LeafException(Integer error, String msg) {
        super(msg);
        this.error = error;
        this.msg = msg;
        doFillInStackTrace();
    }

    public LeafException(String msg) {
        super(msg);
        doFillInStackTrace();
    }


    /**
     * 设置状态码及描述信息
     * 内部使用的方法
     *
     * @param statusCode
     */
    private void setInfo(IStatusCode statusCode) {
        if (null != statusCode) {
            this.error = statusCode.getStatus();
            this.msg = statusCode.getMsg();
        }
    }

    /**
     * 根据HttpStatus设置属性
     * @param httpStatus
     */
    private void setInfo(HttpStatus httpStatus) {
        if (null != httpStatus) {
            this.error = httpStatus.value();
            this.msg = httpStatus.getReasonPhrase();
        }
    }


    /**
     * 提高性能
     * @return Throwable
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public Throwable doFillInStackTrace() {
        return super.fillInStackTrace();
    }
}
