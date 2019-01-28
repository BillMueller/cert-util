package de.exceet.testreadcerfiticate;

import de.exceet.readcertificate.ReadCertificate;
import org.junit.Test;
//import org.junit.jupiter.api.Test;

import javax.security.cert.CertificateException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestReadCertificate{
    @Test
    public void test(){
        List<String> test = new ArrayList<String>();
        try {
            ReadCertificate tc = new ReadCertificate();
            test = tc.read(new File("src/test/resources/testcertificate.crt"));
        }catch (IOException ioe){
            System.out.println(ioe);
            System.out.println("IOException");
        }catch (CertificateException ce){
            System.out.println(ce);
            System.out.println("Missing the certificate test file or wrong path file");
        }
        System.out.println("test fist certificate");
        assert test.get(1).equals("Version: 1");
        assert test.get(2).equals("Serial Number: 1234567890");
        assert test.get(3).equals("Issuer: CN=ca_name");
        assert test.get(4).equals("Subject: CN=owner_name");
        if(Integer.valueOf(test.get(6)) == 0){
            assert test.get(5).equals("Validity: Mon Jan 21 00:00:00 CET 2019 - Tue Jan 28 10:05:06 CET 2020 - The certificate is valid.");
        }else{
            assert test.get(5).equals("Validity: Mon Jan 21 00:00:00 CET 2019 - Tue Jan 28 10:05:06 CET 2020 - The certificate is not valid.");
        }
        assert test.get(8).equals("Hash Code: -40609");
        assert test.get(9).equals("Signature algorithm: SHA256withRSA. The algorithm type is RSA.");
        System.out.println("first certificate tested");
    }
}
