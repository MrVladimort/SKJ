start java project1.Monitor
timeout 2
start java project1.Agent 10001 localhost:8080
timeout 1
start java project1.Agent 15468 localhost:10001 localhost:8080
timeout 1
start java project1.Agent 12357 localhost:15468 localhost:8080
timeout 1
start java project1.Agent 12987 localhost:12357 localhost:8080
timeout 1
start java project1.Agent 19872 localhost:12987 localhost:8080
