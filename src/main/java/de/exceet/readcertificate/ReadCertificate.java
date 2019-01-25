package de.exceet.readcertificate;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.GregorianCalendar;

class Main {
    @Parameter(names = {"-generate", "-g"}, description = "generate a new certificate", help = true)
    public boolean gen;
    @Parameter(names = {"-read", "-r"}, description = "read a certificate", help = true)
    public boolean read;
    @Parameter(names = {"-help","-h"}, description = "prints out a help for the command entered before", help = true)
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
                rc.read(file);
            }
        }

    }

}

public class ReadCertificate {
    /**
     * The function stringToDate() converts a String (i) into a date value. Important for that is, that the String got
     * the format DD-MM-YYYY. If that isn't the case it will close the program with System.exit(). If it works it will
     * return the date in "Date" format.
     *
     * @param i String: Input
     * @return Date:   Output
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
     * The function write() generates a certificate out of the inputs and saves it into the file (File file).
     *
     * @param file               File:       file where the certificate will be saved
     * @param IssuerDnName       String:     name of the CA that generates the certificate
     * @param SubjectDnName      String:     name of the owner of the certificate
     * @param keyPair            KeyPair:    the key pair with the private and public key
     * @param serNumber          int:        the serial number the certificate will have
     * @param startDate          Date:       the first day the certificate is valid
     * @param expiryDate         Date:       the last day the certificate is valid
     * @param signatureAlgorithm String:     the signatureAlgorithm thats used to sign the certificate
     * @throws Exception Needed if some of the inputs are wrong
     */
    public static void write(File file, String IssuerDnName, String SubjectDnName, KeyPair keyPair, long serNumber, Date startDate, Date expiryDate, String signatureAlgorithm) throws Exception {

        // define serial number, certificate generator and the issuer/subjectdn
        BigInteger serialNumber = BigInteger.valueOf(serNumber);
        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal idnName = new X500Principal(IssuerDnName);
        X500Principal sdnName = new X500Principal(SubjectDnName);

        // add all necessary informations to the certificate
        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(idnName);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);
        certGen.setSubjectDN(sdnName);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm(signatureAlgorithm);

        // generate the certificate and use the private key to decode
        X509Certificate cert = certGen.generate(keyPair.getPrivate());

        // convert the certificate to a pem certificate
        String output;
        BASE64Encoder encoder = new BASE64Encoder();
        output = X509Factory.BEGIN_CERT;
        output = output + "\n" + encoder.encodeBuffer(cert.getEncoded());
        output = output + X509Factory.END_CERT;

        // System.out.println(output);

        // write the String into the file ("mycertificate.crt")
        FileWriter wr = new FileWriter(file);
        wr.write(output);
        wr.flush();
        wr.close();
    }

    /**
     * The function read() reads a certificate out of the file that has given to it by the input (File file) and prints
     * them to the console.
     *
     * @param file File:       the file of the certificate that will be read
     * @throws IOException                              Needed if some of the inputs are wrong
     * @throws javax.security.cert.CertificateException Needed if some of the inputs are wrong
     */
    public static void read(File file) throws IOException, javax.security.cert.CertificateException {
        // define input stream to read the file
        InputStream inStream = new FileInputStream(file);

        // safe the certificate in "cert"
        javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);

        // close the input steam
        inStream.close();

        // get the version and add one to it (since the program start counting at 0 while usually 1 is used for the first version
        int v = cert.getVersion() + 1;

        // define the first and last date when the certificate can be used as date 1 and 2 and define a String
        Date date1 = cert.getNotBefore();
        Date date2 = cert.getNotAfter();
        String algorithm = cert.getSigAlgName();
        boolean dateval;

        // test if the certificate is still valid
        if (testDate(date1, date2)) {
            // if it is valid -> set dateshow to ""
            dateval = true;
        } else {
            //if it is not valid -> set dateshow to "not "
            dateval = false;
        }

        // print the version and get and print serial number, issuer, validity, subject, subject public key info,
        // hash code and the name of the signature algorithm
        System.out.println("---Zertifikat---");
        System.out.println("Version: " + v);
        System.out.println("Serial Number: " + cert.getSerialNumber());
        System.out.println("Issuer: " + cert.getIssuerDN());
        if (dateval) {
            System.out.println("Validity: " + date1 + " - " + date2 + " - The certificate is valid.");
        } else {
            System.err.println("Validity: " + date1 + " - " + date2 + " - The certificate is not valid.");
        }
        System.out.println("Subject: " + cert.getSubjectDN());
        System.out.println("Subject Public Key Info: " + cert.getPublicKey());
        System.out.println("Hash Code: " + cert.hashCode());
        System.out.println("Signature algorithm: " + algorithm + ". The algorithm type is " + cert.getPublicKey().getAlgorithm() + ".");
        System.out.println("---?--- " + cert.getSigAlgOID() + "\n"); // I dont know what that  command gets
    }


    /**
     * function to test if the date (right now) is between date 1 and date 2.
     *
     * @param dt1 Date:   start date
     * @param dt2 Date:   end date
     * @return true, if the date right now is between the start and end date
     */
    public static boolean testDate(Date dt1, Date dt2) {
        //tests if the date right now is between date 1 and date 2
        Date dtNow = new Date();
        if (dtNow.after(dt1) && dt2.after(dtNow)) {
            return true;
        } else {
            return false;
        }
    }
}