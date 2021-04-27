USE CustomerHandling;

DROP TABLE IF EXISTS Client;
CREATE TABLE Client (
    id INT,
    firstName VARCHAR(30),
    lastName VARCHAR(30),
    address VARCHAR(100),
    PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS DeviceType;
CREATE TABLE DeviceType (
    id INT,
    name VARCHAR(30),
    cost INT,
    PRIMARY KEY(id)
);

DROP TABLE IF EXISTS Device;
CREATE TABLE Device (
    SIMCARD INT,
    MSISDN INT,
    userId INT,
    deviceTypeId INT,
    PRIMARY KEY (SIMCARD),
    FOREIGN KEY (userId) REFERENCES Client(id),
    FOREIGN KEY (deviceTypeId) REFERENCES  DeviceType(id)
);

DROP TABLE IF EXISTS Service;
CREATE TABLE Service (
      id INT,
      name VARCHAR(50),
      cost INT,
      PRIMARY KEY (id)
);

DROP TABLE IF EXISTS ServiceDeviceType;
CREATE TABLE ServiceDeviceType (
    serviceId INT,
    deviceTypeId INT,
    PRIMARY KEY(serviceId, deviceTypeId),
    FOREIGN KEY(serviceId) REFERENCES Service(id),
    FOREIGN KEY(deviceTypeId) REFERENCES DeviceType(id)
);

DROP TABLE IF EXISTS Subscription;
CREATE TABLE Subscription (
    id INT,
    clientId INT,
    PRIMARY KEY(id),
    FOREIGN KEY (clientId) REFERENCES Client(id)
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