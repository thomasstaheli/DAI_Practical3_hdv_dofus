package ch.heigvd.dai.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.nio.file.Files;
import java.security.*;
import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class JsonWebToken {
    Algorithm certificate;
    JWTVerifier verifier;

    public JsonWebToken() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Pair<RSAPublicKey, RSAPrivateKey> certificate = JsonWebToken.getCertificate();
        this.certificate = Algorithm.RSA256(certificate.first, certificate.second);
        this.verifier = JWT.require(this.certificate).build();
    }

    private record Pair<T, U>(T first, U second) {}

    private static void generateCertificate() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        try (FileOutputStream fos = new FileOutputStream("./rsa/publicKey.key")) {
            fos.write(publicKey.getEncoded());
        }

        try (FileOutputStream fos = new FileOutputStream("./rsa/privateKey.key")) {
            fos.write(privateKey.getEncoded());
        }
    }

    private static boolean hasCertificate() {
        return new File("./rsa/publicKey.key").exists() && new File("./rsa/privateKey.key").exists();
    }

    private static Pair<RSAPublicKey, RSAPrivateKey> getCertificate() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (!JsonWebToken.hasCertificate()) JsonWebToken.generateCertificate();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] publicKeyBytes = Files.readAllBytes(new File("publicKey.key").toPath());
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        byte[] privateKeyBytes = Files.readAllBytes(new File("privateKey.key").toPath());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        return new Pair<>(publicKey, privateKey);
    }

    public String generate(int id) {
        return JWT.create()
            .withSubject(String.valueOf(id))
            .sign(this.certificate);
    }

    public boolean isValid(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public int getSubject(String token) {
        try {
            DecodedJWT decodedToken = verifier.verify(token);

            return Integer.parseInt(decodedToken.getSubject());
        } catch (JWTVerificationException e) {
            return -1;
        }
    }
}
