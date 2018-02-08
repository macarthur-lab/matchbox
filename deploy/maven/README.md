## Build directly via Maven:

###  You will need:

* Java 1.8

* Maven 3.1 (available from https://maven.apache.org/)

* An authenticated (ideally) MongoDB instance (https://www.mongodb.com/)

###  Process:

<i>matchbox</i> relies on a modified version of Exomiser (https://github.com/exomiser/Exomiser), its reference data and libraries for the phenotype matching algorithm. Given that these libraries are currently not in a maven repository, we obtain this dependency via first building Exomiser and then followed by <i>matchbox</i>.  By building Exomiser first, we put its jars in the local maven repository, where the <i>matchbox</i> build is able to see and use them.

* Clone the Exomiser package
	- Make sure you have a settings.xml file in your ~/.m2/ directory with the following entry (to activate a local repsitory for Maven to use)
	
		```
			<settings>
    			<localRepository>${user.home}/.m3/repository</localRepository>
			</settings>
		```


	- Clone the repository

		```git clone https://github.com/exomiser/Exomiser```

	- Build the source files

		``` clean install package ```


* Now download the source code for <i>matchbox</i> and build on your system. It should now see all the Exomiser related dependencies in the local maven repository.

	- Clone the repository

		```git clone https://github.com/macarthur-lab/matchbox```


	- Update the following lines in the src/main/resources/application.properties appropriately. 
	
		- If you are NOT planning to proxy matchbox behind a HTTPS service, you would have to start server matchbox as HTTPS per MME requirements.
	
			- Uncomment and populate the server.ssl.* attributes to start <i>matchbox</i> as HTTPS. 
		
		- The "exomiser.data-directory=" field is required by Exomiser for phenotype matching. This reference data can be fetched by,
		
		```
			wget https://storage.googleapis.com/seqr-reference-data/1711_phenotype.tar.gz
			
		```
		
		Then unzip the file.
		
		Provide the path of the above untar'ed directory to the "exomiser.data-directory="  and "exomiser.phenotype.data-version" fields 
		
		For example, if your reference data was unzipped into,
		/dev/apps/ref_data/1711_phenotype
		
		You would populate the fields as such (note: the "1711_phenotype" in not in the path),
		
		```
			exomiser.data-directory=/dev/apps/ref_data
			exomiser.phenotype.data-version=1711
		```
		
		Next, populate the MongoDB connection fields as per your MongoDB installation.
		
	- Now build source files. 
		
		```mvn clean install package```
		
		
	- That should create a directory called "target" with an executable JAR file


	- Start server

		```java -jar target/matchbox-<version>.jar```


* NOTE: if you would like to change the default port the server listens on (8080), you can either set/use the environment 
variable ```SERVER_PORT``` or add the argument ```--server.port``` after the ```java -jar``` incantation. For example
 
     ```export SERVER_PORT=9020```
    
  or
       
    ```java -jar matchbox-0.1.0.jar --server.port=9020```

  It is similarly possible to change any of the variables contained in the application.properties in this manner. The 
  latter is usually a better option as this will be application instance specific rather than as a global system variable. 
  
  