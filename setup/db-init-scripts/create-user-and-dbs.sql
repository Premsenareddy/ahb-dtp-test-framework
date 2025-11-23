-- Create user

--   NOTE: Run the following SQL if you aren't running via `docker-compose up`
--
--     create user alpha;
--     alter user alpha password 'alpha';

create database customers;
create database cards;
create database loans;
create database payments;
create database certificate;
create database flowable;
create database temenos;
create database banks;
create database banking_config;

-- Grant access of database to `alpha` user

grant all privileges on database customers to alpha;
grant all privileges on database cards to alpha;
grant all privileges on database payments to alpha;
grant all privileges on database loans to alpha;
grant all privileges on database certificate to alpha;
grant all privileges on database flowable to alpha;
grant all privileges on database temenos to alpha;
grant all privileges on database banks to alpha;
grant all privileges on database banking_config to alpha;

