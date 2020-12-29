package fx.com.idcard.presenter.base;

import io.reactivex.Observable;

public interface BasePresenterInterface {
    void createApiService();

    <T> Observable<T> subscriptionObservable(Observable<T> observable);
}
