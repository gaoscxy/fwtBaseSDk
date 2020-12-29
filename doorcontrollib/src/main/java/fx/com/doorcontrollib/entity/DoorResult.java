package fx.com.doorcontrollib.entity;

public class DoorResult {

    private int code = -1;
    private String errDes;
    private boolean success;
    private String return_bytes = null;
    private String send_bytes = null;
    public DoorResult() {
    }

    public DoorResult(int code, String errDes, boolean success,String send_bytes,String return_bytes) {
        this.code = code;
        this.errDes = errDes;
        this.success = success;
        this.return_bytes = return_bytes;
        this.send_bytes = send_bytes;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrDes() {
        return errDes;
    }

    public void setErrDes(String errDes) {
        this.errDes = errDes;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReturn_bytes() {
        return return_bytes;
    }

    public void setReturn_bytes(String return_bytes) {
        this.return_bytes = return_bytes;
    }

    public String getSend_bytes() {
        return send_bytes;
    }

    public void setSend_bytes(String send_bytes) {
        this.send_bytes = send_bytes;
    }

    @Override
    public String toString() {
        return "DoorResult{" +
                "code=" + code +
                ", errDes='" + errDes + '\'' +
                ", success=" + success +
                ", return_bytes='" + return_bytes + '\'' +
                ", send_bytes='" + send_bytes + '\'' +
                '}';
    }
}
