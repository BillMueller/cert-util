package de.exceet.readcertificate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class TextEncodingDecoding {
    public static void main(String[] args) throws NoSuchAlgorithmException, Exception {
        // generate public and private keys

        TextEncodingDecoding tc = new TextEncodingDecoding();
//        List<String> x = new ArrayList<>();
//        x.add(0, "hey");
//        x.add(1, "HALLO");
//        tc.write("C:/Users/jvansprang/Desktop", "in", x);
//        System.out.println(tc.read("C:/Users/jvansprang/Desktop", "in"));

        KeyPair keyPair = tc.buildKeyPair();
        tc.code("C:/Users/jvansprang/Desktop", "in", 0, keyPair);
        tc.code("C:/Users/jvansprang/Desktop", "in", 1, keyPair);

//        byte[] signed = encrypt(keyPair.getPublic(), "testcode");
//        FileUtils.writeByteArrayToFile(new File("C:/Users/jvansprang/Desktop/in.txt"), signed);
//
//        byte[] verified = decrypt(keyPair.getPrivate(), FileUtils.readFileToByteArray(new File("C:/Users/jvansprang/Desktop/in.txt")));
//        System.out.println(new String(verified, "UTF-8"));
    }

    public void code(String pFile, String file, int mode, KeyPair keyPair) throws Exception {
        Main main = new Main();

        List<String> in = read(pFile, file), out = new ArrayList<>();
        int ls = in.size(), c = 0;
        System.out.println(ls);
        while (c < ls) {
            try {
                if (mode == 0) {
                    byte[] signed = encrypt(keyPair.getPrivate(), in.get(c));
                    System.out.println(in.get(c));
                    //FileUtils.writeByteArrayToFile(new File("C:/Users/jvansprang/Desktop/in.txt"), signed);
                    try (FileOutputStream fos = new FileOutputStream("C:/Users/jvansprang/Desktop/in.txt")) {
                        fos.write(signed);
                    }
                } else {
                    byte[] verified;
                    try (FileInputStream fis = new FileInputStream("C:/Users/jvansprang/Desktop/in.txt")) {
                        verified = decrypt(keyPair.getPublic(), IOUtils.toByteArray(new BufferedReader(new FileReader("C:/Users/jvansprang/Desktop/in.txt")), Charset.defaultCharset()));
                    }
                    // byte[] verified = decrypt(keyPair.getPrivate(), FileUtils.readFileToByteArray(new File("C:/Users/jvansprang/Desktop/in.txt")));
                    out.add(c, new String(verified, "UTF-8"));
                    System.out.println(new String(verified, "UTF-8"));
                    // FileUtils.writeByteArrayToFile(new File("C:/Users/jvansprang/Desktop/in.txt"), verified);
                }
            } catch (NoSuchPaddingException nP) {
                nP.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                main.printError("RSA algorithm isn't vlid");
            } catch (InvalidKeyException e) {
                main.printError("the public/private key was invalid");
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException bP) {
            } catch (UnsupportedEncodingException e) {
                main.printError("UTF-8 isn't supported");
            }
            c++;
        }
        if (mode == 1) {
            write(pFile, file, out);
        }
    }

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PrivateKey publicKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PublicKey privateKey, byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }

    public void write(String pF, String f, List<String> msg) {
        Main main = new Main();
        try {
            if (pF == null || f == null) {
                main.printError("you have to enter a file name with parameter --file <filename>");
            } else {
                BufferedWriter bw = new BufferedWriter(new FileWriter(pF + "/" + f + ".txt"));
                int ls = msg.size(), c = 0;
                while (c < ls) {
                    bw.write(msg.get(c));
                    bw.newLine();
                    c++;
                }
                bw.close();
            }
        } catch (FileNotFoundException fE) {
            main.printError("could not find file " + f + " (wrong directory or file name");
            main.printInfo("dont use example.txt as file name but only example");
        } catch (IOException ioe) {
            main.printError("could not read file " + f + ".txt");
        }
    }

    public List<String> read(String pF, String f) {
        Main main = new Main();
        List<String> out = new ArrayList<>();
        try {
            int c = 0;
            FileReader fr;
            if (pF == null || f == null) {
                main.printError("you have to enter a file name with parameter --file <filename>");
            } else {
                BufferedReader br = new BufferedReader(new FileReader(pF + "/" + f + ".txt"));
                String s;
                while ((s = br.readLine()) != null) {
                    out.add(c, s);
                    c++;
                }
                br.close();
            }
        } catch (FileNotFoundException fE) {
            main.printError("could not find file " + f + " (wrong directory or file name");
            main.printInfo("dont use example.txt as file name but only example");
        } catch (IOException ioe) {
            main.printError("could not read file " + f + ".txt");
        }
        return out;
    }
}
