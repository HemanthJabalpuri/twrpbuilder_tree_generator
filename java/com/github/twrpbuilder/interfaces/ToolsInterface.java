package com.github.twrpbuilder.interfaces;

import java.io.File;
import java.util.LinkedList;

public interface ToolsInterface {
    String newLine = "\n";
    String seprator = File.separator;

    boolean fexist(String name);

    String command(String run);

    LinkedList command(String run, boolean LinkList);

    boolean mkdir(String name);

    boolean rm(String name);

    String copyRight();

    void cp(String from, String to);

    String propFile();

    void write(String name, String data);
    void clean();
    void extract(String name);
}
