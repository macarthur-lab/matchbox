# beamr
**B**road **E**xchange **A**PI for **M**atchmaker in **R**DAP

Beamr is the Matchmaker Exchange application for the joint Center for Mendelian Genomics. It has been developed at the MacArthur Lab and shared as open source software. The objective of this system is to allow the automated comparison at scale, of patients with rare diseases cared-for in other centers world wide. We hope to facilitate the identification of novel variants via finding common instances of such in these individuals.

## Installation:

In the future, you would be able to either,

* Download the JAR file and simply start the server via [distribution process for this method is not ready yet],

java -jar beamr-0.1.0.jar

* Or download the source code and simply build on your system. You will require maven (https://maven.apache.org) for this. This process is quite easy as well, and is described below and supported as of now.


## Install from source.

* Clone the repository

git clone https://username@github.com/macarthur-lab/beamr.git

* Build source files (maven is required to be on your system)

mvn package

* That should create a directory called "target" with an executable JAR file

* Start server

java -jar target/beamr-0.1.0.jar


## Test run

* Use the the following path

http://localhost:8080/match

* with the following headers:

X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d

Accept: application/vnd.ga4gh.matchmaker.v0.1+json

Content-Type: application/x-www-form-urlencoded

## Exceution process map

The following describes the typical sequence of events in execution. An addition
of a patient to the matchmaker system starts the following process.

1. A new patient record get's inserted into beamr via seqr https://seqr.broadinstitute.org. This action implies "search in other matchmaker nodes for patients 'similar' to this patient".

2. A search get's initiated in every match maker node that is on record 

3. All results are aggregated and sieved through beamr "similarity" tests. 

4. Valid matches along with scores are communicated back to the patients primary contact.

## Matching criteria

1. Gene matching is considered the primary matching strategy.

2. Phenotype matching is done as a secondary step to help narrow down initial search via genotypes.
