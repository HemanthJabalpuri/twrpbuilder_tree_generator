#!/bin/sh

mkdir out
java -source 1.7 -target 1.7 -sourcepath java java/com/github/twrpbuilder/MainActivity.java -d out

jar cvfm TwrpBuilder.jar twrpbuilder.mf -C out/ . assets/
