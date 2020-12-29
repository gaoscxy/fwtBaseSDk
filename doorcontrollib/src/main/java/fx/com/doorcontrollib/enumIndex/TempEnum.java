package fx.com.doorcontrollib.enumIndex;

public enum TempEnum {
    //1表示ups温度，2表示箱体温度
    UPS("表示ups温度",1),BOX("表示箱体温度",2);

    private String name;
    private int index;
    // 构造方法
    TempEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
