## twrpbuilder_tree_generator
Our magic script to generate omni twrp device tree just using a backup which contain recovery.img and build.prop!

### Download :-
[Get latest jar file](https://github.com/HemanthJabalpuri/twrpbuilder_tree_generator/releases)

### Usage :-
```
-- USAGE --
usage: TwrpBuilder [-aik] [-f <arg>] [-h] [-l] [-otg] [-r <arg>] [-t <arg>]
-- HELP --
usage: java -jar twrp.jar -f backupfile.tar.gz [-aik] [-f <arg>] [-h] [-l]
       [-otg] [-r <arg>] [-t <arg>]
HELP
   -aik,--Android_Image_Kitchen     Extract backup or recovery.img using Android
                                    Image kitchen
   -f,--file <arg>                  build using backup file (made from app).
   -h,--help                        print this help
   -l,--land-scape                  enable landscape mode
   -otg,--otg-support               add otg support to fstab
   -r,--recovery <arg>              build using recovery image file
   -t,--type <arg>                  supported option :- mt, samsung, mrvl
```
