create table if not exists account
(
    id bigint auto_increment primary key,
    name text not null,
    username text not null,
    password text not null -- plain text, for simplicity, but we all know this is not good for production
);

create table if not exists character
(
    id bigint auto_increment primary key,
    account_id bigint not null references account(id) on delete cascade,
    name text not null,
    class text not null,
    health int not null,
    attack int not null,
    experience int not null,
    defense int,
    stamina int,
    healing int,
    mana int
);

create table if not exists leaderboard
(
    character_id bigint not null references character(id) on delete cascade,
    wins int not null,
    losses int not null,
    draws int not null
);

create table if not exists match
(
    id bigint auto_increment primary key,
    challenger_id bigint not null references character(id) on delete cascade,
    opponent_id bigint not null references character(id) on delete cascade,
    match_outcome text not null,
    challenger_xp int not null,
    opponent_xp int not null
);

create table if not exists round
(
    id bigint auto_increment primary key,
    match_id bigint not null references match(id) on delete cascade,
    round_number int not null,
    character_id bigint not null references character(id) on delete cascade,
    health_delta int not null,
    stamina_delta int not null,
    mana_delta int not null
);

