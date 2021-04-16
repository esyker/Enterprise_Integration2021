#Modules

Our project enterprise integration system is divided into 5 modules, which can be found in the SafeHomeIoT project folder:

1. simulator
2. provisioning
3. eventmediationpre
4. events
5. eventmediation


###Installation

Install maven.
Run the command ```mvn clean install``` from the project SafeHomeIoT root folder.
A jar file will be generated for each module.


###Running the project
The project should be run on different virtual machines/serverless functions (aws lambda). Each jar file is deployed on each of these different platforms.
There is a Kafka broker cluster running on AWS EC2. 
The project modules consume/produce to this kafka broker cluster.
The jar files of the modules that run on AWS Lambda are deployed directly to the cloud.
The jar files of the modules that run on virtual machines should run with the following command, from the module root folder:  
```mvn exec:java <arguments>```

1. provisioning - AWS Lambda
   Deployed directly to AWS Lambda with no arguments.  

2. simulator - Ubuntu Virtual Machine with kafka producer that produces all the messages from the IoT devices to a Kafka Topic named EventsTopic.
   Run the command: 
   ```mvn clean compile exec:java -Dexec.args="--telecommunications-provider-name SafeHomeTelco 
      --broker-list {KAFKA_BROKER_IP}:9092 --topic safehomeiot-events --hlr-server {DB_ENDPOINT} 
      --hlr-database HLR --hlr-username {DB_USER} --hlr-password {DB_PASSWORD} --hlr-table activeSubscriber"```

3. eventmediationpre
   1st Layer Mediation
   Ubuntu Virtual machine with a kafka Consumer and a Producer that consume messages from the EventsTopic provisioned by the simulator and write to
   several different topics, based on the client that each IoT device is associated to the events are written to a different topic for each client.
   Command: ```mvn exec:java -Dexec.args="-kafkaip \{KAFKA\_BROKER\_IP\}:9092 -topics safehomeiot-events"```

4. events
   This module shouldn't be called directly on any machine. It just contains event class definitions that are used in the eventmediation module.

5. eventmediation
   2nd Layer Mediation
   Ubuntu Virtual machine with a kafka Consumer that reads messages from the several topics. 
   There is one topic for each client, so events are consumed from several topics each for a different client.
   The events are written to a database and are also available to an upper layer to be consumed.
   Run the command: ```mvn clean compile exec:java -Dexec.args="-kafkaip  \{KAFKA\_BROKER\_IP\}:9092""```
 

##simulator module
This module simulates the IoT device network activity. 
The type of messages simulated are:

1. temperatureMessage:{ "ID":10, "deviceID":3, measurement:28.3, type:"temperature", ts:"2021-01-19 03:14:07"}

2. imageMessage:{ "ID":10, "deviceID":3, description:"cat", type:"image", ts:"2021-01-19 03:14:07"}

3. videoMessage:{ "ID":10, "deviceID":3, description:"cat moving", type:"video", ts:"2021-01-19 03:14:07"}

4. smokeMessage:{ "ID":10, "deviceID":3, measurement:0.7, type:"smoke", ts:"2021-01-19 03:14:07"}
 
5. motionMessage:{ "ID":10, "deviceID":3, measurement:1 , type:"smoke", ts:"2021-01-19 03:14:07"}

These messages are generated according to the IoT devices registered in a database that is defined in provisioning. The IoT devices are defined in that database that is
manipulated by the provisioning. The messages are generated according to the data in this database.

##provisioning module
This module activates the devices present in the radio network simulator. 
A database with two is created with the schema described by the following code:

```
CREATE DATABASE HLR;
CREATE TABLE  HLR.activeSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), userID INT);
CREATE TABLE HLR.suspendedSubscriber (SIMCARD VARCHAR(22), MSISDN VARCHAR(15), suspendedSubscriber INT);
```

Devices are simulated in the radio network for the devices with the SIMCARD and MSISDN defined in the activeSubscriber table.
There are four operations defined in provisioning:

1. activate(SIMCARD,MSISDN,userID)
The device identified by SIMCARD and MSISDN is added to the activeSusbcriber table together with the userID and (if needed) removed from the suspendedSubscriber table.

2. suspend(SIMCARD,MSISDN,userID)
The device identified by SIMCARD and MSISDN is removed from the activeSubscriber table and added to the suspendedSubscriber table together with the userID of the owner. 

3. delete(SIMCARD,MSISDN)
The device identified by SIMCARD and MSISDN is removed both from the activeSubscriber and suspendedSubscribe tables.

4. getStatus(SIMCARD,MSISDN) 
The current status of the device identified by SIMCARD and MSISDN is returned from the database, whether it is active or suspended.

###eventmediationpre module
This module contains the 1st level mediation layer. 
It consumes the messages from the EventsTopic, that is served by the simulator.
Then a new topic is chosen to which the messages are produced, based on the client that is the owner of the device, as there is a topic for each different client.
For example, for a message with the following signature { "ID":10, "deviceID":3, measurement:1 , type:"smoke", ts:"2021-01-19 03:14:07"}, the message would be consumed
and then the same message would be produced to a new topic in the kafka broker with the name TOPIC_USER10.


###eventmediation module
This module contains the 2nd level mediation layer. 
It consumes the messages from the clients topics and saves them to a database. 
It also makes the data available through an API to an upper layer, which will be developed in the next sprint. 