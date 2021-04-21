###########SENSORS#################
CREATE DATABASE IF NOT EXISTS SafeHomeIoTEvents;
USE SafeHomeIoTEvents;

DROP TABLE IF EXISTS temperatureMessage;
CREATE TABLE temperatureMessage (
	ID INT AUTO_INCREMENT, 
	SIMCARD INT,
	MSISDN INT,
	measurement FLOAT,
	type VARCHAR(30),
	ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS imageMessage;
CREATE TABLE imageMessage (
	ID INT AUTO_INCREMENT, 
	SIMCARD INT,
	MSISDN INT,
	description VARCHAR(30),
	type VARCHAR(30),
	ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS videoMessage;
CREATE TABLE videoMessage (
	ID INT AUTO_INCREMENT, 
	SIMCARD INT,
	MSISDN INT,
	description VARCHAR(30),
	type VARCHAR(30),
	ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS smokeMessage;
CREATE TABLE smokeMessage (
	ID INT AUTO_INCREMENT, 
	SIMCARD INT,
	MSISDN INT,
	measurement FLOAT,
	type VARCHAR(30),
	ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS motionMessage;
CREATE TABLE motionMessage (
	ID INT AUTO_INCREMENT, 
	SIMCARD INT,
	MSISDN INT,
	description VARCHAR(30),
	type VARCHAR(30),
	ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(ID)
);

