INSERT INTO item (nom, description) VALUES
('Bouclier de l Ange', 'Un bouclier magique inspiré par les anges. Il est léger et puissant, offrant une grande protection.'),
('Epée de Iop', 'Une épée légendaire qui symbolise la puissance du dieu Iop. Elle est utilisée par les plus braves guerriers.'),
('Anneau du Chevalier', 'Un anneau forgé en métal pur, il est très résistant et symbolise la loyauté du chevalier.'),
('Potion de Soins', 'Une potion magique qui soigne les blessures et restaure la vitalité d un aventurier.'),
('Bouclier en Bois', 'Un bouclier en bois solide mais léger, idéal pour les jeunes aventuriers.'),
('Cape de l Inconnu', 'Une cape mystérieuse qui accorde à son porteur une invisibilité partielle.'),
('Arc de la Nature', 'Un arc long fabriqué à partir du bois d un arbre ancien. Il est très apprécié des archers.'),
('Casque de Guerrier', 'Un casque robuste conçu pour protéger la tête des guerriers dans la bataille.'),
('Grimoire de Magie', 'Un livre ancien contenant des sorts puissants, souvent utilisé par les mages et sorciers.'),
('Bottes de Vitesse', 'Des bottes enchantées qui augmentent la vitesse de déplacement de son porteur.'),
('Potion de Force', 'Une potion qui augmente la force physique de l utilisateur pour un court laps de temps.'),
('Bouclier de Fer', 'Un bouclier solide fait de métal, souvent utilisé par les aventuriers en quête de protection.'),
('Sachet de Fleurs', 'Un petit sac contenant des fleurs magiques utilisées dans l art de la guérison.'),
('Hache de Bois', 'Une hache simple mais efficace pour couper du bois ou attaquer des ennemis.'),
('Robe de Magicien', 'Une robe portée par les mages, augmentant leur pouvoir magique.'),
('Sac à Dos', 'Un sac pratique pour transporter les objets et les ressources récoltées lors des aventures.'),
('Boucles d Oreilles de l Explorateur', 'Des boucles d oreilles légères qui augmentent les capacités d exploration et de détection.'),
('Eau de Vie', 'Une boisson rare qui régénère instantanément la santé de l utilisateur.'),
('Hache du Bois', 'Une hache ordinaire utilisée pour couper du bois et fabriquer des objets.'),
('Gant de Cuir', 'Un gant de cuir souple qui protège les mains tout en maintenant une grande agilité.');

INSERT INTO user (first_name, last_name, mdp) VALUES
('Alice', 'Liddell', 'alice123'),
('Bob', 'Builder', 'bob123');

INSERT INTO invetory_user (user_id, item_id, quantity) VALUES
(0, 0, 10),
(0, 1, 10),
(0, 2, 10),
(0, 3, 10),
(1, 0, 10),
(1, 1, 10),
(1, 2, 10),
(1, 3, 10)