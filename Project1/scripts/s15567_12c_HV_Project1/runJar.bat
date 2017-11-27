start java -jar Monitor.jar
sleep 2
start java -jar Agent.jar 10001 localhost:8080
start java -jar Agent.jar 15468 localhost:10001 localhost:8080
start java -jar Agent.jar 12357 localhost:15468 localhost:8080
start java -jar Agent.jar 12987 localhost:12357 localhost:8080
start java -jar Agent.jar 19872 localhost:12987 localhost:8080
