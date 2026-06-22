package com.cruvs.backend.util;
import com.cruvs.backend.response.ApiResponse;

public class ApiResponseUtil {
    private ApiResponseUtil(){}

    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(200,true,message,data);
    }

    public static <T> ApiResponse<T> created(String message, T data){
        return new ApiResponse<>(201,true,message,data);
    }

    public static <T> ApiResponse<T> badRequest(String message, T data){
        return new ApiResponse<>(400, false, message,null);
    }

    public static <T> ApiResponse<T> unauthorized(String message){
        return new ApiResponse<>(401,false,message,null);
    }

    public static <T> ApiResponse<T> forbidden(String message){
        return new ApiResponse<>(403,false, message,null);
    }

    public static <T> ApiResponse<T> notFound(String message){
        return new ApiResponse<>(404,false, message,null);
    }

    public static <T> ApiResponse<T> serverError(String message){
        return new ApiResponse<>(500,false, message,null);
    }

}
