package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.*;
import java.util.Scanner;

public class EditDocument {
    @Parameter(names = "console", description = "changes to J-Console", help = true)
    public boolean console;
    @Parameter(names = "write", description = "changes to J-Console", help = true)
    public boolean write;
    @Parameter(names = "exit", description = "exits J-Console/J-Editor", help = true)
    public static boolean exit;
    @Parameter(names = "read", description = "reads a file", help = true)
    public boolean read;
    @Parameter(names = "cd", description = "changes working directory", help = true)
    public boolean cd;
    @Parameter(names = "help", description = "prints out a general help", help = true)
    public boolean gHelp;
    //----------+
    @Parameter(names = "--help", description = "prints out a help for the command entered before", help = true)
    public boolean help;

    @Parameter(names = "--file", description = "prints out the file name", help = true)
    public String file;

    @Parameter(names = "--lines", description = "prints out the number of lines the document should have", help = true)
    public int lines;

    public static boolean changeToConsole;

    public String pFile;

    public void startEditor() {
        Main main = new Main();
        EditDocument ed = new EditDocument();
        Scanner sc = new Scanner(System.in);
        String in;
        JCommander jc;
        String[] sin; //args.clone();
        exit = false;
        changeToConsole = false;
        boolean start;
        ed.cd = true;

        while (!exit && !changeToConsole) {
            if (ed.cd) {
                start = false;
                while (!start) {
                    main.printEditor("Enter directory");
                    in = sc.nextLine();
                    if (in.split("/")[0].length() == 2) {
                        ed.pFile = in;
                        start = true;
                    } else if (!in.equals("exit") && !in.equals("console")) {
                        main.printError("the path file you entered is not valid.");
                        main.printInfo("Use '/' for example C:/Users");
                    } else if (in.equals("exit")) {
                        main.printInfo("exiting ...");
                        System.exit(1);
                    } else {
                        changeToConsole = true;
                        start = true;
                    }
                }
            }
            if(!changeToConsole) {
                ed.cd = false;
                ed.setEditorToDefault();
                main.printEditor(ed.pFile);
                in = sc.nextLine();
                sin = in.split(" ");
                if (sin.length != 0 && !in.equals("")) {
                    try {
                        jc = JCommander.newBuilder().addObject(ed).build();
                        jc.parse(sin);
                        ed.run(main, ed.pFile, ed);
                    } catch (com.beust.jcommander.ParameterException pe) {
                        main.printError("unknown command or parameters");
                        exit = false;
                    }
                }
            }
        }
        if (exit) {
            main.printInfo("exiting ...");
            System.exit(1);
        }

    }

    public void run(Main main, String pF, EditDocument ed) {
        if (console)
            changeToConsole = true;
        else {
            if (gHelp || help) {
                if (read || gHelp) {
                    printHelpToConsole(0);
                }
                if (write || gHelp) {
                    printHelpToConsole(3);
                }
                if (cd || gHelp) {
                    printHelpToConsole(1);
                }
                if (console || gHelp) {
                    printHelpToConsole(2);
                }
                if (exit || gHelp) {
                    printHelpToConsole(4);
                    exit = false;
                }
            } else {
                if (cd) {
                    ed.cd = true;
                } else if (read) {
                    if (file == null)
                        main.printError("you have to enter a file name with the argument --file <filename>");
                    else
                        read(file, pF, main);
                } else if (write) {
                    lines = main.defaultInt(lines, 10, 1);
                    file = main.defaultString(file, "auto_generated_file.txt");
                    write(file, ed.pFile, main, lines);
                }
            }
        }
    }

    private void setEditorToDefault() {
        console = false;
        read = false;
        help = false;
        gHelp = false;
        cd = false;
        write = false;
        lines = 0;
        file = null;
    }

    private void printHelpToConsole(int x) {
        Main main = new Main();
        if (x == 0) {
            main.printHelp("read\t\t\t[reads the file with your filename]");
            main.printHelp("\t   --file\t\t<name of the file to read>");
        } else if (x == 1) {
            main.printHelp("cd\t\t\t[gives you the option to select another directory]");
        } else if (x == 2) {
            main.printHelp("console\t\t\t[returns to console]");
        } else if (x == 4){
            main.printHelp("exit \t\t\t\t[exits the console]");
        } else if (x == 3){
            main.printHelp("write\t\t\t[writes a file to you filename]");
            main.printHelp("\t   --file\t\t<name of the file to write>");
            main.printHelp("\t   --lines\t\t<amount of lines you want to write>");
        }
    }

    private void read(String f, String pF, Main main) {
        try {
            FileReader fr;
            if (pF == null || f == null)
                main.printError("you need to enter a file name");
            else {
                BufferedReader br = new BufferedReader(new FileReader(pF + "/" + f));
                BufferedReader fR = new BufferedReader(new FileReader(pF + "/" + f));
                main.printInfo("reading file");
                String s;
                int c = 1, max = (int) fR.lines().count();
                while ((s = br.readLine()) != null) {
                    main.printDocumentData(s, c, max);
                    c++;
                }
                br.close();
                fR.close();
                main.printInfo("completed reading file");
            }
        } catch (FileNotFoundException fE) {
            main.printError("could not find file " + f + " (wrong directory or file name");
        } catch (IOException ioe) {
            main.printError("could not read file " + f);
        }
    }

    private void write(String f, String pF, Main main, int numberOfLines) {
        BufferedWriter bw;
        if (pF == null || f == null)
            main.printError("you need to enter a file name");
        else {
            try {
                bw = new BufferedWriter(new FileWriter(pF + "/" + f));
                Scanner sc = new Scanner(System.in);
                String in;
                main.printInfo("Now you can write " + numberOfLines + " line/-s to your selected document:");
                int c = 0;
                while (numberOfLines > c) {
                    main.printEditorInput(c + 1, numberOfLines);
                    in = sc.nextLine();
                    bw.write(in);
                    bw.newLine();
                    //out = out + in + "\n";
                    c++;
                }
                bw.close();
            } catch (IOException ioe) {
                main.printError("directory couldn't be found");
            }
        }
    }
}
