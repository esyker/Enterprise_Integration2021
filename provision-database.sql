
CREATE DATABASE HLR;

USE HLR


CREATE TABLE activeSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), userID INT, deviceType VARCHAR(30));
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("173864374","911234567", 1, "temperature");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("273864374","912234568", 2, "motion");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("383864374","913234569", 3, "video");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("493864574","914234570", 4, "image"); 
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("573964574","915234571", 5, "smoke"); 

CREATE TABLE suspendedSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), userID INT, deviceType VARCHAR(30));
INSERT INTO suspendedSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("673964574","916234571", 6, "temperature"); 
