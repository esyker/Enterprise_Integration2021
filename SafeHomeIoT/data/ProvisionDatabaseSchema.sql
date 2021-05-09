DROP DATABASE IF EXISTS HLR;
CREATE DATABASE HLR;

USE HLR;
DROP TABLE IF EXISTS Subscriber;
CREATE TABLE Subscriber (SIMCARD INT, MSISDN INT, deviceType VARCHAR(30), state VARCHAR(30), PRIMARY KEY (SIMCARD));
INSERT INTO Subscriber (SIMCARD , MSISDN, deviceType, state) VALUES (173864374, 911234567, 'temperature', 'ACTIVE');
INSERT INTO Subscriber (SIMCARD , MSISDN, deviceType, state) VALUES (273864374, 912234568, 'motion', 'ACTIVE');
INSERT INTO Subscriber (SIMCARD , MSISDN, deviceType, state) VALUES (383864374, 913234569, 'video', 'ACTIVE');
INSERT INTO Subscriber (SIMCARD , MSISDN, deviceType, state) VALUES (493864574, 914234570, 'image', 'ACTIVE');
INSERT INTO Subscriber (SIMCARD , MSISDN, deviceType, state) VALUES (573964574, 915234571, 'smoke', 'ACTIVE');
INSERT INTO Subscriber (SIMCARD , MSISDN, deviceType, state) VALUES (673964574, 916234571, 'temperature', 'SUSPENDED');
