CREATE TABLE user (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    mdp VARCHAR(50) NOT NULL
);

CREATE TABLE item (
    item_id SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    description TEXT
);

CREATE TABLE offer (
    offer_id SERIAL PRIMARY KEY,
    item_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE OR REPLACE table inventory_hdv (
	user_id INT,
    item_id INT,
    offer_id INT,

    PRIMARY KEY (user_id, item_id, offer_id),

    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
	FOREIGN KEY (offer_id) REFERENCES offer(offer_id)
);


CREATE OR REPLACE FUNCTION transfer_item_to_hdv()
RETURNS TRIGGER AS $$
BEGIN
    -- Vérifie si l'utilisateur possède suffisamment de quantité dans son inventaire
    IF (SELECT quantity FROM inventory_user 
        WHERE user_id = NEW.user_id AND item_id = NEW.item_id) >= NEW.quantity THEN
        
        -- Met à jour l'inventaire de l'utilisateur en déduisant la quantité offerte
        UPDATE inventory_user
        SET quantity = quantity - NEW.quantity
        WHERE user_id = NEW.user_id AND item_id = NEW.item_id;

        -- Si la quantité devient 0, supprimer l'objet de l'inventaire de l'utilisateur
        DELETE FROM inventory_user
        WHERE user_id = NEW.user_id AND item_id = NEW.item_id AND quantity = 0;

        -- Insère l'objet dans l'inventaire de l'HDV
        INSERT INTO inventory_hdv (user_id, item_id, offer_id)
        VALUES (NEW.user_id, NEW.item_id, NEW.offer_id);

    ELSE
        -- Génère une erreur si l'utilisateur ne possède pas assez de quantité
        RAISE EXCEPTION 'Quantité insuffisante dans l''inventaire de l''utilisateur pour cette offre.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER transfer_item_on_offer_insert
AFTER INSERT ON offer
FOR EACH ROW
EXECUTE FUNCTION transfer_item_to_hdv();


