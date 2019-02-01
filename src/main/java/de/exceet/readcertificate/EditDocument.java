package de.exceet.readcertificate;

import java.io.*;
import java.util.Scanner;

public class EditDocument {
    /**
     * Reads a *.txt document and prints every line with line number to the console
     *
     * @param f    file name
     * @param pF   directory path
     * @param main main object of the Main class (needed to call all printError/-Info/-Help/-Console functions)
     */
    public void read(String f, String pF, Main main) {
        try {
            FileReader fr;
            if (pF == null || f == null) {
                main.printError("you need to enter a file name");
                main.printInfo("for example use 'rd --file example' -> will read the file example.txt");
            } else {
                BufferedReader br = new BufferedReader(new FileReader(pF + "/" + f + ".txt"));
                BufferedReader fR = new BufferedReader(new FileReader(pF + "/" + f + ".txt"));
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
            main.printInfo("dont use example.txt as file name but only example");
        } catch (IOException ioe) {
            main.printError("could not read file " + f + ".txt");
        }
    }

    /**
     * Writes a *.txt document and writes it to the file
     *
     * @param f             file name
     * @param pF            directory name
     * @param main          main object of the Main class (needed to call all printError/-Info/-Help/-Console functions)
     * @param numberOfLines number of lines the file will have
     */
    public void write(String f, String pF, Main main, int numberOfLines) {
        BufferedWriter bw;
        if (pF == null || f == null)
            main.printError("you need to enter a file name");
        else {
            try {
                bw = new BufferedWriter(new FileWriter(pF + "/" + f + ".txt"));
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
