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
    public String cd;
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
        boolean start = false;

        while (!start) {
            main.printEditor("enter the pathfile to your working directory");
            in = sc.nextLine();
            if (in.split("/")[0].length() == 2) {
                ed.pFile = in;
                start = true;
            } else {
                main.printError("the path file you entered is not valid.");
                main.printInfo("Use '/' for example C:/Users");
            }
        }

        while (!exit && !changeToConsole) {
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
                if (cd != null || gHelp) {
                    printHelpToConsole(1);
                }
                if (console || gHelp) {
                    printHelpToConsole(2);
                }
            } else {
                if (cd != null) {
                    if (cd.split("/")[0].length() == 2) {
                        ed.pFile = cd;
                    } else {
                        main.printError("the path file you entered is not valid.");
                        main.printInfo("Use '/' for example C:/Users");
                    }
                }if (read) {
                    if(file == null)
                        main.printError("you have to enter a file name with the argument --file <filename>");
                    else
                        read(file, pF, main);
                }else if(write){
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
        cd = null;
        write = false;
        lines = 0;
        file = null;
    }

    private void printHelpToConsole(int x) {
        Main main = new Main();
        if (x == 0) {
            main.printHelp("read\t\t<file name>");
        } else if (x == 1) {
            main.printHelp("cd\t\t\t<new directory>");
        } else if (x == 2) {
            main.printHelp("console\t\t[returns to console]");
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
    private void write(String f, String pF, Main main, int numberOfLines){
        BufferedWriter bw;
        if (pF == null || f == null)
            main.printError("you need to enter a file name");
        else{
            try {
                bw = new BufferedWriter(new FileWriter(pF + "/" + f));
                Scanner sc = new Scanner(System.in);
                String in;
                main.printInfo("Now you can write " + numberOfLines + " line/-s to your selected document:");
                int c = 0;
                while(numberOfLines > c){
                    main.printEditorInput(c+1, numberOfLines);
                    in = sc.nextLine();
                    bw.write(in);
                    bw.newLine();
                    //out = out + in + "\n";
                    c++;
                }
                bw.close();
            }catch(IOException ioe){
                main.printError("directory couldn't be found");
            }
        }
    }
}
