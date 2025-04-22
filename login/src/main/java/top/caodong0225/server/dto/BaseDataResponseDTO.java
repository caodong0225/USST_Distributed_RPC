package top.caodong0225.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseDataResponseDTO extends BaseResponseDTO {
    private Object data;

    public BaseDataResponseDTO() {
        super();
    }

    public BaseDataResponseDTO(Object data) {
        super();
        this.data = data;
    }

    public BaseDataResponseDTO(Integer code, String message) {
        super(code, message);
    }

    public BaseDataResponseDTO(Integer code, String message, Object data) {
        super(code, message);
        this.data = data;
    }
}
