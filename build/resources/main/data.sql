insert into account (name, username, password) values ('Game', 'game', 'game');
insert into account (name, username, password) values ('Motyka', 'motyka', 'heslo');

-- User character
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'motyka'),'Moni The Teacher', 100, 40, 30, 30, 6000, 'SORCERER');

-- Harry Potter Characters
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Harry Potter', 100, 40, 30, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Hermione Granger', 90, 40, 40, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Ron Weasley', 120, 50, 20, 10, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Severus Snape', 80, 60, 30, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Albus Dumbledore', 90, 40, 40, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Lord Voldemort', 80, 80, 10, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Minerva McGonagall', 100, 40, 30, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Bellatrix Lestrange', 80, 70, 20, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Draco Malfoy', 100, 40, 30, 30, 0, 'SORCERER');
insert into character (account_id, name, health, attack, mana, healing, experience, class) values ((select id from account where username = 'game'), 'Neville Longbottom', 130, 30, 10, 30, 0, 'SORCERER');

-- Star Wars Characters
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Luke Skywalker', 110, 40, 20, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Yoda', 80, 30, 50, 40, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Han Solo', 120, 40, 10, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Darth Vader', 100, 60, 10, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Obi-Wan Kenobi', 100, 40, 30, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Emperor Palpatine', 80, 80, 10, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Mace Windu', 110, 40, 20, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Darth Maul', 90, 60, 20, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Kylo Ren', 100, 50, 20, 30, 0, 'WARRIOR');
insert into character (account_id, name, health, attack, stamina, defense, experience, class) values ((select id from account where username = 'game'), 'Finn', 130, 20, 10, 40, 0, 'WARRIOR');
