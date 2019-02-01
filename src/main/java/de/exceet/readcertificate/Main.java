package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.security.cert.CertificateException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.util.*;

public class Main {
    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_RED = "\u001B[91m";
    private final String ANSI_GREEN = "\u001B[92m";
    private final String ANSI_YELLOW = "\u001B[93m";
    private final String ANSI_BLUE = "\u001B[94m";

    @Parameter(names = {"writeCertificate", "wc"}, description = "generate a new certificate", help = true)
    private boolean writeC;
    @Parameter(names = {"writeDocument", "wd"}, description = "changes to J-Console", help = true)
    private boolean writeD;
    @Parameter(names = {"readCertificate", "rc"}, description = "read a certificate", help = true)
    private boolean readC;
    @Parameter(names = {"readDocument", "rd"}, description = "reads a file", help = true)
    private boolean readD;
    @Parameter(names = {"changeDirectory", "cd"}, description = "changes working directory", help = true)
    private boolean cd;
    @Parameter(names = "help", description = "prints out a general help", help = true)
    private boolean gHelp;
    @Parameter(names = "exit", description = "exits J-Console/J-Editor", help = true)
    private static boolean exit;
    //-------------------------------+
    @Parameter(names = {"--issuerName", "--iName"}, description = "eneter the ca name")
    private String iName;
    @Parameter(names = {"--subjectName", "--sName"}, description = "enter the owner name")
    private String sName;
    @Parameter(names = {"--startDate", "--sDate"}, description = "startdate for the certificate to be valid")
    private String sDate;
    @Parameter(names = {"--expiryDate", "--eDate"}, description = "expirydate for the certificate to be valid")
    private String eDate;
    @Parameter(names = "--keySize", description = "keySize of the public key (in bits)")
    private int keys;
    @Parameter(names = {"--serialNumber", "--serNumb"}, description = "set a serial number")
    private long serNumber;
    @Parameter(names = "--file", description = "set the certificate name")
    private String fileName;
    @Parameter(names = {"--signatureAlgorithm", "signAlg"}, description = "set signature algorithm")
    private String signAlg;
    @Parameter(names = "--read", description = "decide if you want to read the certificate after generating it")
    private boolean bRead;
    @Parameter(names = {"--help", "-h"}, description = "prints out a help for the command entered before")
    private boolean help;
    @Parameter(names = "--config", description = "set a config path file")
    private String cFile;
    @Parameter(names = "--lines", description = "prints out the number of lines the document should have", help = true)
    private int lines;

    private String pFile;

    public void main() {
        Main main = new Main();
        EditDocument ed = new EditDocument();
        Scanner sc = new Scanner(System.in);
        String in;
        JCommander jc;
        String[] sin; //args.clone();
        main.exit = false;
        boolean start;
        main.cd = true;

        while (!main.exit) {
            if (main.cd) {
                start = false;
                while (!start) {
                    main.printEditor("Enter directory");
                    in = sc.nextLine();
                    if (in.split("/")[0].length() == 2) {
                        main.pFile = in;
                        start = true;
                    } else if (!in.equals("exit")) {
                        main.printError("the path file you entered is not valid.");
                        main.printInfo("Use '/' for example C:/Users");
                    } else {
                        main.printInfo("exiting ...");
                        System.exit(1);
                    }
                }
            }
            main.cd = false;
            main.setToDefault();
            main.printEditor(main.pFile);
            in = sc.nextLine();
            sin = in.split(" ");
            if (sin.length != 0 && !in.equals("")) {
                try {
                    jc = JCommander.newBuilder().addObject(main).build();
                    jc.parse(sin);
                    main.run(main, main.pFile, ed);
                } catch (com.beust.jcommander.ParameterException pe) {
                    main.printError("unknown command or parameters");
                    main.exit = false;
                    main.cd = false;
                }
            }
        }
        printInfo("exiting...");
    }

    private void run(Main main, String pF, EditDocument ed) {
        if (gHelp || help) {
            if (gHelp) {
                printHelpToConsole(6);
            }
            if (readD) {
                printHelpToConsole(3);
            }
            if (writeD) {
                printHelpToConsole(5);
            }
            if (readC) {
                printHelpToConsole(1);
            }
            if (writeC) {
                printHelpToConsole(0);
            }
            if (cd) {
                printHelpToConsole(4);
                main.cd = false;
            }
            if (main.exit) {
                printHelpToConsole(2);
                main.exit = false;
            }
        } else {
            if (!main.exit) {
                if (cd) {
                    main.cd = true;
                } else {
                    String defaultConfigFileName = "config.properties";
                    cFile = defaultString(cFile, "");
                    EditCertificate rc = new EditCertificate();
                    Properties dProps;

                    if (cFile.equals("")) {
                        dProps = readProperties(defaultConfigFileName, true, true);
                    } else {
                        dProps = readProperties(cFile + "/" + defaultConfigFileName, true, false);
                    }

                    String dIssuerName = dProps.getProperty("defaultIssuerName", "ca_name");
                    String dSubjectName = dProps.getProperty("defaultSubjectName", "owner_name");
                    int dKeys = Integer.valueOf(dProps.getProperty("defaultHeySize", "4096"));
                    String dPropsSerNumber = dProps.getProperty("defaultSerialNumber", "default");
                    String dPropsStDate = dProps.getProperty("defaultStDate", "default");
                    String dPropsExDate = dProps.getProperty("defaultExDate", "default");
                    String dPropsValidity = dProps.getProperty("defaultValidity", "default");
                    String dSignAlg = dProps.getProperty("defaultSignatureAlgorithm", "SHA256withRSA");

                    Date dStDate, dExDate = new Date();
                    long milSecValid = 31536000000L, dSerNumber;

                    if (dPropsStDate.equals("default"))
                        dStDate = new Date();
                    else
                        dStDate = stringToDate(dPropsStDate);

                    if (!dPropsValidity.equals("default"))
                        milSecValid = Long.valueOf(dPropsValidity);

                    if (dPropsExDate.equals("default"))
                        dExDate.setTime(dStDate.getTime() + milSecValid);
                    else
                        dExDate = stringToDate(dPropsExDate);

                    if (dPropsSerNumber.equals("default"))
                        dSerNumber = new Date().getTime();
                    else
                        dSerNumber = Long.valueOf(dPropsSerNumber);

                    if (readD) {
                        if (fileName == null)
                            main.printError("you have to enter a file name with the argument --file <filename>");
                        else
                            ed.read(fileName, pF, main);
                    } else if (writeD) {
                        if (fileName == null)
                            main.printError("you have to enter a file name with the argument --file <filename>");
                        else {
                            lines = main.defaultInt(lines, 10, 1);
                            fileName = main.defaultString(fileName, "auto_generated_file.txt");
                            ed.write(fileName, main.pFile, main, lines);
                        }
                    } else if (writeC) {
                        if (fileName == null)
                            main.printError("you have to enter a file name with the argument --file <filename>");
                        else
                            startWriter(rc, dIssuerName, dSubjectName, dStDate, dExDate, dKeys, dSerNumber, dSignAlg, main);
                    } else if (readC) {
                        if (fileName == null)
                            main.printError("you have to enter a file name with the argument --file <filename>");
                        else
                            startReader(rc, main);
                    }
                }
            }
        }
    }

    /**
     * Prints out the help for the command.
     *
     * @param x tells the function the help of what command it should print<br>
     *          (0 = writeCertificate, 1 = readCertificate, 2 = exit, 3 = readDocument, 4 = cd, 5 = writeDocument, 6 = general help)
     */
    private void printHelpToConsole(int x) {
        if (x == 0) {
            printHelp("writeCertificate | wc\t\t[generates a certificate]");
            printHelp("\t\t--issuerName\t\t<CA-name>");
            printHelp("\t\t--subjectName\t\t<owner-name>");
            printHelp("\t\t--startDate\t\t<start date of the certificate>");
            printHelp("\t\t--expiryDate\t\t<expiry date of the certificate>");
            printHelp("\t\t--keySize\t\t<size of the public key in bits>");
            printHelp("\t\t--serialNumber\t\t<serial number of the certificate>");
            printHelp("\t\t--signatureAlgorithm\t<signature algorithm>");
            printHelp("\t\t--file\t\t\t<name of the generated certificate>");
            printHelp("\t\t--config\t\t<set the pathfile of the config.properties file you want to use>");
            printHelp("\t\t--read\t\t\t[enables read]");
        } else if (x == 1) {
            printHelp("readCertificate | rc \t\t[reads a certificate]");
            printHelp("\t\t--file\t\t\t<name of the file to read>");
        } else if (x == 2) {
            printHelp("exit\t\t\t\t[exits the console]");
        } else if (x == 3) {
            printHelp("readDocument | rd\t\t[reads a *.txt file with your filename]");
            printHelp("\t\t--file\t\t\t<name of the file to read>");
        } else if (x == 4) {
            printHelp("cd\t\t\t\t[gives you the option to select another directory]");
        } else if (x == 5) {
            printHelp("writeDocument | wd\t\t[writes a *.txt file to you filename]");
            printHelp("\t\t--file\t\t\t<name of the file to write>");
            printHelp("\t\t--lines\t\t\t<amount of lines you want to write>");
        } else if (x == 6) {
            printHelp("writeCertificate | wc\t\t[generates a certificate]");
            printHelp("readCertificate | rc\t\t[reads a certificate]");
            printHelp("writeDocument | wd\t\t[writes a file to you filename]");
            printHelp("readDocument | rd\t\t[reads the file with your filename]");
            printHelp("cd\t\t\t\t[gives you the option to select another directory]");
            printHelp("exit\t\t\t\t[exits the console]");
            printHelp("");
            printHelp("Use <command> -h | --help to get further information about the command and the parameters you can apply to it");
        }
    }

    /**
     * Tests if the String in is set.
     *
     * @param in the input String
     * @param d  the default value for the input String
     * @return if in != null -> in <br>
     * If in == null -> d
     */
    public String defaultString(String in, String d) {
        if (in == null) {
            in = d;
        }
        return in;
    }

    /**
     * Tests if the int in is bigger than the int val.
     *
     * @param in  the input String
     * @param d   the default value for the input String
     * @param val the test value
     * @return if in > val -> in<br>
     * If in <= val -> d
     */
    public int defaultInt(int in, int d, int val) {
        if (in < val) {
            in = d;
        }
        return in;
    }

    /**
     * Tests if the int in is bigger than the int val.
     *
     * @param in  the input String
     * @param d   the default value for the input String
     * @param val the test value
     * @return if in > val -> in<br>
     * If in <= val -> d
     */
    private long defaultLong(long in, long d, int val) {
        if (in < val) {
            in = d;
        }
        return in;
    }

    /**
     * Tests if the String in is set.
     *
     * @param in the input String
     * @param d  the default value for the input String
     * @return if in != null -> in <br>
     * If in == null -> d
     */
    private Date defaultDate(String in, Date d) {
        Date out;
        if (in == null) {
            out = d;
        } else {
            out = stringToDate(in);
        }
        return out;
    }

    /**
     * The function stringToDate() converts a String (i) into a date value. Important for that is, that the String got
     * the format DD-MM-YYYY. If that isn't the case it will close the program with System.exit(). If it works it will
     * return the date in "Date" format.
     *
     * @param i string in DD-MM-YYYY format
     * @return date in Date format as output
     */
    private Date stringToDate(String i) {
        int d = 0, y = 0, m = 0;
        try {
            String[] sa = i.split("");
            d = (Integer.valueOf(sa[0])) * 10 + (Integer.valueOf(sa[1]));
            m = (Integer.valueOf(sa[3])) * 10 + (Integer.valueOf(sa[4]));
            y = (Integer.valueOf(sa[6])) * 1000 + (Integer.valueOf(sa[7])) * 100 + (Integer.valueOf(sa[8])) * 10 + (Integer.valueOf(sa[9]));
        } catch (Exception e) {
            // out.println(e);
            System.out.println("No valid date");
            System.exit(1);
        }
        Date o = new GregorianCalendar(y, m - 1, d).getTime();
        // out.println(o);
        return o;
    }

    /**
     * Starts the Certificate generator. Needs all default values for the Certificate parameters.
     *
     * @param rc         editCertificate class object that's needed to start the Certificate generator in the that Class
     * @param dStDate    default start date the Certificate generator should use if it isn't set
     * @param dExDate    default expiry date the Certificate generator should use if it isn't set
     * @param dKeys      default key size the Certificate generator should use if it isn't set
     * @param dSerNumber default serial number the Certificate generator should use if it isn't set
     * @param dSignAlg   default signature algorithm the Certificate generator should use if it isn't set
     * @param main       main class object
     */
    private void startWriter(EditCertificate rc, String dIssuerName, String dSubjectName, Date dStDate, Date dExDate, int dKeys, long dSerNumber,String dSignAlg, Main main) {
        printInfo("checking inputs");
        iName = defaultString(iName, dIssuerName);
        sName = defaultString(sName, dSubjectName);
        Date stDate = defaultDate(sDate, dStDate);
        Date exDate = defaultDate(eDate, dExDate);
        keys = defaultInt(keys, dKeys, 512);
        serNumber = defaultLong(serNumber, dSerNumber, 1);
        signAlg = defaultString(signAlg, dSignAlg);

        printInfo("generating key pair");

        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(keys);
        KeyPair keypair = keyGen.generateKeyPair();

        File file = new File(main.pFile + "/" + fileName + ".crt");

        try {
            rc.write(file, "CN = " + iName, "CN = " + sName, keypair, serNumber, stDate, exDate, signAlg);
        } catch (CertificateEncodingException e) {
            printError("Couldn't encode the certificate");
        } catch (SignatureException e) {
            printError("Couldn't sign the certificate");
        } catch (InvalidKeyException e) {
            printError("The generated key isn't valid");
        } catch (IOException e) {
            printError("Couldn't write the certificate");
        } catch (NoSuchAlgorithmException e) {
            printError("The entered algorithm is wrong");
        }

        if (bRead) {
            try {
                rc.printCertDataToConsole(rc.read(file));
            } catch (IOException e) {
                printError("[ERROR] Couldn't find the certificate to read");
            } catch (CertificateException e) {
                printError("[ERROR] Couldn't read the certificate");
            }
        }

    }

    /**
     * Starts the Certificate reader. Needs all default values for the reading parameters.
     *
     * @param rc        readCertificate class object that's needed to start the Certificate reader in the that Class
     * @param main      main class object
     */
    private void startReader(EditCertificate rc,Main main) {
        File file = new File(main.pFile + "/" + fileName + ".crt");
        try {
            rc.printCertDataToConsole(rc.read(file));
        } catch (IOException e) {
            printError("Couldn't find the certificate to read");
        } catch (CertificateException e) {
            printError("Couldn't read the certificate");
        }
    }

    /**
     * Reads the config.properties file in the main project folder or a custom config.properties file given with the
     * --config parameter
     *
     * @param configFileName name of the config file (with directory unless its the the config.properties file in hte resources folder.
     * @param printMsg       should be true unless it gets called in the test run
     * @param defaultPath    if the name is just config.properties -> true <br>
     *                       if the name is with directory (for example C:/.../config.properties -> false
     * @return object of the type Properties (with object.getProperty(property) you can get the value for the property
     */
    public Properties readProperties(String configFileName, boolean printMsg, boolean defaultPath) {
        if (printMsg)
            if (defaultPath)
                printInfo("loading config.properties");
            else
                printInfo("loading " + configFileName);
        Properties prop = new Properties();

        if (defaultPath) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
                if (input != null)
                    prop.load(input);
                else
                    throw new IOException();

                printInfo("successfully loaded settings from " + configFileName);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            try (InputStream input = new FileInputStream(configFileName)) {
                if (input != null)
                    prop.load(input);
                else
                    throw new IOException();

                printInfo("successfully loaded settings from " + configFileName);

            } catch (IOException ex) {
                if (printMsg) {
                    printError("no file could be found at " + configFileName);
                    printInfo("The name of the config file must be config.properties");
                    printInfo("using default config.properties file");

                }
            }
        }
        return prop;
    }

    /**
     * Resets all the Mains global variables needed for JCommander
     */
    private void setToDefault() {
        writeC = false;
        readC = false;
        readD = false;
        cd = false;
        writeD = false;
        gHelp = false;
        //-------+
        iName = null;
        sName = null;
        sDate = null;
        eDate = null;
        keys = 0;
        serNumber = 0L;
        fileName = null;
        signAlg = null;
        bRead = false;
        help = false;
        cFile = null;
    }

    /**
     * Prints the message with a blue [INFO] in front of it
     *
     * @param msg the message after the [INFO]
     */
    public void printInfo(String msg) {
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a red [ERROR] in front of it
     *
     * @param msg the message after the [ERROR]
     */
    public void printError(String msg) {
        System.out.println("[" + ANSI_RED + "ERROR" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a green [HELP] in front of it
     *
     * @param msg the message after the [HELP]
     */
    public void printHelp(String msg) {
        System.out.println("[" + ANSI_GREEN + "HELP" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a blue [-] in front of it
     *
     * @param msg the message after the [-]
     */
    public void printCertData(String msg) {
        System.out.println("[" + ANSI_BLUE + "-" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a red [-] in front of it
     *
     * @param msg the message after the [-]
     */
    public void printRedCertData(String msg) {
        System.out.println("[" + ANSI_RED + "-" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a blue [number of the lines] in front of it for example [25]
     *
     * @param msg the message after the [number of the lines]
     * @param i   the number inside the []
     * @param max the maximum Number of the list (important for the amount of " " in front of the number)
     */
    public void printDocumentData(String msg, int i, int max) {
        if (max / 1000. > 1 && i / 10. < 1) {
            System.out.println("[" + ANSI_BLUE + "   " + i + ANSI_RESET + "] " + msg);
        } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
            System.out.println("[" + ANSI_BLUE + "  " + i + ANSI_RESET + "] " + msg);
        } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1) || (max / 10. > 1 && i / 10. < 1)) {
            System.out.println("[" + ANSI_BLUE + " " + i + ANSI_RESET + "] " + msg);
        } else {
            System.out.println("[" + ANSI_BLUE + i + ANSI_RESET + "] " + msg);
        }
    }

    /**
     * Prints a yellow [J-CONSOLE> in front of the message and the msg after it also in yellow
     *
     * @param msg the message in yellow after the [J-CONSOLE>
     */
    public void printEditor(String msg) {
        System.out.print("[" + ANSI_YELLOW + "J-CONSOLE" + ANSI_RESET + "> " + ANSI_YELLOW + msg + ANSI_RESET + "> ");
    }

    /**
     * Prints a blue [number of the lines] for example [25]
     *
     * @param i   the number inside the []
     * @param max the maximum Number of the list (important for the amount of " " in front of the number)
     */
    public void printEditorInput(int i, int max) {
        if (max / 1000. > 1 && i / 10. < 1) {
            System.out.print("[" + ANSI_YELLOW + "   " + i + ANSI_RESET + "] ");
        } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
            System.out.print("[" + ANSI_YELLOW + "  " + i + ANSI_RESET + "] ");
        } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1) || (max / 10. > 1 && i / 10. < 1)) {
            System.out.print("[" + ANSI_YELLOW + " " + i + ANSI_RESET + "] ");
        } else {
            System.out.print("[" + ANSI_YELLOW + i + ANSI_RESET + "] ");
        }
    }
}