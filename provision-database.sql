
CREATE DATABASE HLR;

USE HLR

DROP TABLE IF EXISTS user;
CREATE TABLE user(ID INT AUTO_INCREMENT, name VARCHAR(100) UNIQUE , PRIMARY KEY(ID));
INSERT INTO user (ID, name) VALUES (1, "user1");
INSERT INTO user (ID, name) VALUES (2, "user2");
INSERT INTO user (ID, name) VALUES (3, "user3");
INSERT INTO user (ID, name) VALUES (4, "user4");
INSERT INTO user (ID, name) VALUES (5, "user5");
INSERT INTO user (ID, name) VALUES (6, "user6");
INSERT INTO user (ID, name) VALUES (7, "user7");

DROP TABLE IF EXISTS activeSubscriber;
CREATE TABLE activeSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), userID INT, deviceType VARCHAR(30), FOREIGN KEY(userID) REFERENCES user(ID));
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("173864374","911234567", 1, "temperature");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("273864374","912234568", 2, "motion");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("383864374","913234569", 3, "video");
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("493864574","914234570", 4, "image"); 
INSERT INTO activeSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("573964574","915234571", 5, "smoke"); 

DROP TABLE IF EXISTS suspendedSubscriber;
CREATE TABLE suspendedSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), userID INT, deviceType VARCHAR(30), FOREIGN KEY(userID) REFERENCES user(ID));
INSERT INTO suspendedSubscriber (SIMCARD , MSISDN, userID, deviceType) VALUES ("673964574","916234571", 6, "temperature"); 
