package com.atguigu.gmall.common.exception;

public class CartException extends  RuntimeException {
    public CartException() {
        super();
    }

    public CartException(String message) {
        super(message);
    }
}
