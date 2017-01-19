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
