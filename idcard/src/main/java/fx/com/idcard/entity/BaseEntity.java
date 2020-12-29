package fx.com.idcard.entity;

import com.google.gson.Gson;

public class BaseEntity {

    /**
     * code : 300
     * message : 接口调用错误（必填参数为空）
     */

    private int code;
    private String message;

    public static BaseEntity objectFromData(String str) {

        return new Gson().fromJson(str, BaseEntity.class);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
