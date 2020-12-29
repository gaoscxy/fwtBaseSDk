package fx.com.doorcontrollib.entity;

public class DoorState {

    int code = -1;//是1的话代表成功 2是返回错误
    int doorState = -1;//0:结果错误1：门已开,2：开门中,3：门关


    protected DoorState() {
    }

    public DoorState(int code, int isDoorOpen) {
        this.code = code;
        this.doorState = isDoorOpen;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getDoorState() {
        return doorState;
    }

    public void setDoorState(int doorState) {
        this.doorState = doorState;
    }

    @Override
    public String toString() {
        return "DoorState{" +
                "code=" + code +
                ", doorState=" + doorState +
                '}';
    }
}
