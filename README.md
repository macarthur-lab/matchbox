# <i>matchbox</i>


<i>matchbox</i> was originally developed at the MacArthur Lab for the Broad Center for Mendelian Genomics. Its purpose was to function as the primary connection point to the Matchmaker Exchange. It was then shared as open source software under the BSD License. 

A major challenge faced by rare disease investigators is the difficulty of finding more than one individual with the same genetic disorder. This complicates the identification of causal variants and novel gene discovery. The Matchmaker Exchange (MME) provides a decentralized federated network of genomic centers with collections of rare disease cases. MME allows you to find similar individuals based on genotype, phenotype -and soon other types of data-, globally and at scale. It allows members to host data locally and reduce data ownership challenges  as well have more control of sharing preferences and matching algorithms. Its service oriented architecture allows member centers to keep existing infrastructure. It has gained international support via the GA4GH and currently has many members spanning multiple continents.

A significant amount of development is typically required to join the MME; this has a detrimental effect on network growth. To address this and facilitate growth, we developed <i>matchbox</i> to be completely portable and easily usable in any center wishing to join the MME.



## Requirements:

* Java 1.8

* A MongoDB instance (available from https://www.mongodb.org/)

* Maven 3.1 (available from https://maven.apache.org/)   

## Installation:

* Download the source code and simply build on your system via.

	- Clone the repository

		git clone https://github.com/macarthur-lab/matchbox

	- Build source files (maven is required to be on your system)

		mvn clean install package

	- That should create a directory called "target" with an executable JAR file

	- Start server

		java -jar target/matchbox-<version>.jar


* NOTE: if you would like to change the default port the server listens on (8080), please set/use the environment variable SERVER_PORT

		for example,
		export SERVER_PORT=9020




## Test run

* Use the the following path

http://localhost:8080/match

* with the following headers:

	X-Auth-Token: abcd

	Accept: application/vnd.ga4gh.matchmaker.v1.0+json

	Content-Type: application/x-www-form-urlencoded

## Execution process map

* Patients (one at a time) can be added to the matchmaker system via:

	/patient/add
	
* You can view all patients in the system with:

	/patient/view
	
* You can match a patient, with all other patients ONLY IN the matchbox database with a POST containing query patient JSON to:

	/match

* You can match a patient, with all other patients ONLY IN the Matchmaker network (EXCLUDING matchbox database). The nodes that it will query against are specified in the config.xml file found in the resources directory at the application root. To make the query, make a POST containing patient JSON to:

	/match/external
	
* The correct JSON format a query patient should be described in can be found at:

	https://github.com/ga4gh/mme-apis/blob/master/search-api.md



## Matching criteria

* Gene based matching is the current primary matching strategy. (if 2 individuals have at least 1 gene in common, it is considered a match). 

* Phenotype matching is done as a secondary step to help narrow down initial search via genotypes.(not implemented yet). Though we are in the process of adding in phenotype-only based matching in the absence of genotype information.

## Data model notes

* A database named "mme_primary" will be created in your localhost MongoDB instance. If you wish to use a different host name or different database name please update the application.properties file in the resources directory as needed,



## Adding a new matchmaker node to search in:

* To the config.xml file found at the top level of the application in the resources directory, add the following lines,

```

  <bean id="testRefSvrNode"
      class="org.broadinstitute.macarthurlab.matchbox.entities.MatchmakerNode">
      <constructor-arg type="java.lang.String" value="A name" />
      <constructor-arg type="java.lang.String" value="token" />
      <constructor-arg type="java.lang.String" value="http://localhost:8090/match" />
      <constructor-arg type="java.lang.String" value="application/vnd.ga4gh.matchmaker.v1.0+json"/>
      <constructor-arg type="java.lang.String" value="application/vnd.ga4gh.matchmaker.v1.0+json"/>
      <constructor-arg type="java.lang.String" value="en-US"/>
      <constructor-arg type="boolean" value="false"/>
  </bean>

  <bean id="matchmakerSearch"
      class="org.broadinstitute.macarthurlab.matchbox.matchmakers.MatchmakerSearch">
      <property name="matchmakers">
         <list>
         	<ref bean="testRefSvrNode"/> 
         </list>
      </property>
  </bean>

  
```

## Adding a token to give a user access to matchbox:


```
  <bean id="defaultAccessToken"
      class="org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken">
      <constructor-arg type="java.lang.String" value="Default Access Token" />
      <constructor-arg type="java.lang.String" value="abcd" />
      <constructor-arg type="java.lang.String" value="Local Center name" />
      <constructor-arg type="java.lang.String" value="user@center.org" />
  </bean>
  
  <bean id="accessAuthorizedNode"
      class="org.broadinstitute.macarthurlab.matchbox.authentication.AccessAuthorizedNode">
      <property name="accessAuthorizedNodes">
         <list>
            <ref bean="defaultAccessToken"/>            
         </list>
      </property>
  </bean>

```

## Examples

*  View all individuals in matchbox(eventually this will be a privileged branch with limited access)

API endpoint (GET):  patient/view

	curl -X GET -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/patient/view



* Add a patient to matchbox 


API endpoint (POST):  patient/add

	curl -X POST -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/patient/add -d '{"patient" : {"id" : "id_ttn-8","label" : "identifier","contact" : {"name" : "Full Name","institution" : "Contact Institution","href" : "URL"},"species" : "NCBI_taxon_identifier","sex" : "FEMALE","ageOfOnset" : "HPOcode","inheritanceMode" : "HPOcode","disorders" : [{"id" : "Orphanet:#####"}],"features" : [{"id" : "HPOcode","observed" : "yes","ageOfOnset" : "HPOcode"},{"id" : "HPOcode2","observed" : "yes2","ageOfOnset" : "HPOcode2"}],"genomicFeatures" : [{"gene" : {"id" : "TTN"},"variant" : {"assembly" : "NCBI36","referenceName" : "1","start" : 12,"end" : 24,"referenceBases" : "A","alternateBases" : "A"},"zygosity" : 1,"type" : {"id" : "SOcode","label" : "STOPGAIN"}}]}}'

A successful result would look something like:

{"message":"insertion OK"}


*  Find a match for a patient in other Matchmaker nodes ONLY

API endpoint (POST):  /match/external


*  Find a match in local matchbox data model ONLY

API endpoint (as per matchmaker specification and this would be the target endpoint for external matchmakers looking for matches at in local DB:  /match


	curl -X POST -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost/match -d '{"patient" : {"id" : "id_ttn-8","label" : "identifier","contact" : {"name" : "Full Name","institution" : "Contact Institution","href" : "URL"},"species" : "NCBI_taxon_identifier","sex" : "FEMALE","ageOfOnset" : "HPOcode","inheritanceMode" : "HPOcode","disorders" : [{"id" : "Orphanet:#####"}],"features" : [{"id" : "HPOcode","observed" : "yes","ageOfOnset" : "HPOcode"},{"id" : "HPOcode2","observed" : "yes2","ageOfOnset" : "HPOcode2"}],"genomicFeatures" : [{"gene" : {"id" : "TTN"},"variant" : {"assembly" : "NCBI36","referenceName" : "1","start" : 12,"end" : 24,"referenceBases" : "A","alternateBases" : "A"},"zygosity" : 1,"type" : {"id" : "SOcode","label" : "STOPGAIN"}}]}}'



