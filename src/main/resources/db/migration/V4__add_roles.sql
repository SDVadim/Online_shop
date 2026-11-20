alter table if exists users
    add column role varchar(10) not null default 'USER';
