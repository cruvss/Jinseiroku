package com.cruvs.backend.util;
import com.cruvs.backend.response.ApiResponse;

public class ApiResponseUtil {
    private ApiResponseUtil(){}

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(200,message,data);
    }

    public static <T> ApiResponse<T> created(String message, T data){
        return new ApiResponse<>(201,message,data);
    }

    public static <T> ApiResponse<T> badRequest(String message, T data){
        return new ApiResponse<>(400, message,null);
    }

    public static <T> ApiResponse<T> unauthorized(String message){
        return new ApiResponse<>(401,message,null);
    }

    public static <T> ApiResponse<T> forbidden(String message){
        return new ApiResponse<>(403,message,null);
    }

    public static <T> ApiResponse<T> notFound(String message){
        return new ApiResponse<>(404,message,null);
    }

    public static <T> ApiResponse<T> serverError(String message){
        return new ApiResponse<>(500,message,null);
    }

}
