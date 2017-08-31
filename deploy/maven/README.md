## Build directly via Maven:

<i>matchbox</i> relies on a modified version of Exomiser (https://github.com/exomiser/Exomiser), its reference data and libraries for the phenotype matching algorithm. Given that these libraries are currently not in a maven repository, we obtain this dependency via first building Exomiser and then followed by <i>matchbox</i>.  By building Exomiser first, we put its jars in the local maven repository, where the <i>matchbox</i> build is able to see and use them.

* Clone the Exomiser package
	- Make sure you have a settings.xml file in your ~/.m2/ directory with the following entry (to activate a local repsitory for Maven to use)
	
		```
			<settings>
    			<localRepository>${user.home}/.m3/repository</localRepository>
			</settings>
		```


	- Clone the repository

		```git clone -b development https://github.com/exomiser/Exomiser```

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
			wget https://storage.googleapis.com/pub/gsutil.tar.gz
			
			tar -xvzf gsutil.tar.gz
		```
		
		Provide the path of the above untar'ed "data" directory to the "exomiser.data-directory=" field, for example,
		```
			exomiser.data-directory==/Users/john/Documents/exomiser-cli-8.0.0/data
		```
		
		A full example would look like,
		

		```
		spring.application.name=matchbox
		logging.file=logs/matchbox.log
		
		spring.http.encoding.force=false
		
		#Enable these as required for any specific MongoDB setup.
		#spring.data.mongodb.host=
		#spring.data.mongodb.port=
		#spring.data.mongodb.username=
		#spring.data.mongodb.password=
		#spring.data.mongodb.database=mme_primary
		
		#Enable the following to be HTTPS (REQUIRED by MME if server is not proxied)
		#thanks to https://www.drissamri.be/blog/java/enable-https-in-spring-boot/
		#keyTrustStore=<your_KeyStore.jks>
		#server.port=8443
		#server.ssl.key-store=file:<path-to-JKS-file>
		#server.ssl.key-store-password=<your-password>
		#server.ssl.key-password=<you-jks-domain>
		
		matchbox.gene-symbol-to-id-mappings=${user.dir}/config/gene_symbol_to_ensembl_id_map.txt
		matchbox.connected-nodes=${user.dir}/config/nodes.json
		
		exomiser.data-directory=

        #IF YOU WANT TO ALLOW PHENOTYPE ONLY MATCHES WHERE THERE WAS NO GENE IN COMMON
		allow.no-gene-in-common.matches=false
		```
		
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
  
  