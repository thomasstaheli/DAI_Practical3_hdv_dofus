package ch.heigvd.dai.api.users;

import io.javalin.http.Context;

public class User {
    public static void getMe(Context ctx) {
        ctx.result("My user");
    }
}
