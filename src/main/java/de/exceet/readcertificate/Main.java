package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.security.cert.CertificateException;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.GregorianCalendar;

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
    @Parameter(names = {"--serialNumber", "--serNumber"}, description = "set a serial number")
    public long serNumber;
    @Parameter(names = "--file", description = "set the certificate name")
    public String certName;
    @Parameter(names = {"--signatureAlgorithm", "signAlg"}, description = "set signature algorithm")
    public String signAlg;
    @Parameter(names = "--read", description = "decide if you want to read the certificate after generating it")
    public boolean bRead;
    @Parameter(names = {"--pathfile", "--pfile"}, description = "set the pathfile")
    public String pFile;
    @Parameter(names = "--help", description = "prints out a help for the command entered before")
    public boolean help;


    /**
     * Main function starts the JController and calls the function run().
     *
     * @param argv
     * @throws Exception Needed if one of the next functions throws an Exeption
     */
    public static void main(String[] argv) throws Exception {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(argv);
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
        // TODO create and read config file
        ReadCertificate rc = new ReadCertificate();
        Date dExDate = new Date();
        long milSecValid = 31536000000L;                                    // default time in milliseconds the certificate is valid after generating it
        String dPathFile = "C:/Users/jvansprang/Desktop/Certificates";     // default pathfile
        String dCertName = "mycertificate";                                 // default certificate name
        Date dStDate = new Date();                                          // default startdate
        dExDate.setTime(dExDate.getTime() + milSecValid);                   // default expriydate
        long dSerNumber = new Date().getTime();                             // default serial number
        int dKeys = 4096;                                                   // default keysize
        String dSignAlg = "SHA256withRSA";                                  // default signature algorithm

        Date stDate, exDate;
        if (help || gHelp) {
            if (gen || gHelp) {
                printHelp(0);
            }
            if (read || gHelp) {
                printHelp(1);
            }
        }
        if (help != true && gen == true) {
            startGenerator(rc, dStDate, dExDate, dKeys, dSerNumber, dCertName, dSignAlg, dPathFile);
        }
        if (read && help != true) {
            startReader(rc, dPathFile);
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
            System.exit(1);
        }
        Date o = new GregorianCalendar(y, m - 1, d).getTime();
        // out.println(o);
        return o;
    }

    /**
     * Starts the Certificate generator. Needs all default values for the Certificate parameters.
     * @param rc ReadCertificate class object that's needed to start the Certificate generator in the that Class
     * @param dStDate Default start date the Certificate generator should use if it isn't set
     * @param dExDate Default expiry date the Certificate generator should use if it isn't set
     * @param dKeys Default key size the Certificate generator should use if it isn't set
     * @param dSerNumber Default serial number the Certificate generator should use if it isn't set
     * @param dCertName Default certificate filename the Certificate generator should use if it isn't set
     * @param dSignAlg Default signature algorithm the Certificate generator should use if it isn't set
     * @param dPathFile Default path file the Certificate generator should use if it isn't set
     * @throws Exception If the Generator throws an Exception
     */
    public void startGenerator(ReadCertificate rc, Date dStDate, Date dExDate, int dKeys, long dSerNumber, String dCertName, String dSignAlg, String dPathFile) throws Exception {
        if (iName == null || sName == null) {
            System.out.println("Issuer or subject Name missing (both are necessary to generate the certificate");
            System.exit(1);
        } else {
            Date stDate = defaultDate(sDate, dStDate);
            Date exDate = defaultDate(eDate, dExDate);
            keys = defaultInt(keys, dKeys, 512);
            serNumber = defaultLong(serNumber, dSerNumber, 0);
            certName = defaultString(certName, dCertName);
            signAlg = defaultString(signAlg, dSignAlg);
            pFile = defaultString(pFile, dPathFile);

            pFile = pFile + "/";


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
    }

    /**
     * Starts the Certificate reader. Needs all default values for the reading parameters.
     * @param rc ReadCertificate class object that's needed to start the Certificate reader in the that Class
     * @param dPathFile Default path file the Certificate reader should use if it isn't set
     * @throws IOException If the Reader throws an IOException
     * @throws CertificateException If the Reader throws a Certificate Exception
     */
    public void startReader(ReadCertificate rc, String dPathFile) throws IOException, CertificateException {
        if (certName == null) {
            System.out.println("The filename is missing (is necessary to read the certificate");
            System.exit(1);
        } else {
            if (pFile == null) {
                pFile = dPathFile;
            }
            File file = new File(pFile + certName + ".crt");
            rc.printCertDataToConsole(rc.read(file));
        }
    }
}