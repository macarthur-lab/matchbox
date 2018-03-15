Regression Tests
================

This system is still under construction and provides a semi-automated testing method. Please use unit tests for now, which are fairly extensive

Before using, please update the line,

	ACCESS_TOKEN="abcd"
	
with the appropriate token in regression_test.py



#Network Testing

##Objective

To start two instance of <i>matchbox</i> and prototype a Matchmaker Exchange (MME) network instance on your machine, so the two instances could mimic a real-time conversation between two instances across the network.

## To do this test, you will need:

1. Docker (https://www.docker.com/)

## The recipe

1. You will need two instances of MongoDB listening on two different ports to accurately mimic two MME nodes running in two distinct locations around the world. Since this is a test and proof of concept, we will use unauthenticated instances. When using a single instance in production, we strongly encourage password protected MongoDB instances.

i.   Create two distinct data directories for the mongod instances
		```
			mkdir data18
			mkdir data19
		```

ii.  Start two mongod instance containers listening on two different ports

		```
			docker run --name mongo18 -d -p 27018:27017 -v data18:/data/db mongo
			docker run --name mongo19 -d -p 27019:27017 -v data19:/data/db mongo
		```
		
		```
			Note: these can be brought down by first,
			docker container kill mongo18
			docker container rm mongo18
			docker container kill mongo19
			docker container rm mongo19
			
			Verify that they are gone,
			docker container ls
		```
		
iii. Make two directories for the two distinct matchbox instances we will start up

		```
			mkdir matchbox18
			mkdir matchbox19
		```

iv.  Clone a <i>matchbox</i> Master branch into each

		```
			git clone https://github.com/macarthur-lab/matchbox matchbox19
			git clone https://github.com/macarthur-lab/matchbox matchbox19
		```
		
		
v.   Update the following in each Dockerfile as below

		```
			env MONGODB_HOSTNAME=localhost
			env MONGODB_PORT=27018
			env MONGODB_USERNAME=
			env MONGODB_PASSWORD=
			env MONGODB_DATABASE=
		```
		
		```
			env MONGODB_HOSTNAME=localhost
			env MONGODB_PORT=27019
			env MONGODB_USERNAME=
			env MONGODB_PASSWORD=
			env MONGODB_DATABASE=
		```
		
		Uncomment following to serve as HTTPS,
		
		```
			env USE_HTTPS=true
			env SERVER_PORT=8443
			env HTTPS_SSL_KEY_STORE=matchbox_keystore
			env HTTPS_SSL_KEY_STORE_PASSWORD=changeit
			env HTTPS_SSL_KEY_PASSWORD=temp_ks_pwd__change_me!
			
			RUN keytool -genkey -noprompt \
								-alias matchbox \
								-dname "CN=, OU=, O=, L=, S=, C=" \
								-keystore $HTTPS_SSL_KEY_STORE \
								-storepass $HTTPS_SSL_KEY_STORE_PASSWORD \
								-keypass $HTTPS_SSL_KEY_PASSWORD
		```

		Build image
		
		```
			docker build -t matchbox-docimg18 .
		```