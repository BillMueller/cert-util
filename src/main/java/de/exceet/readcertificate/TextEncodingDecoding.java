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

    public void main(String pFile, String fileName, String certName, int mode) {
        Main main = new Main();
        TextEncodingDecoding tc = new TextEncodingDecoding();
        try {
            if (mode == 0)
                tc.encode(pFile, fileName, tc.getPublicKey(pFile + "/" + certName + ".crt"), getValidity(pFile + "/" + certName + ".crt"));
            else
                tc.decode(pFile, fileName, tc.getPrivateKey(pFile + "/" + certName + "_private_key"));
        } catch (IOException e) {
            main.printError("file couldn't be found");
        } catch (CertificateException e) {
            main.printError("certificate couldn't be found");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            main.printError("invalid key spec");
        }
    }

    public void encode(String pFile, String file, PublicKey pubKey, boolean validity) throws IOException {
        Main main = new Main();
        String in = read(pFile, file);
        if (!validity)
            main.printError("The certificate isn't valid. Please contact the owner of the certificate to get a new one");
        else {
            if (in != null) {
                try {
                    byte[] signed = encrypt(pubKey, in);
                    try (FileOutputStream fos = new FileOutputStream(pFile + "/" + file + ".txt")) {
                        fos.write(signed);
                    }
                } catch (NoSuchPaddingException nP) {
                    nP.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    main.printError("RSA algorithm isn't valid");
                } catch (InvalidKeyException e) {
                    main.printError("the public/private key was invalid");
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException bP) {
                    bP.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    main.printError("UTF-8 isn't supported");
                }
            }
        }
    }

    public void decode(String pFile, String file, PrivateKey priKey) throws IOException {
        Main main = new Main();
        String out = null;
        try {
            byte[] verified;
            try (FileInputStream fis = new FileInputStream(pFile + "/" + file + ".txt")) {
                verified = decrypt(priKey, IOUtils.toByteArray(fis));
            }
            out = new String(verified, "UTF-8");
        } catch (NoSuchPaddingException nP) {
            nP.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            main.printError("RSA algorithm isn't valid");
        } catch (InvalidKeyException e) {
            main.printError("the public/private key was invalid");
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException bP) {
        } catch (UnsupportedEncodingException e) {
            main.printError("UTF-8 isn't supported");
        }
        write(pFile, file, out);
    }

    public byte[] encrypt(PublicKey publicKey, String message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Main main = new Main();
        main.printInfo("encrypting Text");
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public byte[] decrypt(PrivateKey privateKey, byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Main main = new Main();
        main.printInfo("decrypting Text");
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
                main.printInfo("writing text to " + f + ".txt");
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
            main.printInfo("wrong private key - file is empty");
        }
    }

    public String read(String pF, String f) {
        Main main = new Main();
        String out = "";
        try {
            int c = 0;
            if (pF == null || f == null) {
                main.printError("you have to enter a file name with parameter --file <filename>");
            } else {
                main.printInfo("reading text from " + f + ".txt");
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
            main.printError("could not find file " + f + " (wrong directory or file name)");
            main.printInfo("dont use example.txt as file name but only example");
            out = null;
        } catch (IOException ioe) {
            main.printError("could not read file " + f + ".txt");
            out = null;
        }
        return out;
    }

    public PrivateKey getPrivateKey(String file) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Main main = new Main();
        main.printInfo("getting private key");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(FileUtils.readFileToByteArray(new File(file))));
    }

    public PublicKey getPublicKey(String file) throws CertificateException, IOException {
        Main main = new Main();
        main.printInfo("getting public key");
        InputStream inStream = new FileInputStream(file);
        javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);
        inStream.close();
        return cert.getPublicKey();
    }

    public boolean getValidity(String file) throws CertificateException, IOException {
        Main main = new Main();
        EditCertificate ec = new EditCertificate();
        main.printInfo("checking validity");
        InputStream inStream = new FileInputStream(file);
        javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);
        inStream.close();
        return ec.testDate(cert.getNotBefore(), cert.getNotAfter());
    }
}