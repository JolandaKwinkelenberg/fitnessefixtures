# fitnessefixtures
The FitNesse Fixtures in this repository are created as part of an overall solution for automating the test cycle.
Visit https://fitnesse.solutions for more info.
and for downloads of compiled versions.

## Linux installation ReadMe
The readme for Linux can be found in the directory 'linuxscripts', here on github: https://github.com/consag/fitnessefixtures/tree/master/linuxscripts


## Download
You can download the jar with all compiled fixtures from our fitnesse.solutions website: https://fitnesse.solutions/files/ConsagFitNesseFixtures.jar

Note: Fixtures are compiled for Java 1.8

If you're running FitNesse with Java 1.7 you will need the 1.7 jar: https://fitnesse.solutions/files/java17/ConsagFitNesseFixtures.jar

## Fixtures in action

Some fixtures can be seen in action on our fitnesse.solutions website: https://fitnesse.solutions/ConsagDemo, e.g. the fixture to start an Informatica PowerCenter workflow: https://fitnesse.solutions/ConsagDemo.StartWorkflow 

## Fixture documentation
We're are working on it.
Check Gist, here on github, for some examples:

[Start Workflow example](https://gist.github.com/jacbeekers/2dd0c97d2b3f98457a1223af83341a09)

## FitNesse Configuration
Fixtures rely on various parameters that need to be set up in properties files. Not all fixtures use all properties files.

### Fixture: StartWorkflow
* properties files used
powercenter.properties, wsh.properties, appwsh.properties
* Documentation and example: https://gist.github.com/jacbeekers/30e972aaecbb54dd0941568ee87cd6d4
