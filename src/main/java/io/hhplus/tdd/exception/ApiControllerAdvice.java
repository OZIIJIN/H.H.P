package io.hhplus.tdd.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "예상치 못한 에러가 발생했습니다."));
    }

    @ExceptionHandler(value = InvalidPointAmountException.class)
    public ResponseEntity<ErrorResponse> handleException(InvalidPointAmountException e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", e.getMessage()));
    }
}
