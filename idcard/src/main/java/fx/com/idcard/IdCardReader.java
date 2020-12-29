package fx.com.idcard;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.sdses.bean.ID2Data;
import com.sdses.bean.ID2FP;
import com.sdt.Sdtapi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import fx.com.idcard.entity.IdCardEntity;
import fx.com.idcard.excep.CollectExcepUtil;
import fx.com.idcard.excep.InitException;
import fx.com.idcard.excep.InitExceptionListener;
import fx.com.idcard.listener.ReadIDCardListener;
import fx.com.idcard.listener.SAMIDResultListener;

public class IdCardReader {

    private static final String initErr = "init()未初始化";
    private static volatile IdCardReader singleton;
    private Sdtapi stda;
    private boolean m_bSAMUSBState;
    private Application context;
    int iContinueReadMdeo = 0;

    private IdCardReader() {

    }

    public static IdCardReader getInstance() {
        if (singleton == null) {
            synchronized (IdCardReader.class) {
                if (singleton == null) {
                    singleton = new IdCardReader();
                }
            }
        }
        return singleton;
    }

    public void init(Activity activity) {
        try {
            context = activity.getApplication();
            stda = new Sdtapi(activity);
            m_bSAMUSBState = true;
        } catch (Exception e) {
            e.printStackTrace();
            CollectExcepUtil.getInstance(context).init(e);
            Toast.makeText(activity, "USB设备异常或者没有连接", Toast.LENGTH_SHORT).show();
        }
    }

    public void init(Activity activity, InitExceptionListener listener) {
        int numberInitialization = 0;
        try {
            context = activity.getApplication();
            stda = new Sdtapi(activity);
            if (stda != null) {
                m_bSAMUSBState = true;
                if (listener != null) {
                    listener.success();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            numberInitialization++;
            if (listener != null) {
                if (numberInitialization == 1) {
                    init(activity, listener);
                } else if (numberInitialization == 2) {
                    listener.erroe("身份证设备打开失败");
                }
            }
            CollectExcepUtil.getInstance(context).init(e);
        }
    }

    //读取SMID
    public void readSAMID(SAMIDResultListener listener) throws InitException {
        if (stda != null) {
            String s_SamID = "";
            s_SamID = getSAMID();
            if (listener != null) {
                listener.readSAMIDResult(s_SamID);
            }
        } else {
            throw new InitException(initErr);
        }
    }

    private String getSAMID() {
        if (stda == null) {
            return initErr;
        }
        if (!m_bSAMUSBState) {
            return "";
        }
        char[] puSAMID = new char[36];
        String strSAMID = "";
        try {
            int ret = stda.SDT_GetSAMIDToStr(puSAMID);
            if (ret == 0x90) {
                strSAMID = new String(puSAMID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CollectExcepUtil.getInstance(context).sendSAMIDException(e);
        }
        return strSAMID;
    }

    /*读取身份证*/
    public void readIDCard(ReadIDCardListener listener) throws InitException {
        chenckInit();
        CollectExcepUtil.getInstance(context).readIDCardStart("readIDCard");
        int ret = 0;
        int i = 0, count = 0;
        String s_sexcode = "";
        int sexcode = 0;
        String s_nationcode = "";
        int nationcode = 1;
        char[] cbuf = new char[1024];
        byte[] data = new byte[1280];
        Bitmap bm;
        ID2Data _id2data = new ID2Data();

        String errMsg;
        int errCode;
        IdCardEntity idCardEntity = new IdCardEntity();

        ret = readCard(data);
        if (ret == 0) {
            InputStream inputStream = new ByteArrayInputStream(data, 0, 256);
            InputStreamReader inputStreamReader = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_16LE);
            }
            try {
                inputStreamReader.read(cbuf);
            } catch (IOException e) {

            }
            for (i = count; i < count + 15; i++) {
                if (cbuf[i] == ' ') {
                    count = i;
                    break;
                }
            }
            idCardEntity.setName(new String(cbuf, 0, i));
            for (i = count; i < 15; i++) {
                if (cbuf[i] != ' ') {
                    break;
                }
            }
            count = i;
            s_sexcode = new String(cbuf, count, 1);
            count += 1;
            sexcode = Integer.parseInt(s_sexcode);

            String sex = "";
            switch (sexcode) {
                case 0:
                    sex = "未知";
                    break;
                case 1:
                    sex = "男";
                    break;
                case 2:
                    sex = "女";
                    break;
                case 9:
                    sex = "未说明";
                    break;
            }
            idCardEntity.setSex(sex);
            s_nationcode = new String(cbuf, count, 2);

            count += 2;
            nationcode = Integer.parseInt(s_nationcode);
            idCardEntity.setNation(getnation(nationcode));//国家
            String birthday = new String(cbuf, count, 8);
            idCardEntity.setBirthday(birthday);
            count += 8;

            for (i = count; i < count + 35; i++) {
                if (cbuf[i] == ' ') {
                    break;
                }
            }

            String addr = new String(cbuf, count, i - count);
            idCardEntity.setAddr(addr);
            count = i;
            for (i = count; i < count + 35 - addr.length(); i++) {
                if (cbuf[i] != ' ') {
                    break;
                }
            }
            count = i;
            String ID_Num = new String(cbuf, count, 18);
            idCardEntity.setID_Num(ID_Num);
            count += 18;

            for (i = count; i < count + 15; i++) {
                if (cbuf[i] == ' ') {
                    break;
                }
            }
            String sGov = new String(cbuf, count, i - count);
            idCardEntity.setsGov(sGov);
            count = i;
            i = sGov.length();
            for (i = count; i < count + 15 - sGov.length(); i++) {
                if (cbuf[i] != ' ') {
                    break;
                }
            }
            count = i;

            String startdate = new String(cbuf, count, 8);
            idCardEntity.setStartdate(startdate);
            count += 8;
            String enddate = "";
            for (i = count; i < count + 8; i++) {
                if (cbuf[i] == ' ') {
                    enddate = new String(cbuf, count, i - count);
                    break;
                }
            }
            enddate = new String(cbuf, count, 8);
            idCardEntity.setEnddate(enddate);

            _id2data.clearID2DataRAW();
            _id2data.decode_debug(data);
            _id2data.rePackage();

            bm = BitmapFactory.decodeByteArray(_id2data.getmID2Pic().getHeadFromCard(), 0, 38862);
            idCardEntity.setHeadByte(_id2data.getmID2Pic().getHeadFromCard());
            idCardEntity.setHeadBitmap(bm);
            CollectExcepUtil.getInstance(context).readIDCardStop(idCardEntity);
            listener.readIDCardResult(idCardEntity);
            return;
        } else if (ret == -2001) {
            errMsg = "未放置身份证";
            errCode = -2001;
        } else if (ret == -2002) {
            errMsg = "身份证选卡失败";
            errCode = -2002;
        } else if (ret == -3001) {
            errMsg = "身份证读卡失败";
            errCode = -3001;
        } else if (ret == -2) {
            errMsg = "参数错误";
            errCode = -2;
        } else {
            errMsg = "加载失败";
            errCode = -1;
        }

        CollectExcepUtil.getInstance(context).readIDCardExcep(errCode, errMsg);
        listener.readIDCardReeor(errCode, errMsg);
    }

    private void chenckInit() throws InitException {
        if (stda == null) {
            throw new InitException(initErr);
        }
    }

    /*读取身份证含指纹*/
    public void readIDCard_Fp(ReadIDCardListener listener) throws InitException {
        chenckInit();
//        CollectExcepUtil.getInstance(context).readIDCard_FpStart("读取身份证含指纹readIDCard_Fp()");
        IdCardEntity entity = new IdCardEntity();
        String errMsg;
        int errCode;
        int ret = 0;
        int i = 0, count = 0;
        String s_sexcode = "";
        int sexcode = 0;
        String s_nationcode = "";
        int nationcode = 1;
        char[] cbuf = new char[1024];
        byte[] baseInfo = new byte[1280]; //文字信息+照片
        byte[] fpInfo = new byte[1024]; //指纹数据
        Bitmap bm;
        ID2Data _id2data = new ID2Data();

        String name = "";
        String sex = "";
        String nation = "";
        String birthday = "";
        String ID_Num = "";
        String addr = "";
        String sGov = "";
        String startdate = "";
        String enddate = "";

        ret = readCardAllInfo(baseInfo, fpInfo);
        if (ret == 0) {
            InputStream inputStream = new ByteArrayInputStream(baseInfo, 0, 256);
            InputStreamReader inputStreamReader = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_16LE);
            }
            try {
                inputStreamReader.read(cbuf);
            } catch (IOException e) {

            }
            for (i = count; i < count + 15; i++) {
                if (cbuf[i] == ' ') {
                    count = i;
                    break;
                }
            }
            name = new String(cbuf, 0, i);
            for (i = count; i < 15; i++) {
                if (cbuf[i] != ' ') {
                    break;
                }
            }
            count = i;
            s_sexcode = new String(cbuf, count, 1);
            count += 1;
            sexcode = Integer.parseInt(s_sexcode);
            switch (sexcode) {
                case 0:
                    sex = "未知";
                    break;
                case 1:
                    sex = "男";
                    break;
                case 2:
                    sex = "女";
                    break;
                case 9:
                    sex = "未说明";
                    break;
            }

            s_nationcode = new String(cbuf, count, 2);
            count += 2;
            nationcode = Integer.parseInt(s_nationcode);
            nation = getnation(nationcode);

            birthday = new String(cbuf, count, 8);
            count += 8;

            for (i = count; i < count + 35; i++) {
                if (cbuf[i] == ' ') {
                    break;
                }
            }

            addr = new String(cbuf, count, i - count);
            count = i;
            for (i = count; i < count + 35 - addr.length(); i++) {
                if (cbuf[i] != ' ') {
                    break;
                }
            }
            count = i;
            ID_Num = new String(cbuf, count, 18);
            count += 18;

            for (i = count; i < count + 15; i++) {
                if (cbuf[i] == ' ') {
                    break;
                }
            }
            sGov = new String(cbuf, count, i - count);
            count = i;
            i = sGov.length();
            for (i = count; i < count + 15 - sGov.length(); i++) {
                if (cbuf[i] != ' ') {
                    break;
                }
            }
            count = i;

            startdate = new String(cbuf, count, 8);
            count += 8;

            for (i = count; i < count + 8; i++) {
                if (cbuf[i] == ' ') {
                    enddate = new String(cbuf, count, i - count);
                    break;
                }
            }
            enddate = new String(cbuf, count, 8);

            entity.setName(name);
            entity.setSex(sex);
            entity.setNation(nation);
            entity.setBirthday(birthday);
            entity.setID_Num(ID_Num);
            entity.setAddr(addr);
            entity.setsGov(sGov);
            entity.setEnddate(enddate);
            entity.setStartdate(startdate);
            entity.setFpinfp(fpInfo);
            try {
                String fPInfo = showFPInfo(fpInfo);
                entity.setFPInfo(fPInfo);
            } catch (Exception e) {
                CollectExcepUtil.getInstance(context).fpInfoError(entity, e.getMessage());
            }
            if (fpInfo[0] != 0) {
                String fpHexString = bytesToHexString(fpInfo, 1024);
                entity.setFpHexString(fpHexString);
            }

            _id2data.clearID2DataRAW();
            _id2data.decode_debug(baseInfo);
            _id2data.rePackage();

            bm = BitmapFactory.decodeByteArray(_id2data.getmID2Pic().getHeadFromCard(), 0, 38862);
            entity.setHeadBitmap(bm);
            entity.setHeadByte(_id2data.getmID2Pic().getHeadFromCard());
            CollectExcepUtil.getInstance(context).readIDCard_FpStop(entity);
            listener.readIDCardResult(entity);
            return;
        } else if (ret == -2001) {
            errCode = -2001;
            errMsg = "未放置身份证/请重新放置";
        } else if (ret == -2002) {
            errCode = -2002;
            errMsg = "身份证选卡失败";
        } else if (ret == -3001) {
            errCode = -3001;
            errMsg = "身份证读卡失败";
        } else if (ret == -2) {
            errCode = -2;
            errMsg = "参数错误";
        } else {
            errCode = -1;
            errMsg = "加载失败";
        }
        CollectExcepUtil.getInstance(context).readIDCard_FpError(errCode, errMsg);
        listener.readIDCardReeor(errCode, errMsg);
    }

    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray
     * @return
     */
    private static String bytesToHexString(byte[] bArray, int Len) {
        StringBuffer sb = new StringBuffer(Len);
        String sTemp;
        for (int i = 0; i < Len; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * 显示二代证中指纹信息的指位情况
     *
     * @param fpData
     * @return
     * @throws Exception
     */
    private String showFPInfo(byte[] fpData) throws Exception {
        if (fpData == null || fpData.length < 1024) {
            return "";
        }
        ID2FP id2FP = new ID2FP();
        if (id2FP.initFP(fpData)) {
            return id2FP.getFingerPosition(1) + " " + id2FP.getFingerPosition(2);
        } else {
            return "";
        }

    }

    /**
     * 读取指纹
     *
     * @param baseData 文字信息 + 照片信息
     * @param fpData   指纹信息
     * @return 0:成功， 其他：失败
     */
    private int readCardAllInfo(byte[] baseData, byte[] fpData) {
        if (baseData == null || baseData.length < 1280 || fpData == null || fpData.length < 1024) {
            return -2;
        }
        if (!m_bSAMUSBState) {
            return -1;
        }
        int nRet = 0;
        int[] txtDataLen = new int[1];
        int[] photoDataLen = new int[1];
        int[] fpInfoLen = new int[1];
        byte[] txtData = new byte[256];
        byte[] photoData = new byte[1024];
        byte[] fpInfo = new byte[1024];
//		byte[] rawData = new byte[1280];

        try {

            if (0 == iContinueReadMdeo) {
                nRet = stda.SDT_ReadBaseFPMsg(txtData, txtDataLen, photoData, photoDataLen, fpInfo, fpInfoLen);
                if (nRet != 0x90) {
                    //return -3001;
                } else {
                    System.arraycopy(txtData, 0, baseData, 0, 256);
                    System.arraycopy(photoData, 0, baseData, 256, 1024);
                    System.arraycopy(fpInfo, 0, fpData, 0, 1024);
                    return 0;
                }
            }

            nRet = stda.SDT_StartFindIDCard();//寻卡
            if (nRet != 0x9f) {
                return -2001;
            }
            nRet = stda.SDT_SelectIDCard();//选卡
            if (nRet != 0x90) {
                return -2002;
            }
            nRet = stda.SDT_ReadBaseFPMsg(txtData, txtDataLen, photoData, photoDataLen, fpInfo, fpInfoLen);
            if (nRet != 0x90) {
                return -3001;
            } else {
                System.arraycopy(txtData, 0, baseData, 0, 256);
                System.arraycopy(photoData, 0, baseData, 256, 1024);
                System.arraycopy(fpInfo, 0, fpData, 0, 1024);

                if (1 == iContinueReadMdeo) {
                    stda.SDT_ResetSAM();
                }

                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -3;
        }
    }

    private String getnation(int nationcode) {
        String[] nation = {"汉",
                "蒙古",
                "回",
                "藏",
                "维吾尔",
                "苗",
                "彝",
                "壮",
                "布依",
                "朝鲜",
                "满",
                "侗",
                "瑶",
                "白",
                "土家",
                "哈尼",
                "哈萨克",
                "傣",
                "黎",
                "僳僳",
                "佤",
                "畲",
                "高山",
                "拉祜",
                "水",
                "东乡",
                "纳西",
                "景颇",
                "柯尔克",
                "土",
                "达斡尔",
                "仫佬",
                "羌",
                "布朗",
                "撒拉",
                "毛南",
                "仡佬",
                "锡伯",
                "阿昌",
                "普米",
                "塔吉克",
                "怒",
                "乌孜别克",
                "俄罗斯",
                "鄂温克",
                "德昂",
                "保安",
                "裕固",
                "京",
                "塔塔尔",
                "独龙",
                "鄂伦春",
                "赫哲",
                "门巴",
                "珞巴 ",
                "基诺"};
        return nation[nationcode - 1];
    }

    /**
     * 读卡
     *
     * @param data 1280字节 byte 数组
     * @return 0:成功， 其他：失败
     */
    private int readCard(byte[] data) {
        if (data == null || data.length < 1280) {
            return -2;
        }

        if (!m_bSAMUSBState) {
            return -1;
        }

        int nRet = 0;
        int[] txtDataLen = new int[1];
        int[] photoDataLen = new int[1];
        byte[] txtData = new byte[256];
        byte[] photoData = new byte[1024];

        try {
            nRet = stda.SDT_StartFindIDCard();//寻卡
            if (nRet != 0x9f) {
                return -2001;
            }
            nRet = stda.SDT_SelectIDCard();//选卡
            if (nRet != 0x90) {
                return -2002;
            }
            nRet = stda.SDT_ReadBaseMsg(txtData, txtDataLen, photoData, photoDataLen);
            if (nRet != 0x90) {
                return -3001;
            } else {
                System.arraycopy(txtData, 0, data, 0, 256);
                System.arraycopy(photoData, 0, data, 256, 1024);
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -3;
        }
    }


}