package fx.com.doorcontrollib.entity;

import ch.wrzw.Result;

public class SensorStateEntity {

    private int code = -1;
    private boolean isUpsOk;
    private boolean isBoxSensorOk;
    private Result mResult;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isUpsOk() {
        return isUpsOk;
    }

    public void setUpsOk(boolean upsOk) {
        isUpsOk = upsOk;
    }

    public boolean isBoxSensorOk() {
        return isBoxSensorOk;
    }

    public void setBoxSensorOk(boolean boxSensorOk) {
        isBoxSensorOk = boxSensorOk;
    }

    public Result getmResult() {
        return mResult;
    }

    public void setmResult(Result mResult) {
        this.mResult = mResult;
    }

    @Override
    public String toString() {
        return "SensorStateEntity{" +
                "code=" + code +
                ", isUpsOk=" + isUpsOk +
                ", isBoxSensorOk=" + isBoxSensorOk +
                ", mResult=" + mResult +
                '}';
    }
}
