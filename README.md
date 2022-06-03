# Java Application Deployment Project: UdaSecurity 

![UdaSecurity Logo](UdaSecurity.png)

This is a home security application which tracks the the **status of sensors**, monitors **camera input**, and changes the **alarm state** of the system based on inputs. Users can **arm the system** for when they’re home or away as well as **disarm the system**.

## Project Goals:
-   Update  `pom.xml`  with Missing Dependencies
-   Split project into multiple modules(Security service module and Image service module).
-   Refactor it to be unit-testable.
-   Write unit tests to cover requirements, provide full coverage of all methods that implements the application requirements.
-   Fix any bugs.
-   Automatically run unit tests.
-   Perform static code analysis (generate a `spotbugs.html` report).
-   Build executable file.
## Run Executable
Navigate to `starter/catpoint-parent/security-service/target` folder
Run `java -jar security-service-1.0-SNAPSHOT-jar-with-dependencies.jar`
## Static Code Analysis
Navigate to `starter/catpoint-parent/security-service/site` folder

Check `spotbugs.html`
## More Info:
This is a capstone project of Udacity's Java Programming Nanodegree: Java Application Deployment.

Detailed requirements and starter code can be found here:
https://github.com/udacity/cd0384-java-application-deployment-projectstarter/tree/master/starter
# License  
  
[License](LICENSE.md)
