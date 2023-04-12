package dev.riss.exception.exhandler.advice;

import dev.riss.exception.api.ApiExceptionV2Controller;
import dev.riss.exception.exception.UserException;
import dev.riss.exception.exhandler.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice   // 대상을 지정하지 않으면, 모든 Controller 에서 다 아래의 ExceptionHandler 가 적용
// 대상 지정 1. annotations=RestController.class => 모든 RestController 클래스에만 적용 (일반 Controller 는 예외)
// 2. "org.example.controllers" => 특정 패키지 안에 들어있는 모든 컨트롤러들이 대상 (basePackages = "~~~")
// 3. assignableTypes={ControllerInterface.Class, AbstractController.class} => 해당 클래스 컨트롤러들이 대상 (자식 컨트롤러도 포함)
public class ExControllerAdvice {

    // 스프링은 항상 디테일한 것이 우선권을 가지므로 @ExceptionHandler 도 자식예외처리 메서드가 우선권을 갖고 호출됨
    // @Controller 를 class level 에서 지정하고 (@RestController 면 안됨) String 을 반환하면,
    // 일반적인 Controller 처럼 뷰리졸버를 통해 뷰를 반환함 -> 사실 @ExceptionHandler 는 대부분 api 예외에서 호출하도록 구현함
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)   //IllegalArgumentException 의 자식 들도 포함해서 여기로 들어옴
    public ErrorResult illegalExHandler (IllegalArgumentException e) {
        log.error("[exceptionHandler][{}] ex", MDC.get("uuid"), e);
        return new ErrorResult("BAD", e.getMessage());  // @RestController 가 Class Level 에 있으므로 여기에서도 @ResponseBody 적용
    }
    // 1. 컨트롤러에서 IllegalArgumentException 예외 발생(컨트롤러가 밖으로 해당 예외 던짐)
    // 2. 핸들러어댑터->Dispatcher Servlet -> ExceptionResolver(예외터지면 예외해결시도를 위해) 까지 이동
    // 3. 우선 순위에 의해 ExceptionHandlerExceptionResolver 가 실행
    // 4. 컨트롤러에 @ExceptionHandler 어노테이션이 있으면, 해당하는 에러를 처리하는 @ExceptionHandler 가 붙은 메서드를 실행
    // (ExceptionHandlerExceptionResolver 가 해당 컨트롤러에 IllegalArgumentException 을 처리할 수 있는 @ExceptionHandler 가 있는지 확인)
    // (여기서는 illegalExHandler() 가 실행) => 정상적인 처리를 하므로 http status code 가 200 으로 나감
    // 예외 상태 코드도 바꾸고 싶으면, @ResponseStatus 에노테이션 추가하면 됨
    // (서블릿 컨테이너로 올라가서 다시 에러 받아서 오류 경로(/error/~)로 내려오고 등등의 처리 안하고 여기서 그냥 끝냄, WAS 에 정상응답으로 나감)

    @ExceptionHandler   // 여기와 메서드 파라미터랑 exception class 가 같은 경우 생략 가능 (여기선 UserException 으로 같아서 생략)
    public ResponseEntity<ErrorResult> userExHandler (UserException e) {    // UserException 의 자식들도 포함해서 여기로 들어옴
        log.error("[exceptionHandler][{}]", MDC.get("uuid"), e.getMessage());
        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
//        return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResult exHandler (Exception e) {    // Exception => 해당 컨트롤러에서 처리하지 못한 예외는 여기로 다 넘어옴
        // (Exception 이 최상위이므로 모든 exception 이 자식들이기 때문)
        log.error("[exceptionHandler][{}]", MDC.get("uuid"), e.getMessage());
        return new ErrorResult("EX", "내부 오류");
    }

    @ExceptionHandler({NoSuchElementException.class, NullPointerException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResult exHandlerMulti (Exception e) {
        log.error("[exceptionHandler][{}]", MDC.get("uuid"), e.getMessage());
        return new ErrorResult("NO-SUCH-OR-NULL", "해당 요소가 없거나 널포인터 예외");
    }

}
