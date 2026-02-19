package com.hf.newmidx.uhf.readerInterface;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

public interface GetCommendInterface {
    void setBaudrate();

    void getFirmware();

    void setWorkAntenna();

    void inventoryRealTime();

    void selectEPC(byte[] var1);

    void readFrom6C(int var1, int var2, int var3);

    void writeTo6C(byte[] var1, int var2, int var3, byte[] var4);
}
