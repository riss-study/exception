package dev.riss.exception.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    public static final String LOG_ID="logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String uuid= UUID.randomUUID().toString();
        MDC.put("uuid", uuid);
        request.setAttribute(LOG_ID, uuid);

        log.info("REQUEST(PRE0-HANDLE) [{}][{}][{}]", uuid, request.getDispatcherType(), requestURI);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("REQUEST(POST-HANDLE) [{}][{}][{}][{}]", MDC.get("uuid"), request.getDispatcherType(), request.getRequestURI(), modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("RESPONSE(AFTER-COMPLETION) [{}][{}][{}]", MDC.get("uuid"), request.getDispatcherType(), requestURI);
        if (null != ex) {
            log.error("AFTER-COMPLETION ERROR!! [{}][{}][{}]", MDC.get("uuid"), requestURI, ex.getMessage());
        }
        MDC.remove("uuid");
    }
}
