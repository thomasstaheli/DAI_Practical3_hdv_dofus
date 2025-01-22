package ch.heigvd.dai.api.hdv;

import ch.heigvd.dai.api.Status;
import ch.heigvd.dai.api.auth.Auth;
import ch.heigvd.dai.database.Sqlite;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Hdv {
    private final Sqlite database;
    private final Auth auth;
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
    }

    public void getAll(Context ctx) throws SQLException {
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer", new Object[]{});
                ResultSet result = pstmt.executeQuery()
        ) {
            List<OfferEntry> offers = new ArrayList<>();
            while (result.next()) {
                offers.add(OfferEntry.get(result));
            }
            ctx.status(200).json(offers);
        }
    }

    public void getMe(Context ctx) throws SQLException {
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer WHERE user_id = ?", new Object[]{auth.getMe(ctx)});
                ResultSet result = pstmt.executeQuery()
        ) {
            List<OfferEntry> offers = new ArrayList<>();
            while (result.next()) {
                offers.add(OfferEntry.get(result));
            }
            ctx.status(200).json(offers);
        }
    }

    public void buy(Context ctx) throws SQLException {
        int id = Integer.parseInt(ctx.pathParam("id"));
        try (
                PreparedStatement pstmt = database.prepare("SELECT * FROM offer WHERE offer_id = ?", new Object[]{id});
                ResultSet result = pstmt.executeQuery()
        ) {
            if (!result.next()) throw new UnauthorizedResponse();
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
            pstmt.execute();
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
            pstmt.execute();
            ctx.status(200).json(Status.ok());
        }
    }

    public void create(Context ctx) {
        OfferBody body = OfferBody.full(ctx);
        try (PreparedStatement pstmt = database.prepare("INSERT INTO offer(item_id, user_id, price_in_kamas, quantity) VALUES (?, ?, ?, ?)", new Object[]{body.itemId, auth.getMe(ctx), body.price, body.amount})) {
            pstmt.execute();
            ctx.status(201).json(Status.ok());
        } catch (SQLException e) {
            throw new UnauthorizedResponse();
        }
    }

    public void update(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        OfferUpdateBody body = OfferUpdateBody.full(ctx);

        try (
                PreparedStatement pstmt = database.prepare("UPDATE offer SET price_in_kamas = ?, quantity = ? WHERE offer_id = ? AND user_id = ?", new Object[]{body.price, body.amount, id, auth.getMe(ctx)})
        ) {
            pstmt.execute();
            ctx.status(200).json(Status.ok());
        } catch (SQLException e) {
            throw new UnauthorizedResponse();
        }
    }

    public void partialUpdate(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        OfferUpdateBody body = OfferUpdateBody.partial(ctx);

        StringBuilder query = new StringBuilder("UPDATE offer SET ");
        List<Object> params = new ArrayList<>();

        if (body.amount != null) {
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
                pstmt.execute();
            } catch (SQLException e) {
                throw new UnauthorizedResponse();
            }
        }

        ctx.status(200).json(Status.ok());
    }
}
