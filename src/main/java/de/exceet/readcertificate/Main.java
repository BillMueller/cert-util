package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.security.cert.CertificateException;
import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    @Parameter(names = {"-generate", "-g"}, description = "generate a new certificate", help = true)
    public boolean gen;
    @Parameter(names = {"-read", "-r"}, description = "read a certificate", help = true)
    public boolean read;
    @Parameter(names = {"-help", "-h"}, description = "prints out a help for the command entered before", help = true)
    public boolean gHelp;
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
    @Parameter(names = "--help", description = "prints out a help for the command entered before")
    public boolean help;
    @Parameter(names = "--config", description = "set a config path file")
    public String cFile;


    /**
     * Main function starts the JController and calls the function run().
     *
     * @param args Arguments given when running the main function
     * @throws Exception Needed if one of the next functions throws an Exeption
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        //TestJCom tjc = new TestJCom();
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        jc.parse(args);
        //tjc.printout();
        // Files.copy(Paths.get("meineDatei"));
        main.run();
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
    public void run() throws Exception {
        if (help || gHelp) {
            if (gen || gHelp) {
                printHelp(0);
            }
            if (read || gHelp) {
                printHelp(1);
            }
        } else {
            String defaultConfigFileName = "config.properties";
            cFile = defaultString(cFile, "");
            ReadCertificate rc = new ReadCertificate();

            Properties dProps = readProperties(cFile + "/" + defaultConfigFileName, true, cFile.equals(""));

            String dIssuerName = dProps.getProperty("defaultIssuerName", "ca_name");
            String dSubjectName = dProps.getProperty("defaultSubjectName", "owner_name");
            int dKeys = Integer.valueOf(dProps.getProperty("defaultHeySize", "4096"));
            String dPropsSerNumber = dProps.getProperty("defaultSerialNumber", "default");
            String dPropsStDate = dProps.getProperty("defaultStDate", "default");
            String dPropsExDate = dProps.getProperty("defaultExDate", "default");
            String dPropsValidity = dProps.getProperty("defaultValidity", "default");
            String dCertName = dProps.getProperty("defaultCertificateFileName", "generated_certificate");
            String dPathFile = dProps.getProperty("defaultPathFile", "src/main/resources");
            String dSignAlg = dProps.getProperty("defaultSignatureAlgorithm", "SHA256withRSA");

            Date dStDate, dExDate = new Date();
            long milSecValid = 31536000000L, dSerNumber;

            if (dPropsStDate.equals("default")) {
                dStDate = new Date();
            } else {
                dStDate = stringToDate(dPropsStDate);
            }
            if (!dPropsValidity.equals("default")) {
                milSecValid = Long.valueOf(dPropsValidity);
            }
            if (dPropsExDate.equals("default")) {
                dExDate.setTime(dStDate.getTime() + milSecValid);
            } else {
                dExDate = stringToDate(dPropsExDate);
            }
            if (dPropsSerNumber.equals("default")) {
                dSerNumber = new Date().getTime();
            } else {
                dSerNumber = Long.valueOf(dPropsSerNumber);
            }
            if (!help && gen) {
                startGenerator(rc, dIssuerName, dSubjectName, dStDate, dExDate, dKeys, dSerNumber, dCertName, dSignAlg, dPathFile);
            }
            if (read && !help) {
                startReader(rc, dPathFile, dCertName);
            }
        }

    }

    /**
     * Prints out the help for the command.
     *
     * @param x Tells the function the help of what command it should print<br>
     *          (0 = -generate, 1 = -read)
     */
    public void printHelp(int x) {
        if (x == 0) {
            System.out.println("-generate --iName <CA-name> --sName <owner-name>\t[generates a certificate]");
            System.out.println("\t--startDate\t\t\t\t<start date of the certificate>");
            System.out.println("\t--expiryDate\t\t\t<expiry date of the certificate>");
            System.out.println("\t--keySize\t\t\t\t<size of the public key in bits>");
            System.out.println("\t--serialNumber\t\t\t<serial number of the certificate>");
            System.out.println("\t--signatureAlgorithm\t<signature algorithm>");
            System.out.println("\t--file\t\t\t\t\t<name of the generated certificate>");
            System.out.println("\t--pathfile\t\t\t\t<set the pathfile of the certificate>");
            System.out.println("\t--read");
        } else if (x == 1) {
            System.out.println("-read --file <name of the file to read>\t[reads a certificate]");
            System.out.println("\t--pathfile\t\t\t\t<set the pathfile of the certificate to read>");
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
    public int defaultInt(int in, int d, int val) {
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
    public long defaultLong(long in, long d, int val) {
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
     * @param i String in DD-MM-YYYY format
     * @return Date in Date format as output
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
    public void startGenerator(ReadCertificate rc, String dIssuerName, String dSubjectName, Date dStDate, Date dExDate, int dKeys, long dSerNumber, String dCertName, String dSignAlg, String dPathFile) throws Exception {
        System.out.println("[INFO] checking inputs");
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

        System.out.println("[INFO] generating key pair");

        //-----+
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");        // select the altgorithm type
        keyGen.initialize(keys);                                              // the keysize
        KeyPair keypair = keyGen.generateKeyPair();                           // generate the keypair
        //-----+

        File file = new File(pFile + certName + ".crt");

        rc.write(file, "CN = " + iName, "CN = " + sName, keypair, serNumber, stDate, exDate, signAlg);

        if (bRead) {
            rc.printCertDataToConsole(rc.read(file));
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
    public void startReader(ReadCertificate rc, String dPathFile, String dCertName) throws IOException, CertificateException {
        certName = defaultString(certName, dCertName);
        pFile = defaultString(pFile, dPathFile);
        File file = new File(pFile + "/" + certName + ".crt");
        rc.printCertDataToConsole(rc.read(file));
    }

    /**
     * Reads the config.properties file in the main project folder
     *
     * @return object of the type Properties (with object.getProperty(property) you can get the value for the property
     */
    public Properties readProperties(String configFileName, boolean printMsg, boolean defaultPath) {
        if (printMsg)
            if (defaultPath)
                System.out.println("[INFO] loading config.properties");
            else
                System.out.println("[INFO] loading " + configFileName);
        Properties prop = new Properties();

        if (defaultPath) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
                if (input != null)
                    prop.load(input);
                else
                    throw new IOException();

                System.out.println("[INFO] successfully loaded settings from " + configFileName);

            } catch (IOException ex) {
                printEx(printMsg, true, configFileName);
            }
        } else {
            try (InputStream input = new FileInputStream(configFileName)) {
                if (input != null)
                    prop.load(input);
                else
                    throw new IOException();

                System.out.println("[INFO] successfully loaded settings from " + configFileName);

            } catch (IOException ex) {
                if(printMsg)
                    ex.printStackTrace();
            }
        }
        return prop;
    }

    public void printEx(boolean p, boolean d, String fn) {
        if (p && !d) {
            System.out.println("[ERROR] no file could be found at " + fn);
            System.out.println("[WARNING] The name of the config file must be config.properties");
            System.out.println("[INFO] using default config.properties file");
        }
    }
}