package com.tastygamesstudio.phone;

public class Config {
    public static final int X1 = 20;
    public static final int X2 = 1100;
    public static final int SIZE_X = X2 - X1;
    public static final int Y1 = 30;
    public static final int Y2 = 600;
    public static final int SIZE_Y = Y2 - Y1;
    public static final int imageSize = SIZE_X * SIZE_Y * 4;
    public static final int bytePackageCount = 128;
    public static final int bytePackegeSize = imageSize / bytePackageCount;

    public static final String CONNECTION_NAME_CODE = "CONNECTION/CODE/NAME:";
}
