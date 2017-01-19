# InvasivePlantSpecies
The goal is to detect invasive plant species in google street view images using a trained convolutional neural network (CNN).
## Web annotation tool
The gsvannotation folder contains code for a web based annotation tool.  Initially, the tool allows you to enter a lat/lon , if there is a GSV panorama for this lat/lon, it is downloaded and you can draw bounding boxes around invasive plant species.  The goal is to use this tool to collect training data and/or test trained models and verify their performance.

### Software requirements
To build and run the project you will need the following
* Mysql server
  * See the gsvannotation/src/main/db/invasivedb.mwb file for an EER diagram you can edit using Mysql workbench
  * The gsvannotation/src/main/db/dbschema.sql contains the forward engineered sql script from the eer diagram, plus some inserts
* Java
* Apache maven ( http://maven.apache.org/ ) 

To build the annotation tool, cd into the gsvannotation folder and run `mvn install`.  Maven should download all the required dependencies.  Dependencies include:
* SparkJava ( http://sparkjava.com/ ), a java based web framework
* sql2o ( http://www.sql2o.org/ ), database layer
* Jackson ( https://github.com/FasterXML/jackson ), for JSON serializing/deserializing 

Alternatively, you can use an IDE such as Eclipse ( www.eclipse.org ).  In eclipse, import the project as a maven project.

To run the project
* first create the database using the dbschema.sql file.  
* build the project using `mvn install`
* from the gsvannotation folder run `java -jar target/gsvannotation-jar-with-dependencies.jar [dbhost] [dbusername] [dbpassword] [dbport]` .  Assuming the database is on the same machine, it would be something like `java -jar target/gsvannotation-jar-with-dependencies.jar localhost someuser supersecretpassword 3306`
* open a browser to http://localhost:4567/index.html 

In eclipse, you can also run/debug the Main.java class as a plain java program.  You will have to edit the run/debug configuration to include the command line arguments.  Eclipse makes it easy to debug changes, once you run the program, any changes you make are usually available instantly.  In some cases it may be necessary to restart the program.
