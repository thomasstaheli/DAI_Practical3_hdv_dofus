package ch.heigvd.dai.api.hdv;

import ch.heigvd.dai.api.Status;
import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.caching.Cacher;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Hdv {
    private final Sqlite database;
    private final Auth auth;
    private final Cacher cacher;
    public record OfferBody(Integer itemId, Integer price, Integer amount) {
        public static OfferBody full(Context ctx) {
            return ctx.bodyValidator(OfferBody.class).check((o) -> o.itemId != null && o.price != null && o.amount != null && o.price > 0 && o.amount > 0, "Invalid body").get();
        }
    }
    public record OfferUpdateBody(Integer price, Integer amount) {
        public static OfferUpdateBody full(Context ctx) {
            return ctx.bodyValidator(OfferUpdateBody.class).check((o) -> o.price != null && o.amount != null && o.price > 0 && o.amount > 0, "Invalid body").get();
        }
        public static OfferUpdateBody partial(Context ctx) {
            return ctx.bodyValidator(OfferUpdateBody.class).check((o) -> (o.price == null || o.price > 0) && (o.amount == null || o.amount > 0), "Invalid body").get();
        }
    }
    public record OfferEntry(Integer offerId, Integer itemId, Integer userId, Integer price, Integer amount) {
        public static OfferEntry get(ResultSet resultSet) throws SQLException {
            return new OfferEntry(
                    resultSet.getInt("offer_id"),
                    resultSet.getInt("item_id"),
                    resultSet.getInt("user_id"),
                    resultSet.getInt("price_in_kamas"),
                    resultSet.getInt("quantity")
            );
        }
    }

    public Hdv(Sqlite database, Auth auth) {
        this.database = database;
        this.auth = auth;
        this.cacher = new Cacher();
    }

    public void getAll(Context ctx) throws SQLException {
        cacher.checkCache("ALL", ctx);
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer", new Object[]{});
                ResultSet result = pstmt.executeQuery()
        ) {
            List<OfferEntry> offers = new ArrayList<>();
            while (result.next()) {
                offers.add(OfferEntry.get(result));
            }
            cacher.setCacheHeader("ALL", ctx);
            ctx.status(200).json(offers);
        }
    }

    public void getMe(Context ctx) throws SQLException {
        cacher.checkCache("ME:" + auth.getMe(ctx), ctx);
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer WHERE user_id = ?", new Object[]{auth.getMe(ctx)});
                ResultSet result = pstmt.executeQuery()
        ) {
            List<OfferEntry> offers = new ArrayList<>();
            while (result.next()) {
                offers.add(OfferEntry.get(result));
            }
            cacher.setCacheHeader("ME:" + auth.getMe(ctx), ctx);
            ctx.status(200).json(offers);
        }
    }

    public void buy(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer WHERE offer_id = ?", new Object[]{id});
                ResultSet result = pstmt.executeQuery()
        ) {
            if (!result.next()) throw new NotFoundResponse();
            OfferEntry offerEntry = OfferEntry.get(result);
            try (
                    PreparedStatement pstmt2 = database.prepare("SELECT 1 from user WHERE user_id = ? AND kamas >= ?", new Object[]{auth.getMe(ctx), offerEntry.price});
                    ResultSet result2 = pstmt2.executeQuery()
            ) {
                if (!result2.next()) throw new UnauthorizedResponse();
            }
        }

        try (
                PreparedStatement pstmt = database.prepare("UPDATE offer SET buyer_id = ? WHERE offer_id = ?", new Object[]{auth.getMe(ctx), id})
        ) {
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) throw new NotFoundResponse();
            cacher.removeCache(String.valueOf(id));
            cacher.setLastModified("ALL");
            try (ResultSet result = pstmt.getResultSet()) {
                if (!result.next()) throw new InternalServerErrorResponse();
                cacher.setLastModified("ME:" + result.getInt("user_id"));
            }
            ctx.status(200).json(Status.ok());
        }
    }

    public void remove(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        try (
                PreparedStatement pstmt = database.prepare("SELECT 1 FROM offer WHERE offer_id = ? AND user_id = ?", new Object[]{id, auth.getMe(ctx)});
                ResultSet result = pstmt.executeQuery()
        ) {
            if (!result.next()) throw new UnauthorizedResponse();
        }

        try (
                PreparedStatement pstmt = database.prepare("DELETE FROM offer WHERE offer_id = ? AND user_id = ?", new Object[]{id, auth.getMe(ctx)})
        ) {
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) throw new NotFoundResponse();
            cacher.removeCache(String.valueOf(id));
            cacher.setLastModified("ALL");
            cacher.setLastModified("ME:" + auth.getMe(ctx));
            ctx.status(200).json(Status.ok());
        }
    }

    public void create(Context ctx) throws SQLException {
        OfferBody body = OfferBody.full(ctx);
        try (
                PreparedStatement pstmt = database.prepare("SELECT 1 FROM inventory_user WHERE user_id = ? AND item_id = ? AND quantity >= ?", new Object[]{auth.getMe(ctx), body.itemId, body.amount});
                ResultSet result = pstmt.executeQuery()
        ) {
            if (!result.next()) throw new UnauthorizedResponse();
        }


        try (PreparedStatement pstmt = database.prepareWithKeys("INSERT INTO offer(item_id, user_id, price_in_kamas, quantity) VALUES (?, ?, ?, ?)", new Object[]{body.itemId, auth.getMe(ctx), body.price, body.amount}, new String[]{"offer_id"})) {
            pstmt.execute();
            try (ResultSet result = pstmt.getGeneratedKeys()) {
                if (!result.next()) throw new InternalServerErrorResponse();
                cacher.setLastModified(String.valueOf(result.getInt(1)));
                cacher.setLastModified("ALL");
                cacher.setLastModified("ME:" + auth.getMe(ctx));
            }
            ctx.status(201).json(Status.ok());
        }
    }

    public void update(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        OfferUpdateBody body = OfferUpdateBody.full(ctx);

        checkUpdateAmount(ctx, body, id);

        try (
                PreparedStatement pstmt = database.prepare("UPDATE offer SET price_in_kamas = ?, quantity = ? WHERE offer_id = ? AND user_id = ?", new Object[]{body.price, body.amount, id, auth.getMe(ctx)})
        ) {
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) throw new NotFoundResponse();
            cacher.setLastModified(String.valueOf(id));
            cacher.setLastModified("ALL");
            cacher.setLastModified("ME:" + auth.getMe(ctx));
            ctx.status(200).json(Status.ok());
        }
    }

    public void partialUpdate(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        OfferUpdateBody body = OfferUpdateBody.partial(ctx);

        StringBuilder query = new StringBuilder("UPDATE offer SET ");
        List<Object> params = new ArrayList<>();

        if (body.amount != null) {
            checkUpdateAmount(ctx, body, id);

            query.append("amount = ?, ");
            params.add(body.amount);
        }
        if (body.price != null) {
            query.append("price = ?, ");
            params.add(body.price);
        }

        if (!query.isEmpty()) {
            query.setLength(query.length() - 2);
            query.append(" WHERE offer_id = ? AND user_id = ?");
            params.add(id, auth.getMe(ctx));

            try (
                    PreparedStatement pstmt = database.prepare(query.toString(), params.toArray())
            ) {
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) throw new NotFoundResponse();
                cacher.setLastModified(String.valueOf(id));
                cacher.setLastModified("ALL");
                cacher.setLastModified("ME:" + auth.getMe(ctx));
            }
        }

        ctx.status(200).json(Status.ok());
    }

    private void checkUpdateAmount(Context ctx, OfferUpdateBody body, int id) throws SQLException {
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer WHERE user_id = ? AND offer_id = ?", new Object[]{auth.getMe(ctx), id});
                ResultSet result = pstmt.executeQuery()
        ) {
            if (!result.next()) throw new NotFoundResponse();
            OfferEntry offerEntry = OfferEntry.get(result);
            if (body.amount > offerEntry.amount) {
                try (
                        PreparedStatement pstmt2 = database.prepare("SELECT 1 from inventory_user WHERE user_id = ? AND item_id = ? AND quantity >= ?", new Object[]{auth.getMe(ctx), offerEntry.itemId, body.amount - offerEntry.amount});
                        ResultSet result2 = pstmt2.executeQuery()
                ) {
                    if (!result2.next()) throw new UnauthorizedResponse();
                }
            }
        }
    }
}
