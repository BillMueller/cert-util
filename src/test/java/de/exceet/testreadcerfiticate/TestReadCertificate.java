package de.exceet.testreadcerfiticate;

import de.exceet.readcertificate.EditCertificate;
import de.exceet.readcertificate.Main;
import de.exceet.readcertificate.TextEncodingDecoding;
import org.junit.Test;

import javax.security.cert.CertificateException;
import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class TestReadCertificate {
    public static final String RESET = "\u001B[0m";
    public static final String YELLOW = "\u001B[93m";

    @Test
    public void test() {
        Main main = new Main();
        EditCertificate ec = new EditCertificate();
        TextEncodingDecoding tc = new TextEncodingDecoding();

        testRead(main, ec, new ArrayList<>());

        testWrite(ec, new ArrayList<>());

        testReadProperties(main);

        testStringToDate(main);

        testTestDate(main, ec);

        testTextEncodingDecoding(main, tc);
    }

    /**
     * Tests the read() function to read certificates with an example certificate
     *
     * @param main     Main class (needed to call main.sErr())
     * @param rc       EditCertificate class (needed to call read())
     * @param testRead new ArrayList<>() (for Testing)
     */
    private void testRead(Main main, EditCertificate rc, List<String> testRead) {
        //---- Test read() ----//
        main.printInfo("Testing read() function");
        try {
            testRead = rc.read("src/test/resources/testCertificate", main);
        } catch (IOException ioe) {
            main.printError("IOException");
            ioe.printStackTrace();
        } catch (CertificateException ce) {
            main.printError("Missing the certificate test file or wrong path file");
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
        main.printInfo("Completed testing read() function");
        //---- +----------+ ----//
    }

    /**
     * Tests the write() function to generate certificates with example values and reads the certificate with the read() function
     * to see if it is working correctly
     *
     * @param wc        EditCertificate class (needed to call write())
     * @param testWrite new ArrayList<>() (for Testing)
     */
    private void testWrite(EditCertificate wc, List<String> testWrite) {
        //---- Test write() ----//
        Main main = new Main();
        main.printInfo("Testing write() function");
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
                wc.write("src/test/resources/testGeneratedCertificate", "", iName, sName, keyPair, serNumber, now, eDate, "SHA256withRSA", true, main);
                testWrite = wc.read("src/test/resources/testGeneratedCertificate", main);
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
        main.printInfo("Completed testing write() function");
        //---- +----------+ ----//
    }

    /**
     * Tests the readProperties() function by taking a wrong and a correct config.properties file and testing if the results will
     * are correct
     *
     * @param main Main class (needed to call
     */
    private void testReadProperties(Main main) {
        main.printInfo("Testing readProperties() function");
        try {
            assert !main.readProperties("config.properties", false, true).isEmpty();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        main.printInfo("Completed testing readProperties() function");
    }

    private void testStringToDate(Main main) {
        main.printInfo("Testing StringToDate()");
        assert main.stringToDate("25-12-2012").equals(new GregorianCalendar(2012, Calendar.DECEMBER, 25).getTime());
        assert main.stringToDate("25-01-2012").equals(new GregorianCalendar(2012, Calendar.JANUARY, 25).getTime());
        main.printInfo("Successfully tested StringToDate()");
    }

    private void testTestDate(Main main, EditCertificate ec) {
        main.printInfo("Testing testDate()");
        long date = new Date().getTime();
        assert ec.testDate(new Date(date - 1000L), new Date(date + 1000L));
        assert !ec.testDate(new Date(date + 1000L), new Date(date + 10000L));
        main.printInfo("Successfully tested testDate()");
    }

    private void testTextEncodingDecoding(Main main, TextEncodingDecoding tc) {
        main.printInfo("Testing encode() and decode()");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("src/test/resources/testFile.txt"));
            bw.write("A test message");
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            KeyPairGenerator kpg = null;
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            KeyPair keyPair = kpg.generateKeyPair();
            tc.encode("src/test/resources/testFile.txt", keyPair.getPublic(), true, false, main);
            tc.decode("src/test/resources/testFile.txt", keyPair.getPrivate(), false, main);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/test/resources/testFile.txt"));
            assert br.readLine().equals("A test message");
        } catch (Exception e) {
            e.printStackTrace();
        }
        main.printInfo("Successfully tested encode() and decode()");
    }
}