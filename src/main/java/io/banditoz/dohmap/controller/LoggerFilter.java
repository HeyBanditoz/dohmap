package io.banditoz.dohmap.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggerFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(LoggerFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        long before = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            long after = System.currentTimeMillis() - before;
            String path = req.getRequestURI().substring(req.getContextPath().length());

            if (shouldLog(path)) {
                if (after < 1000) {
                    log.info("Request finished: {}", getLogLine(req, resp, path, after));
                } else {
                    log.warn("Slow request finished: {}", getLogLine(req, resp, path, after));
                }
            }
        }
    }

    private String getLogLine(HttpServletRequest req, HttpServletResponse resp, String path, long durationMs) {
        return "method=\"%s\" path=\"%s\" duration=\"%dms\" status=\"%s\" userAgent=\"%s\" %s"
                .formatted(req.getMethod(), path, durationMs, resp.getStatus(), req.getHeader("User-Agent"), determineHost(req));
    }

    private boolean shouldLog(String path) {
        return !(path.startsWith("/static/") || path.startsWith("/favicon") || path.startsWith("/android-chrome-") || path.startsWith("/apple-"));
    }

    private String determineHost(HttpServletRequest req) {
        String xff = req.getHeader("x-forwarded-for");
        if (xff == null) {
            return "ip=\"" + req.getRemoteHost() + "\"";
        } else if (xff.equals(req.getRemoteHost())) {
            return "ip=\"" + xff + "\"";
        } else {
            return "ip=\"%s\" proxy_ip=\"%s\"".formatted(xff, req.getRemoteHost());
        }
    }
}
