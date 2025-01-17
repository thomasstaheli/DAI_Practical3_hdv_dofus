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

CREATE TABLE inventory_hdv (
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
CREATE TRIGGER transfer_item_to_hdv
AFTER INSERT ON offer
FOR EACH ROW
BEGIN
    -- Vérifier si l'utilisateur a suffisamment d'objets
    UPDATE inventory_user
    SET quantity = quantity - NEW.quantity
    WHERE user_id = NEW.user_id AND item_id = NEW.item_id;

    -- Supprimer l'objet si la quantité devient 0
    DELETE FROM inventory_user
    WHERE user_id = NEW.user_id AND item_id = NEW.item_id AND quantity = 0;

    -- Insérer dans l'inventaire de l'HDV
    INSERT INTO inventory_hdv (user_id, item_id, offer_id)
    VALUES (NEW.user_id, NEW.item_id, NEW.offer_id);
END;

CREATE TRIGGER complete_transaction_on_buyer_update
AFTER UPDATE OF buyer_id ON offer
FOR EACH ROW
WHEN NEW.buyer_id IS NOT NULL
BEGIN
    -- Calculer et déduire le montant de l'acheteur
    UPDATE user
    SET kamas = kamas - (NEW.price_in_kamas * NEW.quantity)
    WHERE user_id = NEW.buyer_id;

    -- Ajouter le montant au vendeur
    UPDATE user
    SET kamas = kamas + (NEW.price_in_kamas * NEW.quantity)
    WHERE user_id = OLD.user_id;

    -- Ajouter les objets à l'inventaire de l'acheteur
    INSERT INTO inventory_user (user_id, item_id, quantity)
    VALUES (NEW.buyer_id, NEW.item_id, NEW.quantity)
    ON CONFLICT(user_id, item_id) DO UPDATE
    SET quantity = quantity + NEW.quantity;

    -- Supprimer l'objet de l'inventaire de l'HDV
    DELETE FROM inventory_hdv
    WHERE offer_id = NEW.offer_id;

    -- Supprimer l'offre
    DELETE FROM offer
    WHERE offer_id = NEW.offer_id;
END;




