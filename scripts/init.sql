DROP TABLE IF EXISTS user;
CREATE TABLE user (
    user_id INTEGER PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    kamas INT NOT NULL
);

DROP TABLE IF EXISTS item;
CREATE TABLE item (
    item_id INTEGER PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    description TEXT
);

DROP TABLE IF EXISTS offer;
CREATE TABLE offer (
    offer_id INTEGER PRIMARY KEY,
    item_id INT NOT NULL,
    user_id INT NOT NULL,
    buyer_id INT,
    price_in_kamas INT NOT NULL,
    quantity INT NOT NULL,

    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (buyer_id) REFERENCES user(user_id)
);

DROP TABLE IF EXISTS inventory_user;
CREATE TABLE inventory_user (
    user_id INT,
    item_id INT,
    quantity INT NOT NULL,

    PRIMARY KEY (user_id, item_id),

    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);


DROP TRIGGER IF EXISTS transfer_item_to_hdv;
CREATE TRIGGER transfer_item_to_hdv
    AFTER INSERT ON offer
    FOR EACH ROW
BEGIN
    SELECT
        RAISE(ABORT, 'Insufficient quantity in inventory_user.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_user
        WHERE user_id = NEW.user_id AND item_id = NEW.item_id AND quantity >= NEW.quantity
    );

    UPDATE inventory_user
    SET quantity = quantity - NEW.quantity
    WHERE user_id = NEW.user_id AND item_id = NEW.item_id;

    DELETE FROM inventory_user
    WHERE user_id = NEW.user_id AND item_id = NEW.item_id AND quantity = 0;
END;

DROP TRIGGER IF EXISTS update_offer;
CREATE TRIGGER update_offer
    AFTER UPDATE OF quantity ON offer
    FOR EACH ROW
BEGIN
    SELECT
        RAISE(ABORT, 'Insufficient quantity in inventory_user.')
    WHERE NOT EXISTS (
        SELECT 1 FROM inventory_user
        WHERE user_id = NEW.user_id AND item_id = NEW.item_id AND (OLD.quantity >= NEW.quantity OR quantity >= NEW.quantity - OLD.quantity)
    );

    INSERT INTO inventory_user(quantity, item_id)
    values(NEW.quantity - OLD.quantity, NEW.item_id)
    ON CONFLICT(user_id, item_id) DO UPDATE
        SET quantity = quantity - (NEW.quantity - OLD.quantity);

    DELETE FROM inventory_user
    WHERE user_id = NEW.user_id AND item_id = NEW.item_id AND quantity = 0;
END;

DROP TRIGGER IF EXISTS complete_transaction_on_buyer_update;
CREATE TRIGGER complete_transaction_on_buyer_update
    AFTER UPDATE OF buyer_id ON offer
    FOR EACH ROW
    WHEN NEW.buyer_id IS NOT NULL
BEGIN
    SELECT
        RAISE(ABORT, 'Insufficient kamas for user.')
    WHERE NOT EXISTS (
        SELECT 1 FROM user
        WHERE user_id = NEW.user_id AND kamas >= NEW.price_in_kamas
    );

    UPDATE user
    SET kamas = kamas - NEW.price_in_kamas
    WHERE user_id = NEW.buyer_id;

    UPDATE user
    SET kamas = kamas + NEW.price_in_kamas
    WHERE user_id = OLD.user_id;

    INSERT INTO inventory_user (user_id, item_id, quantity)
    VALUES (NEW.buyer_id, NEW.item_id, NEW.quantity)
    ON CONFLICT(user_id, item_id) DO UPDATE
        SET quantity = quantity + NEW.quantity;

    DELETE FROM offer
    WHERE offer_id = NEW.offer_id;
END;
