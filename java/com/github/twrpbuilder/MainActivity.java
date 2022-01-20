package com.github.twrpbuilder;

import com.github.twrpbuilder.interfaces.Tools;
import com.github.twrpbuilder.models.OptionsModel;
import com.github.twrpbuilder.tasks.RunCode;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class MainActivity extends Tools {
    public static String rName;

    public static Options constructPosixOptions() {
        final Options option = new Options();
        option.addOption("f", "file", true, "build using backup file (made from app).");
        option.addOption("t", "type", true, "supported option :- mtk, samsung, mrvl");
        option.addOption("l", "land-scape", false, "enable landscape mode");
        option.addOption("aik", "Android_Image_Kitchen", false, "Extract backup or recovery.img using Android Image kitchen");
        option.addOption("otg", "otg-support", false, "add otg support to fstab");
        option.addOption("r", "recovery", true, "build using recovery image file");
        option.addOption("h", "help", false, "print this help");
        return option;
    }

    public static void printHelp() {
        final PrintWriter writer = new PrintWriter(System.out);
        final int printedRowWidth = 80;
        final String commandLineSyntax = "java -jar TwrpBuilder.jar -f backupfile.tar.gz";
        final String header = "HELP";
        final int spacesBeforeOption = 3;
        final int spacesBeforeOptionDescription = 5;
        final String footer = "End of Help";
        final boolean displayUsage = true;

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                commandLineSyntax,
                header,
                constructPosixOptions(),
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);

        writer.flush();
    }

    public static void usePosixParser(final String[] commandLineArguments) {
        final CommandLineParser cmdLinePosixParser = new DefaultParser();
        final Options posixOptions = constructPosixOptions();
        final OptionsModel optionsModel = new OptionsModel();
        CommandLine commandLine;
        try {
            commandLine = cmdLinePosixParser.parse(posixOptions, commandLineArguments);

            if (commandLine.hasOption("h"))
                printHelp();

            if (commandLine.hasOption("aik"))
                optionsModel.setAndroidImageKitchen(true);

            if (commandLine.hasOption("otg"))
                optionsModel.setOtg(true);

            if (commandLine.hasOption("l"))
                optionsModel.setLandscape(true);

            if (commandLine.hasOption("r") || commandLine.hasOption("f")) {
                String g;
                if (commandLine.hasOption("f")) {
                    g = commandLine.getOptionValue("f");
                    optionsModel.setExtract(true);
                } else {
                    g = commandLine.getOptionValue("r");
                    rName = g;
                }
                if (new File(g).exists())
                    if (!g.contains(" ")) {
                        System.out.println("Building tree using: " + g);
                        if (commandLine.hasOption("t")) {
                            String t = commandLine.getOptionValue("t");
                            if (t.equals("mrvl") || t.equals("samsung") || t.equals("mtk"))
                                new RunCode(g, t, optionsModel).start();
                        } else
                            new Thread(new RunCode(g, optionsModel)).start();
                    } else
                        System.out.println("Please remove spaces from filename.");
                else
                    System.out.println(g + " does not exist.");
            }
        } catch (ParseException parseException) { // checked exception
            System.err.println(
                    "Encountered exception while parsing using PosixParser:\n"
                    + parseException.getMessage());
        }
    }

    public static void main(final String[] commandLineArguments) {
        if (commandLineArguments.length < 1)
            printHelp();
        usePosixParser(commandLineArguments);
    }
}
