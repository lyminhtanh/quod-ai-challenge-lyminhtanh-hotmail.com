# Technical decisions:
1. Frameworks/libraries and benefit
- JAVA 8
- MAVEN
- DEPENDENCIES:
    1. commons-io, commons-csv: for IO operation, export CSV
    2. lombok: using @Builder, @Log4j2, ... for better readable and extendable code. Also prevent boilerplate code.
    3. commons-chain: apply chain of responsibility patern so that each metric will be treated as a node in chain. Adding or removing a node is easy.
    4. commons-collections4: helpful for process collections
    5. log4j: for logging
    6. jackson-databind: JSON parsing
    7. maven plugins
- Key techniques: `Stream API`, `Map data structure`, `concurrency`

2. Improve code for performance

At first I implement with single thread, using List at most. 
And it causes performance issue with Gigabyte data.
Then I applied concurrency together with Map data structure, the speed and performance have been improved significantly. 

3. Refactor for readability and maintainability

* I decide to use Chain of Responsibily design pattern to process main flow of the program. 
Each metric will be treated as a node in the chain.
Each node in the chain will be process separately but the data go as a pineline from this node to others. The flow is like this:

```metric1 -> metric2 -> ... -> aggregate results -> export csv```

* Also apply inheritence for better resusable and maintainable code. (HealthMetric.java and its children)

* Use annotation supported by lombok to speed up progress and avoid boilerplate code.
...

# HOW TO RUN PROGRAM:
1. Run with jar file
`java -cp HealthScoreCalculator-1.0.jar HealthScoreCalculator {startDate} {endDate} {strategy}`

- startDate and endDate ISO format: for example 2019-08-01T00:00:00Z
- strategy: one of support strategy as below:
+ ALL_METRIC : run with all current implemented metrics below
+ AVERAGE_ISSUE_OPEN_TIME : Average time that an issue remains opened 
+ AVERAGE_COMMIT : Average number of commits (push) per day (to any branch)
+ AVERAGE_PULL_REQUEST_MERGE_TIME: Average time for a pull request to get merged
+ AVERAGE_COMMITS_PER_DEVELOPERS_RATIO: Ratio of commit per developers
+ AVERAGE_OPENING_TO_CLOSED_ISSUE_RATIO: Ratio of closed to open issues
+ NUMBER_OF_RELEASES : Number of releases

* If startDate is not passed, ALL_METRIC strategy will be applied
* Working example command:
`java -cp HealthScoreCalculator-1.0.jar HealthScoreCalculator 2019-08-01T00:00:00Z 2019-08-01T03:00:00Z AVERAGE_ISSUE_OPEN_TIME`

2. How to run with maven
- Go to root of project
- Compile and execute:
`maven clean compile exec:java -Dexec.args="2019-08-01T00:00:00Z 2019-08-01T01:00:00Z ALL_METRIC"`
3. How to create jar file
- Go to root of project
- Exucute maven command: 
```mvn package```
# NEXT STEPS:
- Mostly consuming time taken by IO process and JSON parsing although concurrence programming has been applied. 
=> Solution: apply other technique to manage threads more effectively like threadpool
- Some metrics uses same Github Event Type to calculate score, they can reuse without repeatting IO operation and JSON parsing
- Implement other metrics
- Unit tests





