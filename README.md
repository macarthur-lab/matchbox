# beamr
Broad Exchange API for Matchmaker in RDAP

Installation:

1. Clone the repository
git clone https://<username>@github.com/macarthur-lab/beamr.git

2. Build source files (maven is required to be on your system)
mvn package

3. That should create a directory called "target" with an executable JAR file

4. Start server
java -jar target/beam-0.1.0.jar


Test run:

1. The following path,
http://localhost:8080/match

The following headers:
X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d
Accept: application/vnd.ga4gh.matchmaker.v0.1+json
Content-Type: application/x-www-form-urlencoded