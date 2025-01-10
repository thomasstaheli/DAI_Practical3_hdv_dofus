package ch.heigvd.dai.api.auth;

import io.javalin.http.Context;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Auth {
    JsonWebToken jsonWebToken;

    public Auth() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.jsonWebToken = new JsonWebToken();
    }

    public void register(Context ctx) {

    }
}
