package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;

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
        ReadCertificate rc = new ReadCertificate();
        Date dExDate = new Date();
        long milSecValid = 31536000000L;                                    // default time in milliseconds the certificate is valid after generating it
        String dPathFile = "C:/Users/jvansprang/Desktop/Certificates/";     // default pathfile
        String dCertName = "mycertificate";                                 // default certificate name
        Date dStDate = new Date();                                          // default startdate
        dExDate.setTime(dExDate.getTime() + milSecValid);                   // default expriydate
        long dSerNumber = new Date().getTime();                             // default serial number
        int dKeys = 4096;                                                   // default keysize
        String dSignAlg = "SHA256withRSA";                                  // default signature algorithm

        Date stDate, exDate;
        if (help || gHelp) {
            if (gen || gHelp) {
                System.out.println("-generate --iName <CA-name> --sName <owner-name>\t[generates a certificate]");
                System.out.println("\t--startDate\t\t\t\t<start date of the certificate>");
                System.out.println("\t--expiryDate\t\t\t<expiry date of the certificate>");
                System.out.println("\t--keySize\t\t\t\t<size of the public key in bits>");
                System.out.println("\t--serialNumber\t\t\t<serial number of the certificate>");
                System.out.println("\t--signatureAlgorithm\t<signature algorithm>");
                System.out.println("\t--file\t\t\t\t\t<name of the generated certificate>");
                System.out.println("\t--pathfile\t\t\t\t<set the pathfile of the certificate>");
                System.out.println("\t--read");
            }
            if (read || gHelp) {
                System.out.println("-read --file <name of the file to read>\t[reads a certificate]");
                System.out.println("\t--pathfile\t\t\t\t<set the pathfile of the certificate to read>");
            }
        }
        if (help != true && gen == true) {
            if (iName == null || sName == null) {
                System.out.println("Issuer or subject Name missing (both are necessary to generate the certificate");
                System.exit(1);
            } else {
                if (sDate == null) {
                    stDate = dStDate;
                } else {
                    stDate = rc.stringToDate(sDate);
                }
                if (eDate == null) {
                    exDate = dExDate;
                } else {
                    exDate = rc.stringToDate(eDate);
                }
                if (keys < 512) {
                    keys = dKeys;
                }
                if (serNumber == 0) {
                    serNumber = dSerNumber;
                }
                if (certName == null) {
                    certName = dCertName;
                }
                if (signAlg == null) {
                    signAlg = dSignAlg;
                }
                if (pFile == null) {
                    pFile = dPathFile;
                } else {
                    pFile = pFile + "/";
                }

                //-----+
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");        // select the altgorithm type
                keyGen.initialize(keys);                                              // the keysize
                KeyPair keypair = keyGen.generateKeyPair();                           // generate the keypair
                //-----+

                File file = new File(pFile + certName + ".crt");

                rc.write(file, "CN = " + iName, "CN = " + sName, keypair, serNumber, stDate, exDate, signAlg);

                if (bRead) {
                    rc.read(file);
                }
            }
        }
        if (read && help != true) {
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

}