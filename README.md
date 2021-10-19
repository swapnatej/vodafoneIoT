## Vodafone IoT Application 
# Capable of below operation
1. Capable of batch loading the data provided in the attached csv file.
2. Retrieving the customer details.

## Building Locally
```
	Build Spring Boot Project with Maven
		mvn install / mvn clean install
	Run Spring Boot app using Maven:
		mvn spring-boot:run
[optional] Run Spring Boot app with java -jar command
		java -jar target/vodafone-api-server-0.1.0-SNAPSHOT.jar
```
## Swagger Ui
http://localhost:8080/swagger-ui.html#/vodafone-io-t-controller

## Testing Locally
```
## To Load data file
curl --location --request POST 'localhost:8080/iot/event/v1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "filePath":"IoTData.csv"
}'

## To get cunstomer details
curl --location --request GET 'localhost:8080/iot/event/v1?productId=WG11155638&tstmp=1582605257000'
```

