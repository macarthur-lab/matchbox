# beamr
Broad Exchange API for Matchmaker in RDAP

## Installation:

* Clone the repository

git clone https://<username>@github.com/macarthur-lab/beamr.git

* Build source files (maven is required to be on your system)

mvn package

* That should create a directory called "target" with an executable JAR file

* Start server

java -jar target/beamr-0.1.0.jar


## Test run

* Use the the following path

http://localhost:8080/match

with the following headers:

X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d

Accept: application/vnd.ga4gh.matchmaker.v0.1+json

Content-Type: application/x-www-form-urlencoded

## Exceution process map

This following describes the typical sequence of events in execution. A addition
of a parent to the matchmaker system starts the following process.

1. A new patient record get's inserted into beamr through a PUT call. This implies "search
in other matchmaker nodes for patients "similar" to this patient.

2. A search get's initiated (via MatchmakerSearch) in every match maker node that is on record 

3. All results are aggregated and sieved through beamr "similarity" tests as well. 

4. Valid matches along with scores are communicated back to the patients submitter.

## Matching criteria

1. Gene matching is considered the primary matching strategie.

2. Phenotype matching is done as a secondary step to help narrow down initial search via genetypes.
