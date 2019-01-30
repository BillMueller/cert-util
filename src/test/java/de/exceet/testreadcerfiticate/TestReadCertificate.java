package de.exceet.testreadcerfiticate;

import de.exceet.readcertificate.Main;
import de.exceet.readcertificate.ReadCertificate;
import org.junit.Test;

import javax.security.cert.CertificateException;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TestReadCertificate {
    public static final String RESET = "\u001B[0m";
    public static final String YELLOW = "\u001B[93m";

    @Test
    public void test() {
        Main main = new Main();
        ReadCertificate rc = new ReadCertificate();

        testRead(main, rc, new ArrayList<String>());

        testWrite(rc, new ArrayList<String>());

        testReadProperties(main);
    }

    /**
     * Tests the read() function to read certificates with an example certificate
     *
     * @param main     Main class (needed to call main.sErr())
     * @param rc       ReadCertificate class (needed to call read())
     * @param testRead new ArrayList<>() (for Testing)
     */
    public void testRead(Main main, ReadCertificate rc, List<String> testRead) {
        //---- Test read() ----//
        soY("[INFO] Testing read() function");
        try {
            testRead = rc.read(new File("src/test/resources/testCertificate.crt"));
        } catch (IOException ioe) {
            System.out.println("[ERROR] IOException");
            ioe.printStackTrace();
        } catch (CertificateException ce) {
            System.out.println("[ERROR] Missing the certificate test file or wrong path file");
            ce.printStackTrace();
        }
        //----+
        assert testRead.get(1).equals("Version: 1");
        assert testRead.get(2).equals("Serial Number: 1234567890");
        assert testRead.get(3).equals("Issuer: CN=ca_name");
        assert testRead.get(4).equals("Subject: CN=owner_name");
        if (Integer.valueOf(testRead.get(6)) == 0) {
            assert testRead.get(5).equals("Validity: Mon Jan 21 00:00:00 CET 2019 - Tue Jan 28 10:05:06 CET 2020 - The certificate is valid.");
        } else {
            assert testRead.get(5).equals("Validity: Mon Jan 21 00:00:00 CET 2019 - Tue Jan 28 10:05:06 CET 2020 - The certificate is not valid.");
        }
        assert testRead.get(8).equals("Hash Code: -40609");
        assert testRead.get(9).equals("Signature algorithm: SHA256withRSA. The algorithm type is RSA.");
        soY("[INFO] Completed testing read() function");
        //---- +----------+ ----//
    }

    /**
     * Tests the write() function to generate certificates with example values and reads the certificate with the read() function
     * to see if it is working correctly
     *
     * @param wc        ReadCertificate class (needed to call write())
     * @param testWrite new ArrayList<>() (for Testing)
     */
    public void testWrite(ReadCertificate wc, List<String> testWrite) {
        //---- Test write() ----//
        soY("[INFO] Testing write() function");
        String iName = "CN=ca" + (int) (Math.random() * 100);
        String sName = "CN=owner" + (int) (Math.random() * 100);
        KeyPairGenerator keyGen;
        KeyPair keyPair;
        Date now = new Date();
        Date eDate = now;
        eDate.setTime(now.getTime() + 1000000L);
        long serNumber = now.getTime();
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512);
            keyPair = keyGen.generateKeyPair();
            try {
                wc.write(new File("src/test/resources/testGeneratedCertificate.crt"), iName, sName, keyPair, serNumber, now, eDate, "SHA256withRSA");
                testWrite = wc.read(new File("src/test/resources/testGeneratedCertificate.crt"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException nSAE) {
            nSAE.printStackTrace();
        }
        //----+
        assert testWrite.get(1).equals("Version: 1");
        assert testWrite.get(2).equals("Serial Number: " + serNumber);
        assert testWrite.get(3).equals("Issuer: " + iName);
        assert testWrite.get(4).equals("Subject: " + sName);
        assert testWrite.get(9).equals("Signature algorithm: SHA256withRSA. The algorithm type is RSA.");
        soY("[INFO] Completed testing write() function");
        //---- +----------+ ----//
    }

    /**
     * Tests the readProperties() function by taking a wrong and a correct config.properties file and testing if the results will
     * are correct
     *
     * @param main Main class (needed to call
     */
    public void testReadProperties(Main main) {
        soY("[INFO] Testing readProperties() function");
        assert main.readProperties("conffiig.properties", false).isEmpty();
        assert !main.readProperties("config.properties", false).isEmpty();

        soY("[INFO] Completed testing readProperties() function");
    }

    /**
     * Print out msg in yellow
     *
     * @param msg message to print
     */
    public void soY(String msg) {
        System.out.println(YELLOW + msg + RESET);
    }
}
