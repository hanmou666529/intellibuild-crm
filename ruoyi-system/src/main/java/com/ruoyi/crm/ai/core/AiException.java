package com.ruoyi.crm.ai.core;

public class AiException extends RuntimeException {
    private int code;
    public AiException(String message) { super(message); this.code = 500; }
    public AiException(String message, int code) { super(message); this.code = code; }
    public AiException(String message, Throwable cause) { super(message, cause); this.code = 500; }
    public int getCode() { return code; }
}
