package io.banditoz.dohmap.model;

public record BaseResponse<T>(T data, String error) {
    public static <T> BaseResponse<T> of(T data) {
        return new BaseResponse<>(data, null);
    }

    public static <T> BaseResponse<T> error(String error) {
        return new BaseResponse<>(null, error);
    }
}
