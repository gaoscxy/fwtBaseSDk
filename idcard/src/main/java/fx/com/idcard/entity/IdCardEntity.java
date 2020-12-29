package fx.com.idcard.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class IdCardEntity implements Parcelable {

    private String name = "";
    private String sex = "";
    private String nation = "";
    private String birthday = "";
    private String ID_Num = "";
    private String addr = "";
    private String sGov = "";
    private String startdate = "";
    private String enddate = "";
    private byte[] headFromCard;
    private Bitmap bm = null;
    private String fPInfo="";
    private String fpHexString;
    private byte[] fpInfo;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getID_Num() {
        return ID_Num;
    }

    public void setID_Num(String ID_Num) {
        this.ID_Num = ID_Num;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getsGov() {
        return sGov;
    }

    public void setsGov(String sGov) {
        this.sGov = sGov;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public void setHeadByte(byte[] headFromCard) {
        this.headFromCard = headFromCard;
    }

    public void setHeadBitmap(Bitmap bm) {
        this.bm = bm;
    }

    public byte[] getHeadFromCard() {
        return headFromCard;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setFPInfo(String fPInfo) {
        this.fPInfo = fPInfo;
    }

    public String getfPInfo() {
        return fPInfo;
    }

    public void setFpHexString(String fpHexString) {
        this.fpHexString = fpHexString;
    }

    public String getFpHexString() {
        return fpHexString;
    }

    public IdCardEntity() {
    }

    @Override
    public String toString() {
        return "IdCardEntity{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birthday='" + birthday + '\'' +
                ", ID_Num='" + ID_Num + '\'' +
                ", addr='" + addr + '\'' +
                ", sGov='" + sGov + '\'' +
                ", startdate='" + startdate + '\'' +
                ", enddate='" + enddate + '\'' +
                ", headFromCard=" + Arrays.toString(headFromCard) +
                ", bm=" + bm +
                ", fPInfo='" + fPInfo + '\'' +
                ", fpHexString='" + fpHexString + '\'' +
                '}';
    }

    public void setFpinfp(byte[] fpInfo) {
        this.fpInfo = fpInfo;
    }

    public byte[] getFpInfo() {
        return fpInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.sex);
        dest.writeString(this.nation);
        dest.writeString(this.birthday);
        dest.writeString(this.ID_Num);
        dest.writeString(this.addr);
        dest.writeString(this.sGov);
        dest.writeString(this.startdate);
        dest.writeString(this.enddate);
        dest.writeByteArray(this.headFromCard);
        dest.writeParcelable(this.bm, flags);
        dest.writeString(this.fPInfo);
        dest.writeString(this.fpHexString);
        dest.writeByteArray(this.fpInfo);
    }

    protected IdCardEntity(Parcel in) {
        this.name = in.readString();
        this.sex = in.readString();
        this.nation = in.readString();
        this.birthday = in.readString();
        this.ID_Num = in.readString();
        this.addr = in.readString();
        this.sGov = in.readString();
        this.startdate = in.readString();
        this.enddate = in.readString();
        this.headFromCard = in.createByteArray();
        this.bm = in.readParcelable(Bitmap.class.getClassLoader());
        this.fPInfo = in.readString();
        this.fpHexString = in.readString();
        this.fpInfo = in.createByteArray();
    }

    public static final Creator<IdCardEntity> CREATOR = new Creator<IdCardEntity>() {
        @Override
        public IdCardEntity createFromParcel(Parcel source) {
            return new IdCardEntity(source);
        }

        @Override
        public IdCardEntity[] newArray(int size) {
            return new IdCardEntity[size];
        }
    };
}
