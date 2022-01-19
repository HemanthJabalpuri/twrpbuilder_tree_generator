package com.github.twrpbuilder.models;

import java.io.Serializable;

public class DeviceModel implements Serializable {
    private String brand;
    private String codename;
    private String model;
    private String platform;
    private String abi;
    private String fingerprint;
    private String type;
    private String path;
    private boolean mtk;
    private boolean encrypted;
    private boolean mrvl;
    private boolean samsung;

    public DeviceModel() {}

    public DeviceModel(
            String brand,
            String codename,
            String model,
            String type,
            String platform,
            String abi,
            String fingerprint,
            String path,
            boolean mtk,
            boolean mrvl,
            boolean samsung,
            boolean encrypted) {
        this.brand = brand;
        this.codename = codename;
        this.model = model;
        this.platform = platform;
        this.abi = abi;
        this.fingerprint = fingerprint;
        this.type = type;
        this.path = path;
        this.mtk = mtk;
        this.encrypted = encrypted;
        this.mrvl = mrvl;
        this.samsung = samsung;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isMtk() {
        return mtk;
    }

    public void setMtk(boolean mtk) {
        this.mtk = mtk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isMrvl() {
        return mrvl;
    }

    public void setMrvl(boolean mrvl) {
        this.mrvl = mrvl;
    }

    public boolean isSamsung() {
        return samsung;
    }

    public void setSamsung(boolean samsung) {
        this.samsung = samsung;
    }
}
