package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class EditDocument {
    @Parameter(names = "console", description = "changes to J-Console", help = true)
    public boolean console;
    @Parameter(names = "exit", description = "exits J-Console/J-Editor", help = true)
    public static boolean exit;
    @Parameter(names = "read", description = "reads a file", help = true)
    public boolean read;
    @Parameter(names = "help", description = "prints out a general help", help = true)
    public boolean gHelp;
    //----------+
    @Parameter(names = "--help", description = "prints out a help for the command entered before", help = true)
    public boolean help;
    @Parameter(names = {"--pathFile", "--pFile"}, description = "set the path file")
    public String pFile;
    @Parameter(names = "--file", description = "set the file name")
    public String file;
    @Parameter(names = "--config", description = "set a config path file")
    public String cFile;

    public static boolean changeToConsole;

    public void startEditor() {
        Main main = new Main();
        EditDocument ed = new EditDocument();
        Scanner sc = new Scanner(System.in);
        String in;
        JCommander jc;
        String[] sin; //args.clone();
        exit = false;
        changeToConsole = false;
        while (!exit && !changeToConsole) {
            ed.setEditorToDefault();
            main.printEditor();
            in = sc.nextLine();
            sin = in.split(" ");
            if (sin.length != 0 && !in.equals("")) {
                try {
                    jc = JCommander.newBuilder().addObject(ed).build();
                    jc.parse(sin);
                    ed.run(main);
                } catch (com.beust.jcommander.ParameterException pe) {
                    main.printError("unknown command or parameters");
                }
            }
        }
        if (exit) {
            main.printInfo("exiting ...");
            System.exit(1);
        }
    }

    public void run(Main main) {
        if (console)
            changeToConsole = true;
        else {
            if (gHelp || help) {
                if (read || gHelp) {
                    printHelpToConsole(0);
                }
                if (console || gHelp) {
                    printHelpToConsole(1);
                }
            } else {
                String defaultConfigFileName = "config.properties";
                cFile = main.defaultString(cFile, "");
                ReadCertificate rc = new ReadCertificate();
                Properties dProps;

                if (cFile.equals("")) {
                    dProps = main.readProperties(defaultConfigFileName, true, true);
                } else {
                    dProps = main.readProperties(cFile + "/" + defaultConfigFileName, true, false);
                }


                if (read) {
                    read(file, pFile, main);
                }
            }
        }
    }

    private void setEditorToDefault() {
        console = false;
        read = false;
        pFile = null;
        file = null;
        help = false;
        gHelp = false;
    }

    private void printHelpToConsole(int x) {
        Main main = new Main();
        if (x == 0) {
            main.printHelp("read\t\t\t[reads a file]");
            main.printHelp("\t   --file\t\t<name of the generated certificate>");
            main.printHelp("\t   --pathFile\t\t<set the pathfile of the certificate>");
            main.printHelp("\t   --config\t\t<set the pathfile of the config.properties file you want to use>");
        } else if (x == 1){
            main.printHelp("console\t\t[returns to console]");
        }
    }

    private void read(String f, String pF, Main main) {
        try {
            FileReader fr;

            if (pF.equals(null) || f.equals(null))
                main.printError("you need to enter a file name and path file");
            else {
                fr = new FileReader(pF + "/" + f);
                BufferedReader br = new BufferedReader(fr);
                main.printInfo("reading file");
                String s;
                while((s = br.readLine()) != null){
                    main.printCertData(s);
                }
                main.printInfo("completed reading file");
            }
        } catch (FileNotFoundException fE) {
            main.printError("could not find file " + f);
        } catch (IOException ioe) {
            main.printError("could not read file " + f);
        } catch (NullPointerException nE){
            main.printError("you need to enter a file name and path file");
        }
    }
}
