package ch.heigvd.dai.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class JsonWebToken {
    private record Pair<T, U>(T first, U second) {}
    private final Algorithm certificate;
    private final JWTVerifier verifier;

    public JsonWebToken() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Pair<RSAPublicKey, RSAPrivateKey> certificate = JsonWebToken.getCertificate();
        this.certificate = Algorithm.RSA256(certificate.first, certificate.second);
        this.verifier = JWT.require(this.certificate).build();
    }

    private static void generateCertificate() throws NoSuchAlgorithmException, IOException {
        Path path = Path.of("./rsa");
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        try (FileOutputStream publicFos = new FileOutputStream(path.resolve("certificate.pub").toString());
             FileOutputStream privateFos = new FileOutputStream(path.resolve("certificate").toString())) {
            publicFos.write(publicKey.getEncoded());
            privateFos.write(privateKey.getEncoded());
        }
    }

    private static boolean hasCertificate() {
        return Files.exists(Path.of("./rsa/certificate.pub")) && Files.exists(Path.of("./rsa/certificate"));
    }

    private static Pair<RSAPublicKey, RSAPrivateKey> getCertificate() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (!JsonWebToken.hasCertificate()) JsonWebToken.generateCertificate();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] publicKeyBytes = Files.readAllBytes(Path.of("./rsa/certificate.pub"));
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        byte[] privateKeyBytes = Files.readAllBytes(Path.of("./rsa/certificate"));
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

    public int getSubject(String token) throws JWTVerificationException {
        DecodedJWT decodedToken = verifier.verify(token);
        return Integer.parseInt(decodedToken.getSubject());
    }
}
