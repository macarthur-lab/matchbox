<img width="200" src="https://raw.githubusercontent.com/macarthur-lab/matchbox/dev/aux-files/Matchbox-logo_RGB.png">

# <i>matchbox</i>

<i>matchbox</i> was originally developed at the MacArthur Lab for the Broad Center for Mendelian Genomics. Its purpose was to function as a bridge to the Matchmaker Exchange. It was then shared as open source software under the BSD License. 

A major challenge faced by rare disease investigators is the difficulty of finding more than one individual with the same genetic disorder. This complicates the identification of causal variants and novel gene discovery. The Matchmaker Exchange (MME) provides a decentralized federated network of genomic centers with collections of rare disease cases. MME allows you to find similar individuals based on genotype, phenotype -and soon other types of data-, globally and at scale. 

Some important characteristics of the MME as relates to data are:

* It allows members to host data locally and reduce data ownership challenges
* Allows you to have more control of sharing preferences and matching algorithms. 
* Its service oriented architecture allows member centers to keep existing infrastructure. 

MME has gained international support via the GA4GH and currently has many members spanning multiple continents. If you are interested in joining the Matchmaker Exchange, please contact us at matchmaker@broadinstitute.org and we will be happy to help you. More information on Matchmaker Exchange can also be found at http://www.matchmakerexchange.org/

A significant amount of development is typically required to join the MME; this has a detrimental effect on network growth. To address this and facilitate growth, we developed <i>matchbox</i> to be completely portable and easily usable in any center wishing to join the MME.

## To use <i>matchbox</i>, you will need:

* Java 1.8

* Maven 3.1 (available from https://maven.apache.org/)   

* An authenticated MongoDB instance (available from https://www.mongodb.org/). This application requires a password protected MongoDB instance for tests and build to succeed (you can build without tests and MongoDB and configure MongoDB later if required as described below).

## Build:

You can build <i>matchbox</i> via two methods. 

1. Directly through maven. [Detailed maven build instructions](deploy/maven/README.md)

OR

2. Using [Docker](https://www.docker.com/). [Detailed Docker build instructions](deploy/docker/README.md)


## General overview:

* Typically you would use something like the following path

	```http://localhost:8080/match```

* Along with the following headers (we are using "abcd" as an example token, please change before production!"):

	```
	X-Auth-Token: abcd
	Accept: application/vnd.ga4gh.matchmaker.v1.0+json
	Content-Type: application/x-www-form-urlencoded
	```

* And a JSON payload when a POST is required. (complete examples below)

## List of API endpoints

* Patients (one at a time) can be added to the matchmaker system via:

	```/patient/add```

* Patients (one at a time) can be deleted from the system via a DELETE to:

	```/patient/delete```
	
	with payload : ```{"id":"id_to_delete"}```
		
* You can view all patients in the system with (GET):

	```/patient/view```
	
* You can match a patient, with all other patients ONLY IN the matchbox database with a POST containing query patient JSON to:

	```/match```

* You can match a patient, with all other patients ONLY IN the Matchmaker network (EXCLUDING matchbox database). The nodes that it will query against are specified in the config.xml file found in the resources directory at the application root. To make the query, make a POST containing patient JSON to:

	```/match/external```
	
* The correct JSON format a query patient should be described in can be found at:

	````https://github.com/ga4gh/mme-apis/blob/master/search-api.md````



## Matching criteria

* Gene based matching is the current primary matching strategy. (if 2 individuals have at least 1 gene in common, it is considered a match). We will then evaluate the similarity of 
	* Zygosity
	* Variant type using SO codes impact HIGH. We are using the following codes. For now, can be changed in the org.broadinstitute.macarthurlab.matchbox.match.GenotypeSimilarity class and we will soon abstract this out to application.properties for easier modification.
	```
		SO:1000182
		SO:0001624
		SO:0001572
		SO:0001909
		SO:0001910
		SO:0001589
		SO:0001908
		SO:0001906
		SO:0001583
		SO:1000005
		SO:0002012
		SO:0002012
		SO:0002012
		SO:0001619
		SO:0001575
		SO:0001619	
	```
	* We will soon also integrate disorder, and variant position to further improve this matching strategy.

* Phenotype matching is done as a secondary step to help narrow down initial search via genotypes.

* If the matched result patient has the same ID as the query patient, it won't be sent back. In these cases it is assumed that the result and the query -for some reason- are the same patient.

## Data model notes

* A database named "mme_primary" will be created in your localhost MongoDB instance. If you wish to use a different host name or different database name please update the application.properties file in the resources directory as needed. You can add your password and username in that file as well.



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

## Adding a token to give an external user/node access to <i>matchbox</i>:

You can use the top-level config/config.xml file for this purpose. For example,

This describes a node,
```
  <bean id="defaultAccessToken"
      class="org.broadinstitute.macarthurlab.matchbox.entities.AuthorizedToken">
      <constructor-arg type="java.lang.String" value="Default Access Token" />
      <constructor-arg type="java.lang.String" value="abcd" />
      <constructor-arg type="java.lang.String" value="Local Center name" />
      <constructor-arg type="java.lang.String" value="user@center.org" />
  </bean>
```

And the following adds it to the list of nodes,
```
  <bean id="accessAuthorizedNode"
      class="org.broadinstitute.macarthurlab.matchbox.authentication.AccessAuthorizedNode">
      <property name="accessAuthorizedNodes">
         <list>
            <ref bean="defaultAccessToken"/>            
         </list>
      </property>
  </bean>
```


A complete example would be,


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

## Adding a token to give an external user/node access to <i>matchbox</i>:

You can use the top-level config/nodes.json file to give external nodes access to <i>matchbox</i>. For example,

```
{
	"nodes":[{
		"name": "test-ref-server",
		"token" : "abcd",
		"url" : "https://localhost:8443/match",
		"contentTypeHeader" : "application/vnd.ga4gh.matchmaker.v1.0+json",
		"contentLanguage" : "en-US",
		"acceptHeader" : "application/vnd.ga4gh.matchmaker.v1.0+json",
		"selfSignedCertificate": true
		}]
}
```

The "nodes" object here is a list of such nodes. You can add any number of nodes ({..}) to this list and followed by a server restart for <i>matchbox</i> to start giving them access.


## Recommended deployment architecture

We recommend matchbox be deployed behind a fire-wall. The front-end website would communicate with its back-end. That back-end would communicate with matchbox via a privileged port. That port would be the only port opened on the machine matchbox would live on. This would provide its data maximum security layers.

Further we recommend that precautions be taken to avoid commiting to github the config.xml file that contains your tokens. We use a separate private github repository (or ideally a secure file system location or volt) to maintain the completed config.xml file.

## User interface

At Broad we have integrated <i>matchbox</i> into the <i>seqr</i> open-source web application (https://seqr.broadinstitute.org/). The method with which we did this can be observed in the <i>seqr</i> source code at https://github.com/macarthur-lab/seqr.

<i>seqr</i> is a web application that stores variant and phenotype information on patients. Functionality has been added to it such that subsets of information can be grabbed from it and formatted into the matchmaker JSON format and inserted into <i>matchbox</i>, as well has pages that allow users to search in Matchmaker easily via <i>matchbox</i>.

While commandline tools such as cURL can be used with <i>matchbox</i>, an user interface such as <i>seqr</i> (freely available) does make using it very easy.

## Testing

There are unit tests included that can be executed via Maven. To execute the unit tests,

	mvn test


## Adding in access and connecting to other nodes

	
* You can update resources/config.xml with your connections. But for initial test, we can use the default client connection with token "abcd" to connect into. We won't search external databases yet, since that involves getting tokens from other centers.

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
 

* Start the server

	```
	java -jar target/matchbox-version.jar
	```
	
* Insert a test patient with a cURL command to the API

	An example MINIMUM Patient structure would look like,
	```
	{
	  "patient" : {
	    "id" : "1",
	    "contact" : {
	      "name" : "Test Contact",
	      "href" : "test@test.com"
	    },
	    "features" : [
	      {
	        "id" : "HP:0000118",
	        "observed" : "yes"
	      }
	    ],
	    "genomicFeatures" : [
	      {
	        "gene" : {
	          "id" : "ENSG00000128573"
	        }
	      }
	    ]
	  }
	}
	```
	
	An example CURL would be,
	
	```
	curl -X POST -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/patient/add -d '{"patient" : {"id" : "1","contact" : {"name" : "Test Contact","href" : "test@test.com"},"features":[{"id" : "HP:0000118","observed" : "yes"}],"genomicFeatures":[{"gene" : {"id" : "ENSG00000128573"}}]}}'
	```
	
	A successful result would be,
	
	```
    {"message":"insertion OK","status_code":200}
    ```
	
* To view all contents of matchbox (this endpoint is work-in-progress and the JSON needs further formatting)

	```
	curl -X GET -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/patient/view
	```
	
	The result would look something like,
	```
	[{
	"id": "1",
	"label": "",
	"contact": {
		"institution": null,
		"name": "Test Contact",
		"href": "test@test.com"
	},
	"species": "",
	"sex": "",
	"ageOfOnset": "",
	"inheritanceMode": "",
	"disorders": [],
	"features": [{
		"id": "HP:0000118",
		"observed": "yes",
		"ageOfOnset": "",
		"emptyFieldsRemovedJson": "{\"id\":\"HP:0000118\",\"observed\":\"yes\"}"
	}],
	"genomicFeatures": [{
		"gene": {
			"id": "ENSG00000128573"
		},
		"variant": {
			"assembly": "",
			"referenceName": "",
			"start": -1,
			"end": -1,
			"referenceBases": "",
			"alternateBases": "",
			"emptyFieldsRemovedJson": "{}",
			"unPopulated": true
		},
		"zygosity": -1,
		"type": {
			"id": "",
			"label": ""
		},
		"emptyFieldsRemovedJson": "{\"gene\":{\"id\":\"ENSG00000128573\"}}"
	}],
	"emptyFieldsRemovedJson": "{\"id\":\"1\",\"contact\":{\"name\":\"Test Contact\",\"href\":\"test@test.com\"},\"features\":[{\"id\":\"HP:0000118\",\"observed\":\"yes\"}],\"genomicFeatures\":[{\"gene\":{\"id\":\"ENSG00000128573\"}}],\"_disclaimer\":\"The data in Matchmaker Exchange is provided for research use only. Broad Institute provides the data in Matchmaker Exchange 'as is'. Broad Institute makes no representations or warranties of any kind concerning the data, express or implied, including without limitation, warranties of merchantability, fitness for a particular purpose, noninfringement, or the absence of latent or other defects, whether or not discoverable. Broad will not be liable to the user or any third parties claiming through user, for any loss or damage suffered through the use of Matchmaker Exchange. In no event shall Broad Institute or its respective directors, officers, employees, affiliated investigators and affiliates be liable for indirect, special, incidental or consequential damages or injury to property and lost profits, regardless of whether the foregoing have been advised, shall have other reason to know, or in fact shall know of the possibility of the foregoing. Prior to using Broad Institute data in a publication, the user will contact the owner of the matching dataset to assess the integrity of the match. If the match is validated, the user will offer appropriate recognition of the data owner's contribution, in accordance with academic standards and custom. Proper acknowledgment shall be made for the contributions of a party to such results being published or otherwise disclosed, which may include co-authorship. If Broad Institute contributes to the results being published, the authors must acknowledge Broad Institute using the following wording: 'This study makes use of data shared through the Broad Institute matchbox repository. Funding for the Broad Institute was provided in part by National Institutes of Health grant UM1 HG008900 to Daniel MacArthur and Heidi Rehm.' User will not attempt to use the data or Matchmaker Exchange to establish the individual identities of any of the subjects from whom the data were obtained. This applies to matches made within Broad Institute or with any other database included in the Matchmaker Exchange. \"}"
	}]
	```
	
	
* To do a match of patients inside matchbox we can use the /match endpoint. For our example, we can use the patient we just inserted, except changing the ID to be different. matchbox doesn't not send back results that have the same ID as the incoming query. It assumes those cases are the same individual.

	An example patient JSON structure would be,
	```
	{
	"patient": {
		"id": "2",
		"contact": {
			"name": "Test Contact",
			"href": "test@test.com"
		},
		"features": [{
			"id": "HP:0000118",
			"observed": "yes"
		}],
		"genomicFeatures": [{
			"gene": {
				"id": "ENSG00000128573"
			}
		}]
	}
	}
	```
	
	A cURL would look like,
	
	```
	curl -X POST -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/match -d '{"patient" : {"id" : "2","contact" : {"name" : "Test Contact","href" : "test@test.com"},"features":[{"id" : "HP:0000118","observed" : "yes"}],"genomicFeatures":[{"gene" : {"id" : "ENSG00000128573"}}]}}'
	```
	
	The result would look like. The score of 1.0 represents a perfect match.
	```
	{
	"results": [{
		"score": {
			"patient": 1.0
		},
		"patient": {
			"id": "1",
			"contact": {
				"name": "Test Contact",
				"href": "test@test.com"
			},
			"features": [{
				"id": "HP:0000118",
				"observed": "yes"
			}],
			"genomicFeatures": [{
				"gene": {
					"id": "ENSG00000128573"
				}
			}],
			"_disclaimer": "The data in Matchmaker Exchange is provided for research use only. Broad Institute provides the data in Matchmaker Exchange 'as is'. Broad Institute makes no representations or warranties of any kind concerning the data, express or implied, including without limitation, warranties of merchantability, fitness for a particular purpose, noninfringement, or the absence of latent or other defects, whether or not discoverable. Broad will not be liable to the user or any third parties claiming through user, for any loss or damage suffered through the use of Matchmaker Exchange. In no event shall Broad Institute or its respective directors, officers, employees, affiliated investigators and affiliates be liable for indirect, special, incidental or consequential damages or injury to property and lost profits, regardless of whether the foregoing have been advised, shall have other reason to know, or in fact shall know of the possibility of the foregoing. Prior to using Broad Institute data in a publication, the user will contact the owner of the matching dataset to assess the integrity of the match. If the match is validated, the user will offer appropriate recognition of the data owner's contribution, in accordance with academic standards and custom. Proper acknowledgment shall be made for the contributions of a party to such results being published or otherwise disclosed, which may include co-authorship. If Broad Institute contributes to the results being published, the authors must acknowledge Broad Institute using the following wording: 'This study makes use of data shared through the Broad Institute matchbox repository. Funding for the Broad Institute was provided in part by National Institutes of Health grant UM1 HG008900 to Daniel MacArthur and Heidi Rehm.' User will not attempt to use the data or Matchmaker Exchange to establish the individual identities of any of the subjects from whom the data were obtained. This applies to matches made within Broad Institute or with any other database included in the Matchmaker Exchange. "
		}
	}]
	}
	```

* To delete a patient, you would need to know the ID of it (retrieved by the /patient/view endpoint)

	```
	curl -X DELETE -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/patient/delete -d '{"id":"1"}'
	```
	
	The result would look like,
	```
	{"message":"deleted 1 patient.","status_code":200"}
	```
	
	To confirm that the patient was deleted, we can do a view,
	```
	curl -X GET -H "X-Auth-Token: abcd" -H "Accept: application/vnd.ga4gh.matchmaker.v1.0+json" -H "Content-Type: application/x-www-form-urlencoded" http://localhost:8080/patient/view
	```
	
	The result would now be,
	```
	[]
	```
