package dev.riss.exception.api;

import dev.riss.exception.exception.UserException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@Slf4j
@RestController
public class ApiExceptionV3Controller {

    @GetMapping("/api3/members/{id}")
    public MemberDto getMember (@PathVariable("id") String id) {

        if (id.equals("ex")) throw new RuntimeException("잘못된 사용자");
        if (id.equals("bad")) throw new IllegalArgumentException("잘못된 입력 값");
        if (id.equals("no-such")) throw new NoSuchElementException("해당 요소가 없음");
        if (id.equals("null")) throw new NullPointerException("널 포인터 예외");
        if (id.equals("user-ex")) throw new UserException("사용자 오류");
        return new MemberDto(id, "hello " + id);
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String memberId;
        private String name;
    }

}
