# <i>matchbox Docker build process (beta)</i>

We are still in the process of designing a best practices build and deployment process using Docker and Kubernetes, 
so please consider this a work in progress. 

Please be careful to not check in Docker files with private secure
usernames and passwords.

## To use this docker build, you will need:

1. Docker (https://www.docker.com/)

2. Reference data for the Exomiser dependency fetched from:
	```
	Please note, this is a ~20G file, that expands into ~50G. In the recent future we will
	reduce this to about ~10G uncompressed with a ~5G compressed file.
	
	https://storage.googleapis.com/seqr-hail/reference_data/exomiser/data.tar.gz
	
	Once you download this file, please uncompress it and remember the file path. For example,
	/reference_data/exomiser-cli-7.2.1/data
	
	Also make sure this directory is accessible to your docker daemon since it needs to be mounted
	via the -v option at docker "run command"
	```
	
	

## Build process:

1. First update the Dockerfile empty fields at the bottom related to your MongoDB instance.
	
	For example:
	
	```
		env MONGODB_HOSTNAME=192.168.1.4
		env MONGODB_PORT=27017
		env MONGODB_USERNAME=username
		env MONGODB_PASSWORD=pwd
	```
	
2. Then, from the matchbox docker directory, do a build (should take 6-10mins max)
	```
		docker build -t matchbox-docimg .
	```
	
3. Assuming,

	* You have already downloaded the necessary reference data for Exomiser (for example to /reference_data/exomiser-cli-7.2.1/data) 
	
	* And it is accessible to Docker daemon, 
	
	* And you have a MongoDB instance running and you have added its credentials and details to the Dockerfile before
the build step, 


4. You should now be able to start matchbox with (for example, using the image "matchbox-docimg" we built ealier),

	```
		docker run -ti -p 9020:9020 -v "/reference_data/exomiser-cli-7.2.1/data":/Exomiser/matchbox/data/data matchbox-docimg 
	``` 


5. You can test your instance with,
	
	```
		curl -X GET -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:9020/patient/view
	```



 