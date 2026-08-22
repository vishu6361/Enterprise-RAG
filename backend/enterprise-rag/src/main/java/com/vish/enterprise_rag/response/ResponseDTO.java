package com.vish.enterprise_rag.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private boolean flag;
    private String message;
    private Object data;

    public static ResponseDTO success(String message) {
        return new ResponseDTO(true, message, null);
    }

    public static ResponseDTO success(String message, Object data) {
        return new ResponseDTO(true, message, data);
    }

    public static ResponseDTO error(String message) {
        return new ResponseDTO(false, message, null);
    }
}
