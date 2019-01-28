package de.exceet.testreadcerfiticate;

import de.exceet.readcertificate.ReadCertificate;
import org.junit.Test;
//import org.junit.jupiter.api.Test;

import javax.security.cert.CertificateException;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TestReadCertificate{
    @Test
    public void test(){
        //---- Test read() ----//
        List<String> testRead = new ArrayList<String>();
        try {
            ReadCertificate rc = new ReadCertificate();
            testRead = rc.read(new File("src/test/resources/testCertificate.crt"));
        }catch (IOException ioe){
            System.out.println(ioe);
            System.out.println("IOException");
        }catch (CertificateException ce){
            System.out.println(ce);
            System.out.println("Missing the certificate test file or wrong path file");
        }
        //----+
        System.out.println("Test read() function");
        assert testRead.get(1).equals("Version: 1");
        assert testRead.get(2).equals("Serial Number: 1234567890");
        assert testRead.get(3).equals("Issuer: CN=ca_name");
        assert testRead.get(4).equals("Subject: CN=owner_name");
        if(Integer.valueOf(testRead.get(6)) == 0){
            assert testRead.get(5).equals("Validity: Mon Jan 21 00:00:00 CET 2019 - Tue Jan 28 10:05:06 CET 2020 - The certificate is valid.");
        }else{
            assert testRead.get(5).equals("Validity: Mon Jan 21 00:00:00 CET 2019 - Tue Jan 28 10:05:06 CET 2020 - The certificate is not valid.");
        }
        assert testRead.get(8).equals("Hash Code: -40609");
        assert testRead.get(9).equals("Signature algorithm: SHA256withRSA. The algorithm type is RSA.");
        System.out.println("Completed testing read() function");
        //---- +----------+ ----//

        //---- Test write() ----//
        List<String> testWrite = new ArrayList<String>();
        String iName = "CN=ca" + (int)(Math.random()*100);
        String sName = "CN=owner" + (int)(Math.random()*100);
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
                ReadCertificate wc = new ReadCertificate();
                wc.write(new File("src/test/resources/testGeneratedCertificate.crt"), iName, sName, keyPair, serNumber, now, eDate, "SHA256withRSA");
                testWrite = wc.read(new File("src/test/resources/testGeneratedCertificate.crt"));
            } catch (IOException ioe) {
                System.out.println(ioe);
            } catch (Exception e) {
                System.out.println(e);
            }
        }catch(NoSuchAlgorithmException nSAE) {
            System.out.println(nSAE);
        }
        //----+
        System.out.println("Test write() function");
        assert testWrite.get(1).equals("Version: 1");
        assert testWrite.get(2).equals("Serial Number: "+ serNumber);
        assert testWrite.get(3).equals("Issuer: " + iName);
        assert testWrite.get(4).equals("Subject: " + sName);
        assert testWrite.get(9).equals("Signature algorithm: SHA256withRSA. The algorithm type is RSA.");
        System.out.println("Completed testing write() function");
        //---- +----------+ ----//
    }
}
