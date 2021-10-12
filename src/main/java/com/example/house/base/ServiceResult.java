package com.example.house.base;

import lombok.Data;

@Data
public class ServiceResult<T> {
    private boolean success;
    private String message;
    private T result;

    public enum Message {
        NOT_FOUND("Not Found Resource!"),
        NOT_LOGIN("User not login!");
        private final String value;
        Message(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    private ServiceResult() {
    }

    private ServiceResult(boolean success) {
        this.success = success;
    }

    private ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    private ServiceResult(boolean success, String message, T result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }


    public static <T> ServiceResult<T> success() {
        return new ServiceResult<>(true);
    }

    public static <T> ServiceResult<T> notFound() {
        return new ServiceResult<>(
                false, Message.NOT_FOUND.getValue());
    }

    public static <T> ServiceResult<T> of(T result) {
        ServiceResult<T> serviceResult
                = new ServiceResult<>(true);
        serviceResult.setResult(result);
        return serviceResult;
    }
    public static <T> ServiceResult<T> of(T result, String message) {
        ServiceResult<T> serviceResult = new ServiceResult<>(true);
        serviceResult.setResult(result);
        serviceResult.setMessage(message);
        return serviceResult;
    }
    public static <T> ServiceResult<T> of(boolean success, T result, String message) {
        ServiceResult<T> serviceResult = new ServiceResult<>();
        serviceResult.setSuccess(success);
        serviceResult.setResult(result);
        serviceResult.setMessage(message);
        return serviceResult;
    }
    public static <T> ServiceResult<T> of(boolean success, String message) {
        ServiceResult<T> serviceResult = new ServiceResult<>();
        serviceResult.setSuccess(success);
        serviceResult.setMessage(message);
        return serviceResult;
    }
}
