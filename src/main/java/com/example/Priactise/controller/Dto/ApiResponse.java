package com.example.Priactise.controller.Dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String status;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setStatus("success");
        r.setData(data);
        return r;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setStatus("error");
        r.setError(message);
        return r;
    }
}
