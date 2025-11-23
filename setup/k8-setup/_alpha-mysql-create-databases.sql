-- 1) Create all databases

create database if not exists audits;
create database if not exists banks;
create database if not exists cards;
create database if not exists cases;
create database if not exists certificates;
create database if not exists creditcards;
create database if not exists customers;
create database if not exists flowable;
create database if not exists loans;
create database if not exists openbanking;
create database if not exists passkit;
create database if not exists payments;
create database if not exists temenos;

-- 2) Create 'alpha' user

create user if not exists 'alpha'@'%' identified by 'alpha';

-- 3 Grant access to databases to 'alpha' user

grant all on audits.* to 'alpha'@'%';
grant all on banks.* to 'alpha'@'%';
grant all on cards.* to 'alpha'@'%';
grant all on cases.* to 'alpha'@'%';
grant all on certificates.* to 'alpha'@'%';
grant all on creditcards.* to 'alpha'@'%';
grant all on customers.* to 'alpha'@'%';
grant all on flowable.* to 'alpha'@'%';
grant all on loans.* to 'alpha'@'%';
grant all on openbanking.* to 'alpha'@'%';
grant all on passkit.* to 'alpha'@'%';
grant all on payments.* to 'alpha'@'%';
grant all on temenos.* to 'alpha'@'%';

-- 4) Vault database

create database if not exists vault;
grant all on vault.* to 'alpha'@'%';

-- 5) Flush privileges

flush privileges;

