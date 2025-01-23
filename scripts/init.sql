DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      user_id INTEGER PRIMARY KEY,
                      username VARCHAR(50) NOT NULL,
                      password VARCHAR(50) NOT NULL,
                      kamas INT
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