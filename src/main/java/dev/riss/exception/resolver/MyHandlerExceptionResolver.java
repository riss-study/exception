package dev.riss.exception.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        log.info("call resolver", ex);

        try {
            if (ex instanceof IllegalArgumentException) {
                log.info("IllegalArgumentException resolver to 400 [{}]", MDC.get("uuid"));
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                return new ModelAndView();  // 이렇게 새로운(비어있는) 모델앤뷰를 리턴하면 뷰를 렌더링하지 않고 정상흐름으로
                // WAS 까지 정상적으로 return->return-> ... -> return 된다. (그리고 WAS 에서 sendError 를 받았으므로 다시 오류페이지를 찾을 것임)
                // 만약 비어있지 않은 특정 ModelAndView 를 반환하면 해당 정보로 뷰를 렌더링함
            }

            if (ex instanceof NoSuchElementException) {
                log.info("NoSuchElementException resolver [{}]", MDC.get("uuid"));
                response.setHeader("Content-Type", "application/json; charset=UTF-8");
                PrintWriter w = response.getWriter();
                responseWriter(ex, w);
                return new ModelAndView();
            }

            if (ex instanceof NullPointerException) {
                response.setHeader("Content-Type", "application/json; charset=UTF-8");
                log.info("NullPointerException resolver [{}]", MDC.get("uuid"));
                ObjectMapper om=new ObjectMapper();
                String resJson = om.writeValueAsString(new ExceptionDto(MDC.get("uuid").toString(), 555, ex.getMessage()));
                response.getWriter().println(resJson);

                return new ModelAndView();
            }

        } catch (IOException e) {
            log.error("resolver ex", e);
            //e.printStackTrace();
        }

        return null; // null 로 리턴하면 그냥 예외가 터져서 계속 날라간다.
        // null 리턴 시 순서 -> 1. 다음 ExceptionResolver 를 찾아서 실행
        // 2. 만약 처리할 수 있는 ExceptionResolver 가 없으면 예외처리가 되지 않고, 기존에 발생한 예외를 서블릿 밖으로 던진다.
        // (이 메서드로 던져진 ex 가 WAS 까지 다시 날라감)
    }

    // ExceptionResolver 활용
    // 1. 예외 상태 코드 반환: 예외를 sendError 호출로 변경해서 서블릿에서 상태 코드에 따른 오류를 처리하도록 위임 -> 이후 WAS 가 서블릿 오류 페이지를 찾아서 내부 호출 (ex. /error 템플릿 호출)
    // 2. 뷰 템플릿 처리: ModelAndView 에 값을 채워서 예외에 따른 새로운 오류 화면 뷰를 렌더링해서 고객에게 제공
    // 3. API 응답 처리: response.getWriter().println("~~"); 처럼 HTTP 응답 바디에 직접 데이터에 넣어주는 것도 가능. 이 부분에서 JSON 으로 응답하면 API 응답 처리가 가능

    private void responseWriter(Exception ex, PrintWriter w) {
        w.println("{");
        w.println("\t\"uuid\": \"" + MDC.get("uuid") + "\",");
        w.println("\t\"errorCode\": \"" + HttpServletResponse.SC_BAD_REQUEST + "\",");
        w.println("\t\"errorMessage\": \"" + ex.getMessage() + "\"");
        w.println("}");
    }

    @AllArgsConstructor
    @Data
    static class ExceptionDto {
        String uuid;
        int errCode;
        String errMsg;
    }
}
