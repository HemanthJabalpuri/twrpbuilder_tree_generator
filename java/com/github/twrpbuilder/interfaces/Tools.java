package com.github.twrpbuilder.interfaces;

import com.github.twrpbuilder.models.DeviceModel;
import com.github.twrpbuilder.models.PropData;
import com.github.twrpbuilder.utils.Config;
import static com.github.twrpbuilder.MainActivity.rName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;


public class Tools implements ToolsInterface {

    public long size;
    public String out = Config.outDir;

    @Override
    public boolean fexist(String name) {
        if (new File(name).exists())
            return true;
        else
            return false;
    }

    @Override
    public String command(String run) {
        Process process;
        String o = null;
        String[] commands = new String[] { "/bin/bash", "-c", run };
        StringBuilder linkedList = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(commands);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((o = bufferedReader.readLine()) != null) {
                linkedList.append(o.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return linkedList.toString();
    }

    @Override
    public LinkedList<String> command(String run, boolean LinkList) {
        Process process;
        String o = null;
        String[] commands = new String[]{"/bin/bash", "-c", run};
        LinkedList<String> linkedList = new LinkedList<>();
        try {
            process = Runtime.getRuntime().exec(commands);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((o = bufferedReader.readLine()) != null) {
                linkedList.add(o.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return linkedList;
    }

    @Override
    public boolean mkdir(String name) {
        File theDir = new File(name).getAbsoluteFile();

        // if the directory does not exist, create it
        if (!theDir.isDirectory()) {
            try {
                theDir.mkdirs();
            } catch(SecurityException se) {
                System.out.println("Failed to make dir " + name);
                System.exit(0);
            }
        } else
            System.out.println("Dir: " + name + " already exist");
        return theDir.isDirectory();
    }

    public void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents)
                deleteDir(f);
        }
        file.delete();
    }

    @Override
    public boolean rm(String name) {
        if (fexist(name)) {
            File file = new File(name);
            if (file.isDirectory()) {
                deleteDir(file);
                return true;
            } else if (file.isFile()) {
                file.delete();
                return true;
            } else
                return false;
        } else
            return false;
    }

    @Override
    public String copyRight() {
        String copy = "#\n" +
                "# Copyright (C) 2018 The TwrpBuilder Open-Source Project\n" +
                "#\n" +
                "# Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "# you may not use this file except in compliance with the License.\n" +
                "# You may obtain a copy of the License at\n" +
                "#\n" +
                "# http://www.apache.org/licenses/LICENSE-2.0\n" +
                "#\n" +
                "# Unless required by applicable law or agreed to in writing, software\n" +
                "# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "# See the License for the specific language governing permissions and\n" +
                "# limitations under the License.\n" +
                "#\n" +
                "\n";
        return copy;
    }

    @Override
    public void cp(String from, String to) {
        File f = new File(from);
        File t = new File(to);
        InputStream is = null;
        OutputStream os = null;
        if (t.exists())
            rm(t.getAbsolutePath());

        try {
            is = new FileInputStream(f);
            os = new FileOutputStream(t);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0)
                os.write(buffer, 0, length);
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String propFile() {
        String prop = null;
        if (new File("build.prop").exists())
            prop = "build.prop";
        else if (new File(out + "default.prop").exists())
            prop = out + "default.prop";
        else
            prop = "null";

        return prop;
    }

    LinkedList<PropData> propDataArray = new LinkedList<>();

    public void PropData(OnDataRequest request){
        DeviceModel deviceModel = new DeviceModel();
        propDataArray.add(new PropData(commonStr("ro.product.model"), "model"));
        propDataArray.add(new PropData(commonStr("ro.product.brand"), "brand"));
        propDataArray.add(new PropData(commonStr("ro.build.product"), "codename"));
        propDataArray.add(new PropData(commonStr("ro.board.platform"), "platform"));
        propDataArray.add(new PropData(commonStr("ro.product.cpu.abi"), "abi"));
        propDataArray.add(new PropData(commonStr("ro.build.fingerprint"), "fingerprint"));

        Iterator<PropData> iterator = propDataArray.iterator();
        while (iterator.hasNext()) {
            PropData propData = iterator.next();
            String chs;
            chs = command(propData.getCommand());
            switch (propData.getType()) {
                case "model":
                    deviceModel.setModel(chs);
                    break;
                case "brand":
                    if (chs.contains("-"))
                        chs = chs.replace("-", "_");
                    else if (chs.contains(" "))
                        chs = chs.replace(" ", "_");
                    deviceModel.setBrand(chs);
                    break;
                case "codename":
                    if (chs.equals(deviceModel.getBrand()) || chs.isEmpty())
                        chs = checkData(deviceModel.getModel()).toLowerCase();
                    deviceModel.setCodename(chs);
                    break;
                case "platform":
                    if (chs.isEmpty()) {
                        chs = command(commonStr("ro.mediatek.platform"));
                        if (chs.isEmpty())
                            chs="generic";
                    }
                    deviceModel.setPlatform(chs);
                    break;
                case "abi":
                    deviceModel.setAbi(chs);
                    break;
                case "fingerprint":
                    deviceModel.setFingerprint(chs);
                    break;
            }
        }
        String path = "device" + seprator + deviceModel.getBrand() + seprator + deviceModel.getCodename() + seprator;
        deviceModel.setPath(path);
        request.getData(deviceModel);
    }


    private String commonStr(String data){
        return "cat " + propFile() + " | grep -m 1 " + data + "= | cut -d = -f 2";
    }


    private String checkData(String data) {
        if (data.contains("-")) {
            String newstr = data.replace("-", "_");
            return newstr;
        } else if (data.contains(" ")) {
            String str = data.replace(" ", "_");
            return str;
        } else if(data.contains("+")) {
            String wut = data.replace("+", "");
            return wut;
        } else
            return data;
    }

    public Long getSize() {
        size = new File(Config.recoveryFile).length();
        return size;
    }

    @Override
    public void write(String name, String data) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(name, false));
            writer.println(data);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clean() {
        rm("build.prop");
        if (rName == null)
            rm(Config.recoveryFile);
        rm("mounts");
        rm("umkbootimg");
        rm(Config.outDir);
        rm("unpack-MTK.pl");
        rm("unpackimg.sh");
        rm("bin");
        rm("magic");
        rm("androidbootimg.magic");
        rm("build.tar.gz");
    }

    @Override
    public void extract(String name) {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        String resourceName = seprator + name;
        try {
            stream = Tools.class.getResourceAsStream(seprator + "assets" + resourceName);
            if (stream == null)
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");

            int readBytes;
            byte[] buffer = new byte[4096];
            jarFolder = System.getProperty("user.dir");
            resStreamOut = new FileOutputStream(jarFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0)
                resStreamOut.write(buffer, 0, readBytes);

            stream.close();
            resStreamOut.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
