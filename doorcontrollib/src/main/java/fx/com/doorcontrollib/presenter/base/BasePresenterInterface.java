package fx.com.doorcontrollib.presenter.base;

import java.util.Map;

import fx.com.doorcontrollib.enumIndex.ActivateStatus;
import fx.com.doorcontrollib.enumIndex.ResultStatus;
import io.reactivex.Observable;

public interface BasePresenterInterface {
    void createApiService();

    Map<String,Object> getParameterMap(ActivateStatus activateStatus, ResultStatus resultStatus);

    Map<String,Object> getParameterMap(ActivateStatus activateStatus, ResultStatus resultStatus, String msgErr, String remark);

    <T> Observable<T> subscriptionObservable(Observable<T> observable);
}
