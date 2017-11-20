start java -jar s15567_12c_HV/Monitor.jar
sleep 2
start java -jar s15567_12c_HV/Agent.jar 10000 localhost:8080
start java -jar s15567_12c_HV/Agent.jar 15468 localhost:10000 localhost:8080
start java -jar s15567_12c_HV/Agent.jar 12357 localhost:15468 localhost:8080
start java -jar s15567_12c_HV/Agent.jar 12987 localhost:12357 localhost:8080
start java -jar s15567_12c_HV/Agent.jar 19872 localhost:12987 localhost:8080
