package com.esri.sde.sdk.client;


public class SeRasterAttr {

    public SeRasterAttr(boolean b) {
    }

    public int getPixelType() {
        return -1;
    }

    public void setPixelType(int p) {
    }

    public int getTileHeight() {
        return -1;
    }

    public int getTileWidth() {
        return -1;
    }

    public SeRasterBand[] getBands() throws SeException {
        return null;
    }

    public int getMaxLevel() {
        return -1;
    }

    public boolean skipLevelOne() {
        return false;
    }

    public SeExtent getExtentByLevel(int i) throws SeException {
        return null;
    }

    public int getImageWidthByLevel(int i) {
        return -1;
    }

    public int getImageHeightByLevel(int i) {
        return -1;
    }

    public SDEPoint getImageOffsetByLevel(int i) {
        return null;
    }

    public int getTilesPerRowByLevel(int i) {
        return -1;
    }

    public int getTilesPerColByLevel(int i) {
        return -1;
    }

    public int getNumBands() {
        return -1;
    }

    public SeRasterBand getBandInfo(int i) throws SeException {
        return null;
    }

    public SeObjectId getRasterColumnId() {
        return null;
    }

    public SeExtent getExtent() throws SeException {
        return null;
    }

    public void setExtent(SeExtent ext) {
    }

    public SeRaster getRasterInfo() throws SeException, CloneNotSupportedException {
        return null;
    }

    public void setImageSize(int h, int w, int d) {
    }

    public void setTileSize(int w, int h) {
    }

    public void setCompressionType(int c) {
    }

    public void setMaskMode(boolean b) {
    }

    public void setImportMode(boolean b) {
    }

    public void setRasterProducer(SeRasterProducer p) {
    }

}
