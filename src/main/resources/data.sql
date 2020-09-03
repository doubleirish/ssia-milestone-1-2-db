insert into USER (ID, USERNAME, PASSWORD) values (1, 'john' ,'12345');
insert into USER (ID, USERNAME, PASSWORD) values (2, 'admin' ,'secret2');
INSERT INTO AUTHORITY ( USER_ID ,AUTHORITY ) values (1, 'ROLE_USER');
INSERT INTO AUTHORITY ( USER_ID ,AUTHORITY ) values (2, 'ROLE_ADMIN');
