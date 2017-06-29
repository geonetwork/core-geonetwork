package com.esri.sde.sdk.client;

public class SeColumnDefinition {
    public static final int TYPE_SMALLINT = 1;
    public static final int TYPE_INTEGER = 2;
    public static final int TYPE_FLOAT = 3;
    public static final int TYPE_DOUBLE = 4;
    public static final int TYPE_INT16 = 1;
    public static final int TYPE_INT32 = 2;
    public static final int TYPE_FLOAT32 = 3;
    public static final int TYPE_FLOAT64 = 4;
    public static final int TYPE_STRING = 5;
    public static final int TYPE_BLOB = 6;
    public static final int TYPE_DATE = 7;
    public static final int TYPE_SHAPE = 8;
    public static final int TYPE_RASTER = 9;
    public static final int TYPE_XML = 10;
    public static final int TYPE_INT64 = 11;
    public static final int TYPE_UUID = 12;
    public static final int TYPE_CLOB = 13;
    public static final int TYPE_NSTRING = 14;
    public static final int TYPE_NCLOB = 15;

    public SeColumnDefinition(String s, int i, int j, int k, boolean b) throws SeException {
    }

    public String getName() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getSize() {
        return 0;
    }

    public int getScale() {
        return 0;
    }

    public short getRowIdType() {
        return 0;
    }


}
