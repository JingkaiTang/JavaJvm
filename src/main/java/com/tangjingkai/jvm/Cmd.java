package com.tangjingkai.jvm;

import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * Created by totran on 11/12/16.
 */
public class Cmd {
    public static final String USAGE = "JavaJvm [options] class [args...]";
    public String cpOption;
    public String xjreOption;
    public String clsFile;
    public String[] args;

    public static Cmd parse(String[] rawArgs) {
        Option help = new Option("help", false, "print help message");
        Option question = new Option("?", false, "print help message");
        OptionGroup helpGroup = new OptionGroup()
                .addOption(help)
                .addOption(question);
        Option version = new Option("version", false, "print version and exit");
        Option classpath = new Option("classpath", true, "classpath");
        Option cp = new Option("cp", true, "classpath");
        OptionGroup cpGroup = new OptionGroup()
                .addOption(classpath)
                .addOption(cp);
        Option xjre = new Option("Xjre", true, "path to jre");

        Options options = new Options()
                .addOptionGroup(helpGroup)
                .addOption(version)
                .addOptionGroup(cpGroup)
                .addOption(xjre);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cl = parser.parse(options, rawArgs);

            if (helpGroup.getSelected() != null) {
                printUsage(options);
                return null;
            }

            if (cl.hasOption(version.getOpt())) {
                System.out.println(Jvm.VERSION);
                return null;
            }

            String[] args = cl.getArgs();

            if (args.length == 0) {
                printUsage(options);
                return null;
            }

            Cmd cmd = new Cmd();

            String cpOption = "";
            if (cpGroup.getSelected() != null) {
                cpOption = cl.getOptionValue(cpGroup.getSelected());
            }

            String xjreOption = "";
            if (cl.hasOption(xjre.getOpt())) {
                xjreOption = cl.getOptionValue(xjre.getOpt());
            }

            cmd.cpOption = cpOption;
            cmd.xjreOption = xjreOption;
            cmd.clsFile = args[0];
            cmd.args = Arrays.copyOfRange(args, 1, args.length);

            return cmd;
        } catch (ParseException e) {
            printUsage(options);
        }
        return null;
    }

    private static void printUsage(Options options) {
        new HelpFormatter().printHelp(USAGE, options);
    }

    @Override
    public String toString() {
        return "Cmd{" +
                "cpOption='" + cpOption + '\'' +
                ", clsFile='" + clsFile + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
