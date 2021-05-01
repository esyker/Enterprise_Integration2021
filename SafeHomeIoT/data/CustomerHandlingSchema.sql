DROP DATABASE IF EXISTS CustomerHandling;
CREATE DATABASE CustomerHandling;
USE CustomerHandling;

DROP TABLE IF EXISTS Customer;
CREATE TABLE Customer (
    id INT AUTO_INCREMENT,
    firstName VARCHAR(30),
    lastName VARCHAR(30),
    address VARCHAR(100),
    birthDate TIMESTAMP,
    PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS DeviceType;
CREATE TABLE DeviceType (
    id INT AUTO_INCREMENT,
    name VARCHAR(30),
    cost INT,
    PRIMARY KEY(id)
);

DROP TABLE IF EXISTS Device;
CREATE TABLE Device (
    SIMCARD INT,
    MSISDN INT,
    customerId INT,
    deviceTypeId INT,
    PRIMARY KEY (SIMCARD),
    FOREIGN KEY (customerId) REFERENCES Customer(id),
    FOREIGN KEY (deviceTypeId) REFERENCES  DeviceType(id)
);

DROP TABLE IF EXISTS Service;
CREATE TABLE Service (
      id INT AUTO_INCREMENT,
      name VARCHAR(50),
      cost INT,
      PRIMARY KEY (id)
);

DROP TABLE IF EXISTS ServiceDeviceType;
/*CREATE TABLE ServiceDeviceType (
    serviceId INT,
    deviceTypeId INT,
    PRIMARY KEY(serviceId, deviceTypeId),
    FOREIGN KEY(serviceId) REFERENCES Service(id),
    FOREIGN KEY(deviceTypeId) REFERENCES DeviceType(id)
);*/

DROP TABLE IF EXISTS Subscription;
CREATE TABLE Subscription (
    id INT AUTO_INCREMENT,
    customerId INT,
    PRIMARY KEY(id),
    FOREIGN KEY (customerId) REFERENCES Customer(id)
);

DROP TABLE IF EXISTS SubscriptionDevices;
CREATE TABLE SubscriptionDevices (
    subscriptionId INT,
    SIMCARD INT,
    PRIMARY KEY(subscriptionId, SIMCARD),
    FOREIGN KEY (subscriptionId) REFERENCES Subscription(id),
    FOREIGN KEY (SIMCARD) REFERENCES Device(SIMCARD)
);

DROP TABLE IF EXISTS SubscriptionDevices;
CREATE TABLE SubscriptionServices (
     subscriptionId INT,
     serviceId INT,
     PRIMARY KEY(subscriptionId, serviceId),
     FOREIGN KEY (subscriptionId) REFERENCES Subscription(id),
     FOREIGN KEY (serviceId) REFERENCES Service(id)
);

INSERT INTO Service (name, cost) VALUES ('House Security', 30);
INSERT INTO Service (name, cost) VALUES ('Inventory Management', 20);

Insert INTO DeviceType (name, cost) VALUES ('temperature', 5);
Insert INTO DeviceType (name, cost) VALUES ('motion', 7);
Insert INTO DeviceType (name, cost) VALUES ('smoke', 8);
Insert INTO DeviceType (name, cost) VALUES ('image', 10);
Insert INTO DeviceType (name, cost) VALUES ('video', 20);