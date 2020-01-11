# quod-ai-challenge-lyminhtanh-hotmail.com
* Notice:
- split to process file by file (bc lines list may getting huge)

* Run with maven
clean compile exec:java -Dexec.args="2019-08-01T00:00:00Z 2019-08-01T01:00:00Z" -Dexec.cleanupDaemonThreads=false

* Run jar file:
java -cp ai.quod.challenge.HealthScoreCalculator-1.0-SNAPSHOT.jar HealthScoreCalculator 2019-08-01T00:00:00Z 2019-08-01T03:00:00Z ALL_METRIC
