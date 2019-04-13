--liquibase formatted sql
--changeset sb:example_transactions failOnError:false
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-07-29 20:18:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-07-29 20:18:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-07-29 20:18:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-07-30 11:34:00',-50);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-08-05 17:17:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-08-05 17:17:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-08-05 17:17:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-08-07 11:33:00',-50);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-08-12 09:17:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-08-12 09:17:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-08-12 09:17:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-08-13 10:21:00',-47);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-08-21 10:09:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-08-21 10:09:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-08-21 10:09:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-08-22 10:19:00',-45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-08-21 10:09:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-08-21 10:09:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-08-21 10:09:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-08-22 10:19:00',-45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-08-26 15:15:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-08-26 15:15:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-08-26 15:15:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-08-27 10:17:00',-30);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-09-02 14:47:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-09-02 14:47:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-09-02 14:47:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-09-04 11:13:00',-55);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-09-10 13:37:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-09-10 13:37:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-09-10 13:37:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-09-11 11:13:00',-25);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-09-17 16:01:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-09-17 16:01:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-09-17 16:01:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-09-18 14:01:00',-105);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,1,'2016-09-24 22:01:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,2,'2016-09-24 22:01:00', 45);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,3,'2016-09-24 22:01:00', 35);
insert into transactions (account_id,transaction_type_id,transaction_time,transaction_amount) values (4,4,'2016-09-25 22:01:00',-125);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (4,6,45);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (4,2,45);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (4,3,35);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (5,3,35);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (4,4,-125);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (5,1,45);
insert into transactions (account_id,transaction_type_id,transaction_amount) values (5,4,-80);
--rollback truncate table transactions; alter table transactions alter transaction_id restart with 1;
