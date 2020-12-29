package fx.com.doorcontrollib.entity;

public class TempSensorEntity {
    int code = -1;
    int temp;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    @Override
    public String toString() {
        return "TempSensorEntity{" +
                "code=" + code +
                ", temp=" + temp +
                '}';
    }
}
