package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.security.cert.CertificateException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.util.*;

public class Main {
    public String ANSI_RESET = "\u001B[0m";
    public String ANSI_ERROR = "\u001B[91m";
    public String ANSI_HELP = "\u001B[92m";
    public String ANSI_INPUT = "\u001B[93m";
    public String ANSI_OUTPUT = "\u001B[94m";

    @Parameter(names = {"setConfig", "sc"}, description = "copies the config.properties file to a custom file and sets it as default config file")
    public boolean setConfig;
    @Parameter(names = {"writeCertificate", "wc"}, description = "generate a new certificate")
    public boolean writeC;
    @Parameter(names = {"writeDocument", "wd"}, description = "changes to J-Console")
    public boolean writeD;
    @Parameter(names = {"readCertificate", "rc"}, description = "read a certificate")
    public boolean readC;
    @Parameter(names = {"readDocument", "rd"}, description = "reads a file")
    public boolean readD;
    @Parameter(names = {"changeDirectory", "cd"}, description = "changes working directory")
    public boolean cd;
    @Parameter(names = "help", description = "prints out a general help")
    public boolean gHelp;
    @Parameter(names = "exit", description = "exits J-Console/J-Editor")
    public static boolean exit;
    @Parameter(names = {"encodeDocument", "ed"}, description = "encodes a text file with a certificate")
    public boolean et;
    @Parameter(names = {"decodeDocument", "dd"}, description = "decodes a text file with a private key")
    public boolean dt;
    @Parameter(names = {"changeStyle", "cs"}, description = "changes between colored and non-colored mode")
    public boolean cs;
    //-------------------------------+
    @Parameter(names = {"--issuerName", "--iName"}, description = "eneter the ca name")
    public String iName;
    @Parameter(names = {"--subjectName", "--sName"}, description = "enter the owner name")
    public String sName;
    @Parameter(names = {"--startDate", "--sDate"}, description = "startdate for the certificate to be valid")
    public String sDate;
    @Parameter(names = {"--expiryDate", "--eDate"}, description = "expirydate for the certificate to be valid")
    public String eDate;
    @Parameter(names = "--keySize", description = "keySize of the public key (in bits)")
    public int keys;
    @Parameter(names = {"--serialNumber", "--serNumb"}, description = "set a serial number")
    public long serNumber;
    @Parameter(names = "--file", description = "set the certificate name")
    public String fileName;
    @Parameter(names = {"--certificateFile", "--certFile"}, description = "set the certificate name")
    public String certFileName;
    @Parameter(names = {"--signatureAlgorithm", "signAlg"}, description = "set signature algorithm")
    public String signAlg;
    @Parameter(names = "--read", description = "decide if you want to read the certificate after generating it")
    public boolean bRead;
    @Parameter(names = {"--help", "-h"}, description = "prints out a help for the command entered before")
    public boolean help;
    @Parameter(names = "--lines", description = "prints out the number of lines the document should have")
    public int lines;
    @Parameter(names = "--copyConfig", description = "if the program should copy the config file to the directory")
    public boolean copyConfig;
    @Parameter(names = "--directory", description = "set the directory name")
    public String directoryName;
    @Parameter(names = "--certTargetDir", description = "set the custom certificate target folder")
    public String certTargetDirectory;
    @Parameter(names = "--certDirectory", description = "set the custom certificate folder")
    public String certDirectory;
    @Parameter(names = "--docDirectory", description = "set the custom document folder")
    public String docDirectory;
    @Parameter(names = "--style", description = "set a style")
    public String cStyle;
    @Parameter(names = "--toggle", description = "toggle the style")
    public boolean styleToggle;

    public int style = 0;
    private String pFile;
    public String configFile = "default";

    /**
     * Main function with the J-console input functionality <br>
     * When called first it shows [J-CONSOLE> Enter directory> <br>
     * After entering the working directory it will show [J-CONSOLE> C:/.../exampleFolder>
     * when it's read for an input <br>
     * After getting the input it will use JController to handle the input and it will call run() function to
     * call the needed functions
     */
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

    /**
     * Function to use the inputs from JCommander and call the needed functions with the given parameters <br>
     *
     * @param main Main class object (object where the cd and exit variable is saved
     * @param pF   directory path where the user with the editor is right now
     * @param ed   EditDocument class object (needed to call the write() and read() function for document editing)
     */
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
            if (et) {
                printHelpToConsole(7);
            }
            if (dt) {
                printHelpToConsole(8);
            }
            if (setConfig) {
                printHelpToConsole(9);
            }
            if (cd) {
                printHelpToConsole(4);
                main.cd = false;
            }
            if (cs) {
                printHelpToConsole(10);
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
                    if (cs) {
                        if (cStyle == null) {
                            cStyle = "somestring";
                            if (!styleToggle) {
                                style = 0;
                            }
                        }
                        if (cStyle.equals("default") || cStyle.equals("d")) {
                            style = 0;
                        } else if (cStyle.equals("non-colored") || cStyle.equals("nc")) {
                            style = 1;
                        } else if (cStyle.equals("one-colored") || cStyle.equals("oc")) {
                            style = 2;
                        } else if (cStyle.equals("one-lettered") || cStyle.equals("ol")) {
                            style = 3;
                        } else if (cStyle.equals("simple") || cStyle.equals("s")) {
                            style = 4;
                        } else if (styleToggle) {
                            style = (style + 1) % 5;
                        } else if (!cStyle.equals("somestring")) {
                            printError("the style " + cStyle + " doesn't exist");
                        }
                        if (style == 2) {
                            ANSI_ERROR = ANSI_INPUT;
                            ANSI_HELP = ANSI_INPUT;
                            ANSI_OUTPUT = ANSI_INPUT;
                        } else {
                            ANSI_ERROR = "\u001B[91m";
                            ANSI_HELP = "\u001B[92m";
                            ANSI_OUTPUT = "\u001B[94m";
                        }
                    } else {
                        String defaultConfigFileName = "config.properties";
                        EditCertificate ec = new EditCertificate();
                        TextEncodingDecoding tc = new TextEncodingDecoding();
                        Properties dProps = new Properties();

                        if (!configFile.equals("default")) {
                            try {
                                dProps = readProperties(configFile, true, false);
                            } catch (IOException ioe) {
                                printError("no file could be found at " + defaultConfigFileName);
                                printInfo("the name of the config file must be config.properties");
                                printInfo("using default config.properties file");
                            }
                        } else {
                            try {
                                dProps = readProperties(defaultConfigFileName, true, true);
                            } catch (Exception e) {
                                printError("default config file couldn't be found");
                                printInfo("using default configs");
                            }
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
                                startWriter(ec, dIssuerName, dSubjectName, dStDate, dExDate, dKeys, dSerNumber, dSignAlg, main);
                        } else if (readC) {
                            if (fileName == null)
                                main.printError("you have to enter a file name with the argument --file <filename>");
                            else
                                startReader(ec, main);
                        } else if (et) {
                            certDirectory = defaultString(certDirectory, main.pFile);
                            docDirectory = defaultString(docDirectory, main.pFile);
                            if (fileName == null || certFileName == null)
                                main.printError("you have to enter a file name with the argument --file <filename> and a certificate file name with the argument --certFile <file of the certificate>");
                            else
                                tc.main(certDirectory, fileName, certFileName, docDirectory, 0, main);

                        } else if (dt) {
                            docDirectory = defaultString(docDirectory, main.pFile);
                            if (fileName == null || certFileName == null)
                                main.printError("you have to enter a file name with the argument --file <filename> and a certificate file name with the argument --certFile <file of the certificate>");
                            else
                                tc.main(main.pFile, fileName, certFileName, docDirectory, 1, main);
                        } else if (setConfig) {
                            if (directoryName == null)
                                main.printError("you have to enter a directory path where your want the new config file to be with the argument --directory <filename>");
                            else
                                setAndCopyConfig(directoryName + "/config.properties", copyConfig);
                        }
                    }
                }
            }
        }
    }

    /**
     * Prints out the help for the command.
     *
     * @param x tells the function the help of what command it should print<br>
     *          (0 = writeCertificate, 1 = readCertificate, 2 = exit, 3 = readDocument, 4 = cd, 5 = writeDocument, 6 = general help, 7 = encodeDocument, 8 =
     *          decodeDocument, 9 = setConfig)
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
            printHelp("\t\t--read\t\t\t[enables read]");
            printHelp("\t\t--certTargetDir\t\t<the target directory of the certificate>");
        } else if (x == 1) {
            printHelp("readCertificate | rc \t\t[reads a certificate]");
            printHelp("\t\t--file\t\t\t<name of the file to read>");
        } else if (x == 2) {
            printHelp("exit\t\t\t\t[exits the console]");
        } else if (x == 3) {
            if (style < 3) {
                printHelp("readDocument | rd\t\t[reads a *.txt file with your file name]");
            } else {
                printHelp("readDocument | rd\t\t\t[reads a *.txt file with your file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to read>");
        } else if (x == 4) {
            printHelp("changeDirectory | cd\t\t[gives you the option to select another directory]");
        } else if (x == 5) {
            if (style < 3) {
                printHelp("writeDocument | wd\t\t[writes a *.txt file to you file name]");
            } else {
                printHelp("writeDocument | wd\t\t\t[writes a *.txt file to you file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to write>");
            printHelp("\t\t--lines\t\t\t<amount of lines you want to write>");
        } else if (x == 6) {
            printHelp("writeCertificate | wc\t\t[generates a certificate]");
            printHelp("readCertificate | rc\t\t[reads a certificate]");
            if (style < 3) {
                printHelp("writeDocument | wd\t\t[writes a *.txt file to you file name]");
                printHelp("readDocument | rd\t\t[reads a *.txt file with your file name]");
                printHelp("encodeDocument | ed\t\t[encodes a *.txt file at your file name]");
                printHelp("decodeDocument | dd\t\t[decodes a *.txt file at your file name]");
            } else {
                printHelp("writeDocument | wd\t\t\t[writes a *.txt file to you file name]");
                printHelp("readDocument | rd\t\t\t[reads a *.txt file with your file name]");
                printHelp("encodeDocument | ed\t\t\t[encodes a *.txt file at your file name]");
                printHelp("decodeDocument | dd\t\t\t[decodes a *.txt file at your file name]");
            }
            printHelp("setConfig | sc \t\t\t[changes the position of the config file]");
            printHelp("changeDirectory | cd\t\t[gives you the option to select another directory]");
            printHelp("changeStyle | cs\t\t\t[changes between colored and non-colored mode]");
            printHelp("exit\t\t\t\t[exits the console]");
            printHelp("");
            printHelp("Use <command> -h | --help to get further information about the command and the parameters you can apply to it");
        } else if (x == 7) {
            if (style < 3) {
                printHelp("encodeDocument | ed\t\t[encodes a *.txt file at your file name]");
            } else {
                printHelp("encodeDocument | ed\t\t\t[encodes a *.txt file at your file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to encode>");
            printHelp("\t\t--certFile\t\t<name of the certificate file (The name you entered for the writeCertificate command)>");
            printHelp("\t\t--certDirectory\t\t<directory of the certificate>");
            printHelp("\t\t--docDirectory\t\t<directory of the document>");
        } else if (x == 8) {
            if (style < 3) {
                printHelp("decodeDocument | dd\t\t[decodes a *.txt file at your file name]");
            } else {
                printHelp("decodeDocument | dd\t\t\t[decodes a *.txt file at your file name]");
            }
            printHelp("\t\t--file\t\t\t<name of the file to decode>");
            printHelp("\t\t--certFile\t\t<name of the certificate file (The name you entered for the writeCertificate command)>");
            printHelp("\t\t--docDirectory\t\t<directory of the document>");
        } else if (x == 9) {
            printHelp("setConfig | sc\t\t\t[changes the position of the config file]");
            printHelp("\t\t--directory\t\t<name of the new directory of the config.properties file>");
            printHelp("\t\t--copyConfig\t\t[copies the default config file to your selected location]");
        } else if (x == 10) {
            printHelp("changeStyle | cs\t\t\t[changes between colored and non-colored mode]");
            printHelp("\t\t--toggle\t\t[toggles between the different styles");
            printHelp("\t\t--style\t\t\t<the style you want to select");
            printHelp("\t\tavailable styles:");
            printHelp("\t\t\tdefault | d");
            printHelp("\t\t\tnon-colored | nc");
            printHelp("\t\t\tone-colored | oc");
            printHelp("\t\t\tone-lettered | ol");
            printHelp("\t\t\tsimple | s");
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
    public long defaultLong(long in, long d, int val) {
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
    public Date defaultDate(String in, Date d) {
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
    public Date stringToDate(String i) {
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
    private void startWriter(EditCertificate rc, String dIssuerName, String dSubjectName, Date dStDate, Date dExDate, int dKeys, long dSerNumber, String dSignAlg, Main main) {
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

        String pathFile = defaultString(certTargetDirectory, main.pFile);

        try {
            rc.write(pathFile + "/" + fileName, main.pFile + "/" + fileName, "CN = " + iName, "CN = " + sName, keypair, serNumber, stDate, exDate, signAlg, false, main);
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
                rc.printCertDataToConsole(rc.read(pathFile + "/" + fileName, main), main);
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
     * @param rc   readCertificate class object that's needed to start the Certificate reader in the that Class
     * @param main main class object
     */
    private void startReader(EditCertificate rc, Main main) {
        try {
            rc.printCertDataToConsole(rc.read(main.pFile + "/" + fileName, main), main);
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
    public Properties readProperties(String configFileName, boolean printMsg, boolean defaultPath) throws IOException {
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
                if (printMsg)
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
                    throw new IOException("");
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
        et = false;
        dt = false;
        setConfig = false;
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
        certFileName = null;
        lines = 0;
        copyConfig = false;
        certTargetDirectory = null;
        cs = false;
        certDirectory = null;
        docDirectory = null;
        cStyle = null;
        styleToggle = false;
    }

    /**
     * Prints the message with a blue [INFO] in front of it
     *
     * @param msg the message after the [INFO]
     */
    public void printInfo(String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_OUTPUT + "INFO" + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[INFO] " + msg);
        else if (style == 3)
            System.out.println("[" + ANSI_OUTPUT + "I" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[" + ANSI_OUTPUT + "+" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a red [ERROR] in front of it
     *
     * @param msg the message after the [ERROR]
     */
    public void printError(String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_ERROR + "ERROR" + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[ERROR] " + msg);
        else if (style == 3)
            System.out.println("[" + ANSI_ERROR + "E" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[" + ANSI_ERROR + "-" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a green [HELP] in front of it
     *
     * @param msg the message after the [HELP]
     */
    public void printHelp(String msg) {
        if (style == 0 || style == 2)
            System.out.println("[" + ANSI_HELP + "HELP" + ANSI_RESET + "] " + msg);
        else if (style == 1)
            System.out.println("[HELP] " + msg);
        else if (style == 3)
            System.out.println("[" + ANSI_HELP + "H" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[" + ANSI_HELP + "*" + ANSI_RESET + "] " + msg);
    }

    /**
     * Prints the message with a blue [-] in front of it
     *
     * @param msg the message after the [-]
     */
    public void printCertData(String msg) {
        if (style != 1)
            System.out.println("[" + ANSI_OUTPUT + "-" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[-] " + msg);
    }

    /**
     * Prints the message with a red [-] in front of it
     *
     * @param msg the message after the [-]
     */
    public void printRedCertData(String msg) {
        if (style != 1)
            System.out.println("[" + ANSI_ERROR + "-" + ANSI_RESET + "] " + msg);
        else
            System.out.println("[-] " + msg);
    }

    /**
     * Prints the message with a blue [number of the lines] in front of it for example [25]
     *
     * @param msg the message after the [number of the lines]
     * @param i   the number inside the []
     * @param max the maximum Number of the list (important for the amount of " " in front of the number)
     */
    public void printDocumentData(String msg, int i, int max) {
        if (style != 1) {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.println("[" + ANSI_OUTPUT + "   " + i + ANSI_RESET + "] " + msg);
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.println("[" + ANSI_OUTPUT + "  " + i + ANSI_RESET + "] " + msg);
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1) || (max / 10. > 1 && i / 10. < 1)) {
                System.out.println("[" + ANSI_OUTPUT + " " + i + ANSI_RESET + "] " + msg);
            } else {
                System.out.println("[" + ANSI_OUTPUT + i + ANSI_RESET + "] " + msg);
            }
        } else {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.println("[   " + i + "] " + msg);
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.println("[  " + i + "] " + msg);
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1) || (max / 10. > 1 && i / 10. < 1)) {
                System.out.println("[ " + i + "] " + msg);
            } else {
                System.out.println("[" + i + "] " + msg);
            }
        }
    }

    /**
     * Prints a yellow [J-CONSOLE> in front of the message and the msg after it also in yellow
     *
     * @param msg the message in yellow after the [J-CONSOLE>
     */
    public void printEditor(String msg) {
        if (style == 0 || style == 2)
            System.out.print("[" + ANSI_INPUT + "J-CONSOLE" + ANSI_RESET + "> " + ANSI_INPUT + msg + ANSI_RESET + "> ");
        else if (style == 1)
            System.out.print("[J-CONSOLE> " + msg + "> ");
        else {
            String[] msgList = msg.split("/");
            if (msgList.length < 3) {
                System.out.print("[" + ANSI_INPUT + msg + ANSI_RESET + "> ");
            } else {
                System.out.print("[" + ANSI_INPUT + ".../" + msgList[msgList.length - 1] + ANSI_RESET + "> ");
            }
        }
    }

    /**
     * Prints a blue [number of the lines] for example [25]
     *
     * @param i   the number inside the []
     * @param max the maximum Number of the list (important for the amount of " " in front of the number)
     */
    public void printEditorInput(int i, int max) {
        if (style != 1) {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.print("[" + ANSI_INPUT + "   " + i + ANSI_RESET + "] ");
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.print("[" + ANSI_INPUT + "  " + i + ANSI_RESET + "] ");
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1) || (max / 10. > 1 && i / 10. < 1)) {
                System.out.print("[" + ANSI_INPUT + " " + i + ANSI_RESET + "] ");
            } else {
                System.out.print("[" + ANSI_INPUT + i + ANSI_RESET + "] ");
            }
        } else {
            if (max / 1000. > 1 && i / 10. < 1) {
                System.out.print("[   " + i + "] ");
            } else if ((max / 1000. > 1 && i / 100. < 1) || (max / 100. > 1 && i / 10. < 1)) {
                System.out.print("[  " + i + "] ");
            } else if ((max / 1000. > 1 && i / 1000. < 1) || (max / 100. > 1 && i / 100. < 1) || (max / 10. > 1 && i / 10. < 1)) {
                System.out.print("[ " + i + "] ");
            } else {
                System.out.print("[" + i + "] ");
            }
        }
    }

    /**
     * This function sets the public variable configFile to the String file. If copy is true it also copies the config.properties file to the
     * String file.
     *
     * @param file The file that should be set as config file
     * @param copy if the program should copy the config.properties file to the String file
     */
    public void setAndCopyConfig(String file, boolean copy) {
        Properties prop;
        if (copy) {
            try {
                prop = readProperties("config.properties", false, true);
                prop.store(new FileOutputStream(file), "#'default' can be used for: defaultExDate, defaultSerialNumber, defaultValidity, defaultStDate");
                printInfo("successfully copied the config.properties file");
            } catch (IOException ioe) {
                printError("default config file couldn't be found");
                printError("copy failed");
            }
        }
        configFile = file;
    }
}