package top.caodong0225.server.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class BaseResponseDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String message;

    public BaseResponseDTO() {
        this.code = 200;
        this.message = "ok";
    }

    public BaseResponseDTO(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static BaseResponseDTO makeResponse(int code, String message) {
        return new BaseResponseDTO(code, message);
    }

    public static BaseResponseDTO makeResponse(int status) {
        return new BaseResponseDTO(status, HttpStatus.valueOf(status).getReasonPhrase());
    }
}