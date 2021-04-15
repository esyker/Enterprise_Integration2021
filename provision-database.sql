
CREATE DATABASE HLR;
FLUSH PRIVILEGES;
USE HLR
CREATE TABLE activeSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), deviceType VARCHAR(30));
INSERT INTO activeSubscriber (SIMCARD , MSISDN) VALUES (173864374,911234567, "temperature");
INSERT INTO activeSubscriber (SIMCARD , MSISDN) VALUES (273864374,911234568, "motion");
INSERT INTO activeSubscriber (SIMCARD , MSISDN) VALUES (373864374,911234569, "smoke");
INSERT INTO activeSubscriber (SIMCARD , MSISDN) VALUES (373864574,911234570, "video"); 
INSERT INTO activeSubscriber (SIMCARD , MSISDN) VALUES (373964574,911234571, "image"); 

CREATE TABLE suspendedSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), deviceType VARCHAR(30));
