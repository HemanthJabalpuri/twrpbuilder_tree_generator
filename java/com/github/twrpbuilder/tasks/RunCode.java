package com.github.twrpbuilder.tasks;

import com.github.twrpbuilder.interfaces.Tools;
import com.github.twrpbuilder.interfaces.ToolsInterface;
import com.github.twrpbuilder.models.DeviceModel;
import com.github.twrpbuilder.models.OptionsModel;
import com.github.twrpbuilder.mkTree.MakeTree;
import com.github.twrpbuilder.utils.ExtractBackup;

public class RunCode extends Thread implements Runnable {
    private DeviceModel deviceModel = new DeviceModel();
    private ToolsInterface tool = new Tools();
    private OptionsModel model;

    public RunCode(String name, OptionsModel model) {
        this.model = model;
        tool.cp(name, "build.tar.gz");
        if (model.isAndroidImageKitchen()) {
            System.out.println("Using Android Image Kitchen to extract " + name);
            tool.extract("bin");
            tool.extract("unpackimg.sh");
        } else {
            tool.extract("umkbootimg");
            tool.extract("magic");
        }
    }

    public RunCode(String name, String type, OptionsModel model) {
        this.model = model;
        deviceModel.setType(type);
        tool.cp(name,"build.tar.gz");
        if (type.equals("mrvl")) {
            tool.extract("degas-umkbootimg");
            tool.command("mv degas-umkbootimg umkbootimg ");
            deviceModel.setMrvl(true);
        } else if (type.equals("mtk")) {
            tool.extract("unpack-MTK.pl");
            tool.command("mv unpack-MTK.pl umkbootimg");
            deviceModel.setMtk(true);
        } else if (type.equals("samsung")) {
            tool.extract("umkbootimg");
            deviceModel.setSamsung(true);
        }
        if (model.isAndroidImageKitchen()) {
            tool.extract("bin");
            tool.extract("unpackimg.sh");
        }
    }

    @Override
    public void run() {
        if (model.isExtract())
            new ExtractBackup("build.tar.gz");
        new MakeTree(deviceModel, model);
    }
}
