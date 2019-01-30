package de.exceet.readcertificate;

import org.bouncycastle.x509.X509V1CertificateGenerator;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadCertificate {
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
    public void write(File file, String IssuerDnName, String SubjectDnName, KeyPair keyPair, long serNumber, Date startDate, Date expiryDate, String signatureAlgorithm) throws Exception {

        System.out.println("[INFO] starting certificate generator");

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
        System.out.println("[INFO] writing certificate to " + file);

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
    public List<String> read(File file) throws IOException, javax.security.cert.CertificateException {
        System.out.println("[INFO] starting certificate reader");
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
        boolean dateVal;
        List<String> output = new ArrayList<String>();

        // test if the certificate is still valid
        if (testDate(date1, date2)) {
            // if it is valid -> set dateshow to ""
            dateVal = true;
        } else {
            //if it is not valid -> set dateshow to "not "
            dateVal = false;
        }
        // print the version and get and print serial number, issuer, validity, subject, subject public key info,
        // hash code and the name of the signature algorithm

        output.add(0,"[INFO] Printing certificate data:");
        output.add(1,"Version: " + v);
        output.add(2,"Serial Number: " + cert.getSerialNumber());
        output.add(3,"Issuer: " + cert.getIssuerDN());
        output.add(4,"Subject: " + cert.getSubjectDN());
        if (dateVal) {
            output.add(5, "Validity: " + date1 + " - " + date2 + " - The certificate is valid.");
            output.add(6,"0");
        } else {
            output.add(5, "Validity: " + date1 + " - " + date2 + " - The certificate is not valid.");
            output.add(6,"1");
        }
        output.add(7,"Subject Public Key Info: " + cert.getPublicKey());
        output.add(8,"Hash Code: " + cert.hashCode());
        output.add(9,"Signature algorithm: " + algorithm + ". The algorithm type is " + cert.getPublicKey().getAlgorithm() + ".");

        System.out.println("[INFO] successfully read certificate");

        return output;
    }


    /**
     * function to test if the date (right now) is between date 1 and date 2.
     *
     * @param dt1 Date:   start date
     * @param dt2 Date:   end date
     * @return true, if the date right now is between the start and end date
     */
    public boolean testDate(Date dt1, Date dt2) {
        //tests if the date right now is between date 1 and date 2
        Date dtNow = new Date();
        if (dtNow.after(dt1) && dt2.after(dtNow)) {
            return true;
        } else {
            return false;
        }
    }

    public void printCertDataToConsole(List<String> input){
        System.out.println(input.get(0));
        System.out.println("[-] " + input.get(1));
        System.out.println("[-] " + input.get(2));
        System.out.println("[-] " + input.get(3));
        System.out.println("[-] " + input.get(4));
        if(Integer.valueOf(input.get(6)) == 0){
            System.out.println("[-] " + input.get(5));
        }else{
            System.err.println("[-] " + input.get(5));
        }
        System.out.println("[-] " + input.get(7));
        System.out.println("[-] " + input.get(8));
        System.out.println("[-] " + input.get(9));
    }
}