package top.caodong0225.server.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import top.caodong0225.server.dto.BaseResponseDTO;

import java.io.IOException;
import java.nio.file.AccessDeniedException;


/**
 * @author jyzxc
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("参数错误: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getDefaultMessage()).append(", ");
        }
        errorMessage.delete(errorMessage.length() - 2, errorMessage.length());

        return new ResponseEntity<>(
                new BaseResponseDTO(400, errorMessage.toString()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<?> handleIllegalArgumentException(Exception ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(400, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(413, "File size too large"),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<?> handleNotFoundException(NoResourceFoundException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(404, "Not Found"),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<?> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(415, "Unsupported Media Type"),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleWriteException(IOException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(400, ex.getMessage()),
                HttpStatus.LOCKED
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(Exception ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(400, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(400, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(405, "Method Not Allowed"),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(
                new BaseResponseDTO(400, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(
                new BaseResponseDTO(500, "Internal Server Error"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
