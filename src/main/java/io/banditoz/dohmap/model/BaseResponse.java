package io.banditoz.dohmap.model;

import java.util.Map;

public record BaseResponse<T>(T data, Map<String, String> error) {
    public static <T> BaseResponse<T> of(T data) {
        return new BaseResponse<>(data, null);
    }

    public static <T> BaseResponse<T> error(String error) {
        return new BaseResponse<>(null, Map.of("error", error));
    }

    public static <T> BaseResponse<T> error(Map<String, String> error) {
        return new BaseResponse<>(null, error);
    }
}
