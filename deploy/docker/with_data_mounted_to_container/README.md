# <i>matchbox Docker build process (beta)</i>

We are still in the process of designing a best practices build and deployment process using Docker and Kubernetes, so please consider this a work in progress. 

Please be careful to not check in Docker files with private secure
usernames, passwords, and tokens etc. 

Please also remember to change any default passwords built into system before production!

## To use this docker build, you will need:

1. Docker (https://www.docker.com/)

2. Reference data for the Exomiser dependency fetched from:
	```
	Please note, this is a large file
	
	wget https://storage.googleapis.com/seqr-reference-data/1711_phenotype.tar.gz
	
	Once you download this file, please uncompress it, and remember the file path. For example,
	/data/reference_data/
	
	Also make sure this directory is accessible to your docker daemon since it needs to be mounted
	via the -v option at docker "run command"
	```

3. An authenticated (ideally) MongoDB instance (https://www.mongodb.com/)


## Build process:

1. First update the Dockerfile empty fields at the bottom,
	
	For example:
	
	```
		env MONGODB_HOSTNAME=192.168.1.4
		env MONGODB_PORT=27017
		env MONGODB_USERNAME=username
		env MONGODB_PASSWORD=pwd
		env MONGODB_DATABASE=mme_primary
	```
	
	If you want to serve as HTTPS, please uncomment following by removing "#" and populate as needed. You can ignore otherwise
	
	```
	env USE_HTTPS=true
	env SERVER_PORT=8443
	env HTTPS_SSL_KEY_STORE=matchbox_keystore
	env HTTPS_SSL_KEY_STORE_PASSWORD=changeit
	env HTTPS_SSL_KEY_PASSWORD=<temp_ks_pwd__change_me!>
	
	RUN keytool -genkey -noprompt \
						-alias matchbox \
						-dname "CN=, OU=, O=, L=, S=, C=" \
						-keystore $HTTPS_SSL_KEY_STORE \
						-storepass $HTTPS_SSL_KEY_STORE_PASSWORD \
						-keypass $HTTPS_SSL_KEY_PASSWORD
	```

	
2. In the deploy/docker directory there are two files that should be handled extra carefully in production given that they will contain tokens and access information for your instance and other nodes.
	```
	config.xml : this XML file is used to configure the token to give access to your matchbox instance. 
	nodes,json : this JSON file contains tokens that give your matchbox instance access to other MME nodes
	``` 
	
	Using guidance from the example data inside them, populate as needed. 
	
	Please remember to remove default values before production!
	
	Possibly use a secrets-file management system to keep fully populate files that can inserted in at deployment.
	
	
3. Then, from the matchbox docker directory, do a build (should take 6-10mins max)
	```
		docker build -t matchbox-docimg .
	```
	
4. Assuming,

	* You have already downloaded the necessary reference data for Exomiser (for example to /data/reference_data/) 
	
	* And reference data directory is accessible to Docker daemon, 
	
	* And you have a MongoDB instance running and you have added its credentials and details to the Dockerfile before the build step, 


	For example, if you are using the default HTTP settings and didn't change any port numbers:
	```
		docker run -ti -p 9020:9020 -v "/data/reference_data/":/Exomiser/matchbox/data/data matchbox-docimg 
	``` 
	
	OR
	
	For example, if you uncommented the HTTPS settings and didn't change any HTTPS port numbers:
	```
		docker run -ti -p 8443:8443 -v "/reference_data/exomiser-cli/data":/Exomiser/matchbox/data/data matchbox-docimg 
	``` 


6. You can test your instance with (make sure to adjust the URL "http://localhost:9020/patient/view" with the port you used),
	
	```
		curl -X GET -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:9020/patient/view
	```



 