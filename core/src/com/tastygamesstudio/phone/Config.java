package com.tastygamesstudio.phone;

public class Config {
    public static final int X1 = 0;
    public static final int X2 = 1080;
    public static final int SIZE_X = X2 - X1;
    public static final int Y1 = 150;
    public static final int Y2 = 720;
    public static final int SIZE_Y = Y2 - Y1;
    public static final int imageSize = SIZE_X * SIZE_Y * 4;
    public static final int bytePackageCount = 128;
    public static final int bytePackegeSize = imageSize / bytePackageCount;

    public static final String CONNECTION_NAME_CODE = "CONNECTION/CODE/NAME:";
}
