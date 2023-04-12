package dev.riss.exception.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.riss.exception.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {

    private final ObjectMapper om;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {

            if (ex instanceof UserException) {
                log.info("UserException resolver to 400 [{}]", MDC.get("uuid"));
                String acceptHeader = request.getHeader("accept");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                if (MediaType.APPLICATION_JSON_VALUE.equals(acceptHeader)) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("ex", ex.getClass());
                    errorResult.put("message", ex.getMessage());

                    String result = om.writeValueAsString(errorResult);

                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(result);

                    return new ModelAndView();

                } else {
                    // TEXT/HTML
                    return new ModelAndView("error/500");   // 뷰 지정
                }
            }

        } catch (IOException e) {
            log.error("resolver ex", e);
        }

        return null;
    }
}
