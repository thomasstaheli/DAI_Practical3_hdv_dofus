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

    public void login(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        String token = header.replace("Bearer ", "");
        if (!this.jsonWebToken.isValid(token)) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        // TODO ctx.json("Ok");
    }

    public void disconnect(Context ctx) {

    }

    public void protect(Context ctx) {

    }

    public int getMe(Context ctx) {
        return -1;
    }
}
