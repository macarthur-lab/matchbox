FROM openjdk:8-jdk

RUN apt-get update \
 && apt-get install -y wget \
 && apt-get install -y git \
 && apt-get install -y maven 

MAINTAINER MacArthur Lab

ADD settings.xml /root/.m2/settings.xml
ADD entrypoint.sh  /root/bin/entrypoint.sh

env MVN=mvn

#first get Exomiser built in the local maven for matchbox to import in
#---------------------------------------------------------------------------

RUN git clone -b development https://github.com/exomiser/Exomiser
WORKDIR Exomiser
RUN $MVN clean install package

#now matchbox (and it will see Exomisor in local maven repo)
#---------------------------------------------------------------------------

RUN git clone https://github.com/macarthur-lab/matchbox
WORKDIR matchbox
RUN $MVN -Dmaven.test.skip=true clean install package

env MATCHBOX_JAR=/Exomiser/matchbox/target/matchbox-0.1.0.jar
env MATCHBOX_CONFIG_DIR=/Exomiser/matchbox/config


#Now set matchbox up for deployment and copy over jar and config files
#---------------------------------------------------------------------------
WORKDIR /matchbox_deployment
RUN cp -rf $MATCHBOX_CONFIG_DIR . \
 && cp $MATCHBOX_JAR .


###########################################
#										  #
# Make sure this port number is forwarded #
# when doing docker run -p <port>:<port>  #
# command.								  #
#										  #
###########################################
EXPOSE 9020

###########################################
#										  #
# Environment variables for Mongo 		  #
# connection. Please populate before      #
# doing docker build command			  #
#										  #
# Please also note the EXOMISER_DATA_DIR  #
# value. The file system path with ref	  #
# data (viewable by docker daemon) must   #  
# be mounted to this location in 		  #
# container.							  #
#										  #
###########################################
env EXOMISER_DATA_DIR=/Exomiser/matchbox/data/data
env MONGODB_HOSTNAME=
env MONGODB_PORT=
env MONGODB_USERNAME=
env MONGODB_PASSWORD=

CMD ["/root/bin/entrypoint.sh"]
