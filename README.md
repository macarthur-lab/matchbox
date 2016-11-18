# <i>matchbox</i>
**Broad Institute Matchmaker Exchange** 

<i>matchbox</i> is the Matchmaker Exchange application for the joint Center for Mendelian Genomics. It has been developed at the MacArthur Lab and shared as open source software under the AGPL License. The objective of this system is to allow the automated comparison at scale, of patients with rare diseases cared-for in other centers world wide. We hope to facilitate the identification of novel variants via finding common instances of such in these individuals.

## Requirements:

* Java 1.8

* A MongoDB instance (https://www.mongodb.org/)

* Maven (https://maven.apache.org/) if you wish to build from source (only option supported as of now)  

## Installation:

In the future, you would be able to either,

* Download the JAR file and simply start the server via [distribution process for this method is not ready yet],

java -jar matchbox-0.1.0.jar

NOTE: if you would like to change the default port the server listens on (8080), please set/use the environment variable SERVER_PORT

for example,
export SERVER_PORT=9020

* Or download the source code and simply build on your system. You will require maven (https://maven.apache.org) for this. This process is quite easy as well, and is described below and supported as of now.


## Install from source.

* Clone the repository

git clone https://username@github.com/macarthur-lab/beamr.git

* Build source files (maven is required to be on your system)

mvn clean package

* That should create a directory called "target" with an executable JAR file

* Start server

java -jar target/matchbox-0.1.0.jar


## Test run

* Use the the following path

http://localhost:8080/match

* with the following headers:

X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d

Accept: application/vnd.ga4gh.matchmaker.v0.1+json

Content-Type: application/x-www-form-urlencoded

## Execution process map

The following describes the typical sequence of events in execution. An addition
of a patient to the matchmaker system starts the following process.

1. A new patient record get's inserted into matchbox via seqr https://seqr.broadinstitute.org. This action implies "search in other matchmaker nodes for patients 'similar' to this patient".

2. A search get's initiated in every match maker node that is on record 

3. All results are aggregated and sieved through matchbox "similarity" tests. 

4. Valid matches along with scores are communicated back to the patients primary contact.

## Matching criteria

1. Gene matching is the primary matching strategy. (if 2 individuals have at least 1 gene in common, it is considered a match)

2. Phenotype matching is done as a secondary step to help narrow down initial search via genotypes.(not implemented yet)

## Data model notes
* A database named "mme_primary" will be created in your localhost MongoDB instance. If you wish to use a different host name or different database name please update class as needed,

org.broadinstitute.macarthurlab.matchbox.datamodel.mongodb.MongoDBConfiguration


## Adding a new matchmaker node

1. To the config.xml file found at the top level of the application directory, add the following lines,

```

  <bean id="ANameToGiveThisNodeRepresentationInCode"
      class="org.broadinstitute.macarthurlab.matchbox.matchmakerNodes.MatchmakerNode">
      <constructor-arg type="java.lang.String" value="Some name" />
      <constructor-arg type="java.lang.String" value="The authentication token" />
      <constructor-arg type="java.lang.String" value="The URI" />
  </bean>
  
```

  
  For example:
```  

  <bean id="ANameToGiveThisNode"
      class="org.broadinstitute.macarthurlab.matchbox.matchmakerNodes.MatchmakerNode">
      <constructor-arg type="java.lang.String" value="Test Reference Server" />
      <constructor-arg type="java.lang.String" value="854a439d278df4283bf5498ab020336cdc416a7d" />
      <constructor-arg type="java.lang.String" value="http://localhost:8090" />
  </bean>
  
```

then add it to this list,
```

  <bean id="matchmakerSearch"
      class="org.broadinstitute.macarthurlab.matchbox.matchmakerNodes.MatchmakerSearch">
      <property name="matchmakerNodes">
         <list>
            <ref bean="phenomeCentralMatchmakerNode"/>
            <ref bean="testRefSvrNode"/>
         </list>
      </property>
  </bean>

```

## To do
*  More tests!

*  Even better exception handling

*  Metrics gathering for quality control



## Examples

*  **View all individuals in matchbox**(eventually this will be a privileged branch with limited access)

API endpoint (GET):  individual/view

curl -X GET -H "X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d" -H "Accept: application/vnd.ga4gh.matchmaker.v0.1+json" -H "Content-Type: application/x-www-form-urlencoded" http://maclab-utils:8080/individual/view

Result would look something like:


[{"id":"id_ttn-2","label":"identifier","contact":{"institution":"Contact Institution","name":"Full Name","href":"URL"},"species":"NCBI_taxon_identifier","sex":"FEMALE","ageOfOnset":"HPOcode","inheritanceMode":"HPOcode","disorders":[{"id":"Orphanet:#####"}],"features":[{"id":"HPOcode","observed":"yes","ageOfOnset":"HPOcode"},{"id":"HPOcode2","observed":"yes2","ageOfOnset":"HPOcode2"}],"genomicFeatures":[{"gene":{"id":"TTN"},"variant":{"assembly":"NCBI36","referenceName":"1","start":12,"end":24,"referenceBases":"A","alternateBases":"A"},"zygosity":1,"type":{"id":"SOcode","label":"STOPGAIN"}}]}]



*  **Add a patient to matchbox** (eventually this will be a privileged branch with limited access)


API endpoint (POST):  individual/add

curl -X POST -H "X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d" -H "Accept: application/vnd.ga4gh.matchmaker.v0.1+json" -H "Content-Type: application/x-www-form-urlencoded" http://maclab-utils:8080/individual/add -d '{"patient" : {"id" : "id_ttn-8","label" : "identifier","contact" : {"name" : "Full Name","institution" : "Contact Institution","href" : "URL"},"species" : "NCBI_taxon_identifier","sex" : "FEMALE","ageOfOnset" : "HPOcode","inheritanceMode" : "HPOcode","disorders" : [{"id" : "Orphanet:#####"}],"features" : [{"id" : "HPOcode","observed" : "yes","ageOfOnset" : "HPOcode"},{"id" : "HPOcode2","observed" : "yes2","ageOfOnset" : "HPOcode2"}],"genomicFeatures" : [{"gene" : {"id" : "TTN"},"variant" : {"assembly" : "NCBI36","referenceName" : "1","start" : 12,"end" : 24,"referenceBases" : "A","alternateBases" : "A"},"zygosity" : 1,"type" : {"id" : "SOcode","label" : "STOPGAIN"}}]}}'

Result would look something like:

{"message":"insertion OK"}


*  **Find a match for a patient in other Matchmaker nodes**  (not privileged, accessible to everybody with a token)

API endpoint (POST):  individual/match

--NOT IMPLEMENTED YET: AWAITAING AUTH TOKEN GENERATION WITH OTHER CENTERS


*  **Find a match in local matchbox data model** (look for matches ONLY in local beamer database of patients)  (eventually this will be a privileged branch with limited access)

API endpoint (as per matchmaker specification and this would be the target endpoint for external matchmakerNodes looking for matches at Broad (POST):  /match


curl -X POST -H "X-Auth-Token: 854a439d278df4283bf5498ab020336cdc416a7d" -H "Accept: application/vnd.ga4gh.matchmaker.v0.1+json" -H "Content-Type: application/x-www-form-urlencoded" http://maclab-utils:8080/match -d '{"patient" : {"id" : "id_ttn-8","label" : "identifier","contact" : {"name" : "Full Name","institution" : "Contact Institution","href" : "URL"},"species" : "NCBI_taxon_identifier","sex" : "FEMALE","ageOfOnset" : "HPOcode","inheritanceMode" : "HPOcode","disorders" : [{"id" : "Orphanet:#####"}],"features" : [{"id" : "HPOcode","observed" : "yes","ageOfOnset" : "HPOcode"},{"id" : "HPOcode2","observed" : "yes2","ageOfOnset" : "HPOcode2"}],"genomicFeatures" : [{"gene" : {"id" : "TTN"},"variant" : {"assembly" : "NCBI36","referenceName" : "1","start" : 12,"end" : 24,"referenceBases" : "A","alternateBases" : "A"},"zygosity" : 1,"type" : {"id" : "SOcode","label" : "STOPGAIN"}}]}}'


Result would look something like:

{"results":[{"score":{},"patient":{"id":"id_ttn-2","label":"identifier","contact":{"institution":"Contact Institution","name":"Full Name","href":"URL"},"species":"NCBI_taxon_identifier","sex":"FEMALE","ageOfOnset":"HPOcode","inheritanceMode":"HPOcode","disorders":[{"id":"Orphanet:#####"}],"features":[{"id":"HPOcode","observed":"yes","ageOfOnset":"HPOcode"},{"id":"HPOcode2","observed":"yes2","ageOfOnset":"HPOcode2"}],"genomicFeatures":[{"gene":{"id":"TTN"},"variant":{"assembly":"NCBI36","referenceName":"1","start":12,"end":24,"referenceBases":"A","alternateBases":"A"},"zygosity":1,"type":{"id":"SOcode","label":"STOPGAIN"}}]}},{"score":{},"patient":{"id":"id_ttn-4","label":"identifier","contact":{"institution":"Contact Institution","name":"Full Name","href":"URL"},"species":"NCBI_taxon_identifier","sex":"FEMALE","ageOfOnset":"HPOcode","inheritanceMode":"HPOcode","disorders":[{"id":"Orphanet:#####"}],"features":[{"id":"HPOcode","observed":"yes","ageOfOnset":"HPOcode"},{"id":"HPOcode2","observed":"yes2","ageOfOnset":"HPOcode2"}],"genomicFeatures":[{"gene":{"id":"TTN"},"variant":{"assembly":"NCBI36","referenceName":"1","start":12,"end":24,"referenceBases":"A","alternateBases":"A"},"zygosity":1,"type":{"id":"SOcode","label":"STOPGAIN"}}]}},{"score":{},"patient":{"id":"id_ttn-8","label":"identifier","contact":{"institution":"Contact Institution","name":"Full Name","href":"URL"},"species":"NCBI_taxon_identifier","sex":"FEMALE","ageOfOnset":"HPOcode","inheritanceMode":"HPOcode","disorders":[{"id":"Orphanet:#####"}],"features":[{"id":"HPOcode","observed":"yes","ageOfOnset":"HPOcode"},{"id":"HPOcode2","observed":"yes2","ageOfOnset":"HPOcode2"}],"genomicFeatures":[{"gene":{"id":"TTN"},"variant":{"assembly":"NCBI36","referenceName":"1","start":12,"end":24,"referenceBases":"A","alternateBases":"A"},"zygosity":1,"type":{"id":"SOcode","label":"STOPGAIN"}}]}}]}
