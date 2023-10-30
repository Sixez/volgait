INSERT INTO vit_accounts(id, username, password, admin, balance)
VALUES
(1, 'user', '$2a$10$mb5oVgb0n.HqaYlfdgMKk.j2AzZk9NhRvFz2zUcCYi6Ewi3mxz1xS', false, 0),
(2, 'admin', '$2a$10$LApOKafBnldTUu5vVc73yeovC19ZZwXNzkOKS5BEFWOO.UAlWp.Ii', true, 1000000);

INSERT INTO vit_transport(id, owner_id, description, transport_type, model, identifier, color, can_be_rented, latitude, longitude, minute_price, day_price)
VALUES
(1, 1, 'A simple car for simple person', 'Car', 'ВАЗ-2310', 'А123ОВ 31', 'Белый', true, 50.573835, 36.581804, 2.5, 1000),
(2, 1, 'Less simple car for less simple user', 'Car', 'Skoda Rapid', 'С867АК 31', 'Черный', true, 50.555218, 36.571965, 4.5, 2000),
(3, 1, 'A classic vehicle', 'Bike', 'Урал', 'Б777ИК 31', 'Серый', true, 50.532920, 36.685458, 2, 700),
(4, 1, 'Luxurious transport', 'Scooter', 'Apachie Pro', 'О418ВМ 31', 'Черный', true, 50.578352, 36.592063, 15, 5000),
(5, 2, 'The best transport', 'Scooter', 'Zipper M6', 'В777ВС 31', 'Черный', true, 50.554487, 36.564294, 1, 500),
(6, 2, 'Opinion leader', 'Bike', 'BMW R 1250 R', 'В591АА 31', 'Черный', true, 50.566378, 36.551949, 3, 1300);
