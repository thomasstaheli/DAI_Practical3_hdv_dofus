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
	buyer_id INT,
    price_in_kamas INT NOT NULL,
    quantity INT NOT NULL,

    FOREIGN KEY (item_id) REFERENCES item(item_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
	FOREIGN KEY (buyer_id) REFERENCES user(user_id)
);

CREATE TABLE inventory_user (
    user_id INT,
    item_id INT,
    quantity INT NOT NULL,

    PRIMARY KEY (user_id, item_id),

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


-- TRIGGERS ET FONCTIONS --
-- GESTION DES OFFRES --

-- Trigger qui gère le passage de l'objet depuis l'inventaire de l'utilisateur dans l'HDV
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

CREATE OR REPLACE FUNCTION complete_transaction()
RETURNS TRIGGER AS $$
DECLARE
    total_price INT;
    seller_id INT;
BEGIN
    -- Calculer le prix total de l'offre
    total_price := NEW.price_in_kamas * NEW.quantity;

    -- Récupérer l'ID du vendeur
    SELECT user_id INTO seller_id FROM offer WHERE offer_id = NEW.offer_id;

    -- Vérifier si l'acheteur a suffisamment de kamas
    IF (SELECT kamas FROM user WHERE user_id = NEW.buyer_id) >= total_price THEN
        
        -- Déduire le prix total des kamas de l'acheteur
        UPDATE user
        SET kamas = kamas - total_price
        WHERE user_id = NEW.buyer_id;

        -- Ajouter le prix total aux kamas du vendeur
        UPDATE user
        SET kamas = kamas + total_price
        WHERE user_id = seller_id;

        -- Transférer les objets de l'inventaire de l'HDV vers l'inventaire de l'acheteur
        INSERT INTO inventory_user (user_id, item_id, quantity)
        VALUES (NEW.buyer_id, NEW.item_id, NEW.quantity)
        ON CONFLICT (user_id, item_id) DO UPDATE
        SET quantity = inventory_user.quantity + EXCLUDED.quantity;

        -- Supprimer l'objet de l'inventaire de l'HDV
        DELETE FROM inventory_hdv
        WHERE offer_id = NEW.offer_id;

        -- Supprimer l'offre après la transaction
        DELETE FROM offer
        WHERE offer_id = NEW.offer_id;

    ELSE
        -- Si l'acheteur n'a pas assez de kamas, lever une exception
        RAISE EXCEPTION 'Acheteur (%s) ne dispose pas de suffisamment de kamas pour cette transaction.', NEW.buyer_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER complete_transaction_on_buyer_update
AFTER UPDATE OF buyer_id ON offer
FOR EACH ROW
WHEN (NEW.buyer_id IS NOT NULL)
EXECUTE FUNCTION complete_transaction();



