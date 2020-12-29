package fx.com.idcard.listener;

import fx.com.idcard.entity.IdCardEntity;

public interface ReadIDCardListener {
    void readIDCardResult(IdCardEntity idCardEntity);

    void readIDCardReeor(int errCode, String errMsg);
}
