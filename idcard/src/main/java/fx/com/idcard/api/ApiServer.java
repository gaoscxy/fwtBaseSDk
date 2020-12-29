package fx.com.idcard.api;

import java.util.Map;

import fx.com.idcard.entity.BaseEntity;
import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiServer {

    @POST(ApiUrl.DEVICE_SERVICE)
    Observable<BaseEntity> deviceService(@QueryMap Map<String, Object> map);
}
