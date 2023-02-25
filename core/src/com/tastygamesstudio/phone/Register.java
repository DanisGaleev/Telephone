package com.tastygamesstudio.phone;

import com.esotericsoftware.kryo.Kryo;

public class Register {
    public static final int BUFFER_SIZE = Config.bytePackegeSize + 400000;//184400;
    public static final float TIME_DELTA = 0.01f;
    public static final int TIMEOUT = 5000;
    public static final int TCP_PORT = 5555, UDP_PORT = 6666;

    public static void register(Kryo kryo) {
        kryo.register(String.class);
        kryo.register(byte[].class);
    }
}
