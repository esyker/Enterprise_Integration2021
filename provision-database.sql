
CREATE DATABASE HLR;

USE HLR

DROP TABLE IF EXISTS activeSubscriber;
CREATE TABLE activeSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), deviceType VARCHAR(30), PRIMARY KEY (SIMCARD));
INSERT INTO activeSubscriber (SIMCARD , MSISDN, deviceType) VALUES ("173864374","911234567", "temperature");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, deviceType) VALUES ("273864374","912234568", "motion");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, deviceType) VALUES ("383864374","913234569", "video");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, deviceType) VALUES ("493864574","914234570", "image"); 
INSERT INTO activeSubscriber (SIMCARD , MSISDN, deviceType) VALUES ("573964574","915234571", "smoke"); 

DROP TABLE IF EXISTS suspendedSubscriber;
CREATE TABLE suspendedSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), deviceType VARCHAR(30), PRIMARY KEY (SIMCARD));
INSERT INTO suspendedSubscriber (SIMCARD , MSISDN, deviceType) VALUES ("673964574","916234571", "temperature");
