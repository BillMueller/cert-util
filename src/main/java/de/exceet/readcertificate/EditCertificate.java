package de.exceet.readcertificate;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;

import javax.security.auth.x500.X500Principal;
import javax.security.cert.CertificateException;
import java.io.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditCertificate {

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
     */
    public void write(String file, String pKFile, String IssuerDnName, String SubjectDnName, KeyPair keyPair, long serNumber, Date startDate, Date expiryDate, String signatureAlgorithm, boolean test, Main main) throws CertificateEncodingException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        main.printInfo("starting certificate generator");

        X509Certificate cert = setCertificateData(serNumber, IssuerDnName, SubjectDnName, startDate, expiryDate, keyPair, signatureAlgorithm).generate(keyPair.getPrivate());

        writeCertificateToFile(cert, main, file);

        if (!test) {
            writePrivateKeyToFile(pKFile, keyPair);
        }
    }

    /**
     * The function read() reads a certificate out of the file that has given to it by the input (File file) and prints
     * them to the console.
     *
     * @param file File:       the file of the certificate that will be read
     * @throws IOException                              Needed if some of the inputs are wrong
     * @throws javax.security.cert.CertificateException Needed if some of the inputs are wrong
     */
    public List<String> read(String file, Main main) throws IOException, javax.security.cert.CertificateException {
        javax.security.cert.X509Certificate cert = getCertificate(main, file);

        return prepareOutput(cert, main);
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

    /**
     * Prints the every element of the input list to the console with a [-] in front of it
     *
     * @param input the list to be printed out
     */
    public void printCertDataToConsole(List<String> input, Main main) {
        main.printInfo(input.get(0));
        main.printCertData(input.get(1));
        main.printCertData(input.get(2));
        main.printCertData(input.get(3));
        main.printCertData(input.get(4));
        if (Integer.valueOf(input.get(6)) == 0) {
            main.printCertData(input.get(5));
        } else {
            main.printRedCertData(input.get(5));
        }
        main.printCertData(input.get(7));
        main.printCertData(input.get(8));
        main.printCertData(input.get(9));
    }

    /**
     * Sets certificate data to a new X509V1 certificate generator with the input of all data that needs to be set
     *
     * @param serNumber          serial number of the certificate to be generated
     * @param IssuerDnName       issuer name of the certificate to be generated
     * @param SubjectDnName      subject name of the certificate to be generated
     * @param startDate          start date of the certificate to be generated
     * @param expiryDate         expiry date of the certificate to be generated
     * @param keyPair            key pair of the certificate to be generated
     * @param signatureAlgorithm signature algorithm of the certificate to be generated
     * @return the certificate generator
     */
    private X509V1CertificateGenerator setCertificateData(long serNumber, String IssuerDnName, String SubjectDnName, Date startDate, Date expiryDate, KeyPair keyPair, String signatureAlgorithm) {
        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(serNumber));
        certGen.setIssuerDN(new X500Principal(IssuerDnName));
        certGen.setSubjectDN(new X500Principal(SubjectDnName));
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm(signatureAlgorithm);

        return certGen;
    }

    /**
     * Writes the certificate cert to the file with the directory "file"
     *
     * @param cert certificate to be written to the file
     * @param main main function object (needed for console prints)
     * @param file directory for the certificate to be written to
     * @throws IOException                  if the file couldn't be found
     * @throws CertificateEncodingException if the certificate couldn't be encoded
     */
    private void writeCertificateToFile(Certificate cert, Main main, String file) throws IOException, CertificateEncodingException {
        String output;
        BASE64Encoder encoder = new BASE64Encoder();
        output = X509Factory.BEGIN_CERT;
        output = output + "\n" + encoder.encodeBuffer(cert.getEncoded());
        output = output + X509Factory.END_CERT;

        main.printInfo("writing certificate to " + file + ".crt");

        FileWriter wr = new FileWriter(file + ".crt");
        wr.write(output);
        wr.flush();
        wr.close();
    }

    /**
     * Writes the Private key from the keyPair to the file PKFile with the ending _private_key
     *
     * @param pKFile  directory for the private key file to be written to
     * @param keyPair key Pair that contains the private key to be written
     * @throws IOException if the directory couldn't be found
     */
    private void writePrivateKeyToFile(String pKFile, KeyPair keyPair) throws IOException {
        FileUtils.writeByteArrayToFile(new File(pKFile + "_private_key"), keyPair.getPrivate().getEncoded());
    }

    /**
     * Gets the certificate from the directory "file"
     *
     * @param main main class object (needed to print to console)
     * @param file directory where the certificate is found
     * @return the certificate as X509 certificate
     * @throws CertificateException if the file ins't a valid certificate
     * @throws IOException          if the directory coudln't be found
     */
    private javax.security.cert.X509Certificate getCertificate(Main main, String file) throws CertificateException, IOException {
        main.printInfo("starting certificate reader");
        InputStream inStream = new FileInputStream(file + ".crt");

        javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);

        inStream.close();

        return cert;
    }

    /**
     * Prepares the output for the "printCertDataToConsole()" function by putting all data into a List
     *
     * @param cert certificate where the function gets the certificate data from
     * @param main main function (needed to print to console)
     * @return the List<String> of certificate data
     */
    private List<String> prepareOutput(javax.security.cert.X509Certificate cert, Main main) {
        List<String> output = new ArrayList<>();
        Date date1 = cert.getNotBefore();
        Date date2 = cert.getNotAfter();

        output.add(0, "Printing certificate data:");
        output.add(1, "Version: " + (cert.getVersion() + 1));
        output.add(2, "Serial Number: " + cert.getSerialNumber());
        output.add(3, "Issuer: " + cert.getIssuerDN());
        output.add(4, "Subject: " + cert.getSubjectDN());
        if (testDate(date1, date2)) {
            output.add(5, "Validity: " + date1 + " - " + date2 + " - The certificate is valid.");
            output.add(6, "0");
        } else {
            output.add(5, "Validity: " + date1 + " - " + date2 + " - The certificate is not valid.");
            output.add(6, "1");
        }
        output.add(7, "Subject Public Key Info: " + cert.getPublicKey());
        output.add(8, "Hash Code: " + cert.hashCode());
        output.add(9, "Signature algorithm: " + cert.getSigAlgName() + ". The algorithm type is " + cert.getPublicKey().getAlgorithm() + ".");

        main.printInfo("successfully read certificate");

        return output;
    }
}