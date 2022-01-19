package com.github.twrpbuilder.Models;

import java.io.Serializable;

public class OptionsModel implements Serializable {
    private boolean extract;
    private boolean AndroidImageKitchen;
    private boolean landscape;
    private boolean otg;

    public OptionsModel() {}

    public OptionsModel(
            boolean extract,
            boolean AndroidImageKitchen,
            boolean landscape,
            boolean otg) {
        this.extract = extract;
        this.AndroidImageKitchen = AndroidImageKitchen;
        this.landscape = landscape;
        this.otg = otg;
    }

    public boolean isAndroidImageKitchen() {
        return AndroidImageKitchen;
    }

    public void setAndroidImageKitchen(boolean androidImageKitchen) {
        AndroidImageKitchen = androidImageKitchen;
    }

    public boolean isExtract() {
        return extract;
    }

    public void setExtract(boolean extract) {
        this.extract = extract;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public boolean isOtg() {
        return otg;
    }

    public void setOtg(boolean otg) {
        this.otg = otg;
    }
}
