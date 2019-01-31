package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.security.cert.CertificateException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.util.*;

public class Main {
    public final String ANSI_RESET = "\u001B[0m";
    public final String ANSI_RED = "\u001B[91m";
    public final String ANSI_GREEN = "\u001B[92m";
    public final String ANSI_YELLOW = "\u001B[93m";
    public final String ANSI_BLUE = "\u001B[94m";

    @Parameter(names = {"generate", "gen"}, description = "generate a new certificate", help = true)
    public boolean gen;
    @Parameter(names = "read", description = "read a certificate", help = true)
    public boolean read;
    @Parameter(names = "help", description = "prints out a general help", help = true)
    public boolean gHelp;
    @Parameter(names = "editor", description = "changes to J-Editor", help = true)
    public boolean editor;
    @Parameter(names = "exit", description = "exits J-Console/J-Editor", help = true)
    public static boolean exit;
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
    public String certName;
    @Parameter(names = {"--signatureAlgorithm", "signAlg"}, description = "set signature algorithm")
    public String signAlg;
    @Parameter(names = "--read", description = "decide if you want to read the certificate after generating it")
    public boolean bRead;
    @Parameter(names = {"--pathFile", "--pFile"}, description = "set the path file")
    public String pFile;
    @Parameter(names = {"--help", "-h"}, description = "prints out a help for the command entered before")
    public boolean help;
    @Parameter(names = "--config", description = "set a config path file")
    public String cFile;

    public void startConsole() {
        Main main = new Main();
        Scanner sc = new Scanner(System.in);
        String in;
        JCommander jc;
        String[] sin; //args.clone();
        exit = false;
        while (!exit) {
            printConsole();
            in = sc.nextLine();
            sin = in.split(" ");
            if (sin.length != 0 && !in.equals("")) {
                try {
                    jc = JCommander.newBuilder().addObject(main).build();
                    jc.parse(sin);
                    main.run(main);
                    main.setToDefault();
                } catch (com.beust.jcommander.ParameterException pe) {
                    printError("unknown command or parameters");
                }
            }
        }
        printInfo("exiting ...");
        sc.close();

    }

    /**
     * The function run() uses the information of the JController and starts the write() funktion with it.
     * All values that don't get set by the JController get set to their defaut value.
     * Within the function also the stringToDate() function gets called to convert the Strings collected for the start
     * and the expiry date JController command to a Date variable.
     * If the user used the command -read it will also call the function read().
     *
     * @throws Exception Needed if one of the next functions throws an Exeption
     */
    private void run(Main main) {
        if (editor) {
            EditDocument ed = new EditDocument();
            ed.startEditor();
            main.setToDefault();
        } else {
            if (!exit || (exit && help)) {
                if (help || gHelp) {
                    if (gen || gHelp) {
                        printHelpToConsole(0);
                    }
                    if (read || gHelp) {
                        printHelpToConsole(1);
                    }
                    if (exit || gHelp) {
                        printHelpToConsole(2);
                        exit = false;
                    }
                } else {
                    String defaultConfigFileName = "config.properties";
                    cFile = defaultString(cFile, "");
                    ReadCertificate rc = new ReadCertificate();
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
                    String dCertName = dProps.getProperty("defaultFileName", "generated_certificate");
                    String dPathFile = dProps.getProperty("defaultPathFile", "src/main/resources");
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

                    if (!help && gen)
                        startGenerator(rc, dIssuerName, dSubjectName, dStDate, dExDate, dKeys, dSerNumber, dCertName, dSignAlg, dPathFile);
                    if (read && !help)
                        startReader(rc, dPathFile, dCertName);
                }
            }
        }
    }

    /**
     * Prints out the help for the command.
     *
     * @param x Tells the function the help of what command it should print<br>
     *          (0 = -generate, 1 = -read)
     */
    private void printHelpToConsole(int x) {
        if (x == 0) {
            printHelp("generate\t\t\t[generates a certificate]");
            printHelp("\t   --issuerName\t\t<CA-name>");
            printHelp("\t   --subjectName\t<owner-name>");
            printHelp("\t   --startDate\t\t<start date of the certificate>");
            printHelp("\t   --expiryDate\t\t<expiry date of the certificate>");
            printHelp("\t   --keySize\t\t<size of the public key in bits>");
            printHelp("\t   --serialNumber\t<serial number of the certificate>");
            printHelp("\t   --signatureAlgorithm\t<signature algorithm>");
            printHelp("\t   --file\t\t<name of the generated certificate>");
            printHelp("\t   --pathFile\t\t<set the pathfile of the certificate>");
            printHelp("\t   --config\t\t<set the pathfile of the config.properties file you want to use>");
            printHelp("\t   --read\t\t[enables read]");
        } else if (x == 1) {
            printHelp("read \t\t\t[reads a certificate]");
            printHelp("\t   --file\t\t<name of the file to read>");
            printHelp("\t   --pathFile\t\t<set the pathfile of the certificate to read>");
        } else if (x == 2) {
            printHelp("exit \t\t\t[exits the console]");
        }
    }

    /**
     * Tests if the String in is set.
     *
     * @param in The input String
     * @param d  The default value for the input String
     * @return If in != null -> in <br>
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
     * @param in  The input String
     * @param d   The default value for the input String
     * @param val The test value
     * @return If in > val -> in<br>
     * If in <= val -> d
     */
    int defaultInt(int in, int d, int val) {
        if (in < val) {
            in = d;
        }
        return in;
    }

    /**
     * Tests if the int in is bigger than the int val.
     *
     * @param in  The input String
     * @param d   The default value for the input String
     * @param val The test value
     * @return If in > val -> in<br>
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
     * @param in The input String
     * @param d  The default value for the input String
     * @return If in != null -> in <br>
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
     * @param i String in DD-MM-YYYY format
     * @return Date in Date format as output
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
     * @param rc         ReadCertificate class object that's needed to start the Certificate generator in the that Class
     * @param dStDate    Default start date the Certificate generator should use if it isn't set
     * @param dExDate    Default expiry date the Certificate generator should use if it isn't set
     * @param dKeys      Default key size the Certificate generator should use if it isn't set
     * @param dSerNumber Default serial number the Certificate generator should use if it isn't set
     * @param dCertName  Default certificate filename the Certificate generator should use if it isn't set
     * @param dSignAlg   Default signature algorithm the Certificate generator should use if it isn't set
     * @param dPathFile  Default path file the Certificate generator should use if it isn't set
     * @throws Exception If the Generator throws an Exception
     */
    private void startGenerator(ReadCertificate rc, String dIssuerName, String dSubjectName, Date dStDate, Date dExDate, int dKeys, long dSerNumber, String dCertName, String dSignAlg, String dPathFile) {
        printInfo("checking inputs");
        iName = defaultString(iName, dIssuerName);
        sName = defaultString(sName, dSubjectName);
        Date stDate = defaultDate(sDate, dStDate);
        Date exDate = defaultDate(eDate, dExDate);
        keys = defaultInt(keys, dKeys, 512);
        serNumber = defaultLong(serNumber, dSerNumber, 1);
        certName = defaultString(certName, dCertName);
        signAlg = defaultString(signAlg, dSignAlg);
        pFile = defaultString(pFile, dPathFile);

        pFile = pFile + "/";

        printInfo("generating key pair");

        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(keys);
        KeyPair keypair = keyGen.generateKeyPair();

        File file = new File(pFile + certName + ".crt");

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
     * @param rc        ReadCertificate class object that's needed to start the Certificate reader in the that Class
     * @param dPathFile Default path file the Certificate reader should use if it isn't set
     * @param dCertName Default name of the Certificate
     * @throws IOException          If the Reader throws an IOException
     * @throws CertificateException If the Reader throws a Certificate Exception
     */
    private void startReader(ReadCertificate rc, String dPathFile, String dCertName) {
        certName = defaultString(certName, dCertName);
        pFile = defaultString(pFile, dPathFile);
        File file = new File(pFile + "/" + certName + ".crt");
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

    private void setToDefault() {
        gen = false;
        read = false;
        gHelp = false;
        editor = false;
        //-------+
        iName = null;
        sName = null;
        sDate = null;
        eDate = null;
        keys = 0;
        serNumber = 0L;
        certName = null;
        signAlg = null;
        bRead = false;
        pFile = null;
        help = false;
        cFile = null;
    }

    public void printInfo(String msg) {
        System.out.println("[" + ANSI_BLUE + "INFO" + ANSI_RESET + "] " + msg);
    }

    public void printError(String msg) {
        System.out.println("[" + ANSI_RED + "ERROR" + ANSI_RESET + "] " + msg);
    }

    public void printConsole() {
        System.out.print("[" + ANSI_YELLOW + "J-CONSOLE" + ANSI_RESET + "> ");
    }

    public void printHelp(String msg) {
        System.out.println("[" + ANSI_GREEN + "HELP" + ANSI_RESET + "] " + msg);
    }

    public void printCertData(String msg) {
        System.out.println("[" + ANSI_BLUE + "-" + ANSI_RESET + "] " + msg);
    }

    public void printDocumentData(String msg, int i, int max) {
        if(max/1000. > 1 && i/10. < 1) {
            System.out.println("[" + ANSI_BLUE + "   " + i +ANSI_RESET + "] " + msg);
        } else if((max/1000. > 1 && i/100. < 1) || (max/100. > 1 && i/10. < 1)) {
            System.out.println("[" + ANSI_BLUE + "  " + i + ANSI_RESET + "] " + msg);
        } else if((max/1000. > 1 && i/1000. < 1) || (max/100. > 1 && i/100. < 1) || (max/10. > 1 && i/10. < 1)) {
            System.out.println("[" + ANSI_BLUE + " " + i +ANSI_RESET + "] " + msg);
        } else {
            System.out.println("[" + ANSI_BLUE +i + ANSI_RESET + "] " + msg);
        }
    }

    public void printRedCertData(String msg) {
        System.out.println("[" + ANSI_RED + "-" + ANSI_RESET + "] " + msg);
    }

    public void printEditor(String msg) {
        System.out.print("[" + ANSI_YELLOW + "J-CONSOLE" + ANSI_RESET + ">" +ANSI_YELLOW + " Editor" + ANSI_RESET + "> " + ANSI_YELLOW + msg + ANSI_RESET + "> ");
    }

    public void printEditorInput(int i, int max) {
        if(max/1000. > 1 && i/10. < 1) {
            System.out.print("[" + ANSI_YELLOW + "   " + i +ANSI_RESET + "] ");
        } else if((max/1000. > 1 && i/100. < 1) || (max/100. > 1 && i/10. < 1)) {
            System.out.print("[" + ANSI_YELLOW + "  " + i + ANSI_RESET + "] ");
        } else if((max/1000. > 1 && i/1000. < 1) || (max/100. > 1 && i/100. < 1) || (max/10. > 1 && i/10. < 1)) {
            System.out.print("[" + ANSI_YELLOW + " " + i +ANSI_RESET + "] ");
        } else {
            System.out.print("[" + ANSI_YELLOW +i + ANSI_RESET + "] ");
        }
    }
}