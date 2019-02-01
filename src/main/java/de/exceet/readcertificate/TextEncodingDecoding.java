package de.exceet.readcertificate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.security.cert.CertificateException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class TextEncodingDecoding {

    //TODO try to call this function in "Start" and add it as command in J-Console
    public static void main(String pFile, String certFileName, String fileName, int mode) {
        Main main = new Main();
        TextEncodingDecoding tc = new TextEncodingDecoding();
        try {
            tc.code(pFile, fileName, mode, tc.getPrivateKey(pFile + "/private_key"), tc.getPublicKey(pFile + "/" + certFileName + ".crt"));
        } catch (IOException e) {
            main.printError("file couldn't be found");
        } catch (CertificateException e) {
            main.printError("certificate couldn't be found");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void code(String pFile, String file, int mode, PrivateKey priKey, PublicKey pubKey) throws Exception {
        Main main = new Main();
        String in = read(pFile, file), out = null;
        try {
            if (mode == 0) {
                byte[] signed = encrypt(pubKey, in);
                System.out.println(in);
                try (FileOutputStream fos = new FileOutputStream("C:/Users/jvansprang/Desktop/in.txt")) {
                    fos.write(signed);
                }
            } else {
                byte[] verified;
                try (FileInputStream fis = new FileInputStream("C:/Users/jvansprang/Desktop/in.txt")) {
                    verified = decrypt(priKey, IOUtils.toByteArray(new FileInputStream("C:/Users/jvansprang/Desktop/in.txt")));
                }
                out = new String(verified, "UTF-8");
                System.out.println(new String(verified, "UTF-8"));
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
        if (mode == 1) {
            write(pFile, file, out);
        }
    }

    public byte[] encrypt(PublicKey publicKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }

    public void write(String pF, String f, String msg) {
        Main main = new Main();
        try {
            if (pF == null || f == null) {
                main.printError("you have to enter a file name with parameter --file <filename>");
            } else {
                BufferedWriter bw = new BufferedWriter(new FileWriter(pF + "/" + f + ".txt"));
                String[] l = msg.split("\n");
                int ls = l.length, c = 0;
                while (c < ls) {
                    bw.write(l[c]);
                    c++;
                    if (c < ls)
                        bw.newLine();
                }
                bw.close();
            }
        } catch (FileNotFoundException fE) {
            main.printError("could not find file " + f + " (wrong directory or file name");
            main.printInfo("dont use example.txt as file name but only example");
        } catch (IOException ioe) {
            main.printError("could not read file " + f + ".txt");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public String read(String pF, String f) {
        Main main = new Main();
        String out = "";
        try {
            int c = 0;
            FileReader fr;
            if (pF == null || f == null) {
                main.printError("you have to enter a file name with parameter --file <filename>");
            } else {
                BufferedReader br = new BufferedReader(new FileReader(pF + "/" + f + ".txt"));
                String s;
                out = br.readLine();
                while ((s = br.readLine()) != null) {
                    out = out + "\n" + s;
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

    public PrivateKey getPrivateKey(String file) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(FileUtils.readFileToByteArray(new File(file))));
    }

    public PublicKey getPublicKey(String file) throws CertificateException, IOException {
        InputStream inStream = new FileInputStream(file);
        javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);
        inStream.close();
        return cert.getPublicKey();
    }
}