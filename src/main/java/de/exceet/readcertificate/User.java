package de.exceet.readcertificate;

import java.io.*;
import java.util.List;

public class User {
    private String name, password, defaultDirectory;
    private int defaultStyle;
    private List<String> userdata;

    public boolean setUser(Main main, String userName) {
        try (LineNumberReader rdr = new LineNumberReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("users.txt")))) {
            String currentLine;
            while ((currentLine = rdr.readLine()) != null) {
                if ((rdr.getLineNumber() % 5) == 1) {
                    if (currentLine.equals(userName)) {
                        name = userName;
                        password = rdr.readLine();
                        defaultDirectory = rdr.readLine();
                        defaultStyle = Integer.parseInt(rdr.readLine());

                    }
                }
            }
            return name == null;
        } catch (Exception e) {
            main.printError("users.txt file is missing");
            e.printStackTrace();
            return true;
        }
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public int getDefaultStyle() {
        return defaultStyle;
    }
}
