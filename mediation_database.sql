CREATE TABLE User(ID INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), address VARCHAR(50));

###########SENSORS#################
CREATE DATABASE IF NOT EXISTS collectedMSGs;
USE collectedMSGs;
CREATE TABLE device(ID INT AUTO_INCREMENT, userID INT, deviceType VARCHAR(20)
		, PRIMARY KEY(ID,userID));

CREATE TABLE deviceConfiguration(ID INT AUTO_INCREMENT, userID INT
		, PRIMARY KEY(ID,userID), description VARCHAR(200));

CREATE TABLE temperatureMessage(ID INT AUTO_INCREMENT, deviceID INT
		, PRIMARY KEY(ID,deviceID), measurement FLOAT);

CREATE TABLE imageMessage(ID INT AUTO_INCREMENT, deviceID INT
		, PRIMARY KEY(ID,deviceID), Idescription VARCHAR(30));

CREATE TABLE videoMessage(ID INT AUTO_INCREMENT, deviceID INT
		, PRIMARY KEY(ID,deviceID), Vdescription VARCHAR(30));

CREATE TABLE smokeMessage(ID INT AUTO_INCREMENT, deviceID INT
		, PRIMARY KEY(ID,deviceID), measurement FLOAT);

CREATE TABLE motionMessage(ID INT AUTO_INCREMENT, deviceID INT
		, PRIMARY KEY(ID,deviceID), Mdescription VARCHAR(30));

