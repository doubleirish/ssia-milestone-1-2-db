drop table if exists AUTHORITY;
drop table if exists USER;
drop table if exists GRANT;
drop table if exists CLIENT;


create table if not exists USER
(
    ID INT auto_increment primary key,
    USERNAME VARCHAR(50) not null,
    PASSWORD VARCHAR(255) not null
);

create table  if not exists AUTHORITY
(
    ID INT auto_increment primary key,
    USER_ID INT not null,
    AUTHORITY VARCHAR(50) not null,
    constraint AUTHORITY_USER_USERNAME_FK
        foreign key (USER_ID) references USER (ID)
);


create table if not exists CLIENT
(
    ID INT auto_increment   primary key,
    NAME  VARCHAR(255) not null,
    SCOPE VARCHAR(10) not null default 'user',
    SECRET VARCHAR(255) not null,
    REDIRECT_URI VARCHAR(255) not null
);


create table if not exists GRANT
(
    ID INT auto_increment   primary key,
    GRANT VARCHAR(50) not null,
    CLIENT_ID INT not null,
    constraint GRANT_CLIENT_ID_FK
        foreign key (CLIENT_ID) references CLIENT (ID)
);

