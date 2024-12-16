package io.banditoz.dohmap.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Override
    @SuppressWarnings("unchecked") // what we're doing is sorta cursed... but trying to form around BaseResponse, there must be a better way to do it
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        Object newMap = new HashMap<>();
        ((Map<String, Map<String, Object>>) newMap).put("data", null);
        ((Map<String, Map<String, Object>>) newMap).put("error", errorAttributes);
        return (Map<String, Object>) newMap;
    }
}