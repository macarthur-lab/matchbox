#!/usr/bin/env bash

set -x

env

cd /matchbox_deployment

if $USE_HTTPS
then
	if [ "$MONGODB_USERNAME" = "" ];
	then
		java -jar -Dallow.no-gene-in-common.matches=$ALLOW_NO_GENE_IN_COMMON_MATCHES \
					-Dexomiser.data-directory=$EXOMISER_DATA_DIR  \
					-Dspring.data.mongodb.host=$MONGODB_HOSTNAME  \
					-Dspring.data.mongodb.port=$MONGODB_PORT \
					-Dspring.data.mongodb.database=$MONGODB_DATABASE \
					-Dserver.port=$SERVER_PORT \
					-Dserver.ssl.key-store=$HTTPS_SSL_KEY_STORE \
					-Dserver.ssl.key-store-password=$HTTPS_SSL_KEY_STORE_PASSWORD \
					-Dserver.ssl.key-password=$HTTPS_SSL_KEY_PASSWORD \
					-Dexomiser.phenotype.data-version=$EXOMISER_PHENOTYPE_DATA_VERSION \
					matchbox-0.1.0.jar &
	else
		java -jar -Dallow.no-gene-in-common.matches=$ALLOW_NO_GENE_IN_COMMON_MATCHES \
				-Dexomiser.data-directory=$EXOMISER_DATA_DIR  \
				-Dspring.data.mongodb.host=$MONGODB_HOSTNAME  \
				-Dspring.data.mongodb.port=$MONGODB_PORT \
				-Dspring.data.mongodb.username=$MONGODB_USERNAME  \
				-Dspring.data.mongodb.password=$MONGODB_PASSWORD  \
				-Dspring.data.mongodb.database=$MONGODB_DATABASE \
				-Dserver.port=$SERVER_PORT \
				-Dserver.ssl.key-store=$HTTPS_SSL_KEY_STORE \
				-Dserver.ssl.key-store-password=$HTTPS_SSL_KEY_STORE_PASSWORD \
				-Dserver.ssl.key-password=$HTTPS_SSL_KEY_PASSWORD \
				-Dexomiser.phenotype.data-version=$EXOMISER_PHENOTYPE_DATA_VERSION \
				matchbox-0.1.0.jar &
	fi
else
	if [ "$MONGODB_USERNAME" = "" ];
	then
		java -jar -Dallow.no-gene-in-common.matches=$ALLOW_NO_GENE_IN_COMMON_MATCHES \
					-Dexomiser.data-directory=$EXOMISER_DATA_DIR  \
					-Dspring.data.mongodb.host=$MONGODB_HOSTNAME  \
					-Dspring.data.mongodb.port=$MONGODB_PORT \
					-Dspring.data.mongodb.database=$MONGODB_DATABASE \
					-Dserver.port=$SERVER_PORT \
					-Dexomiser.phenotype.data-version=$EXOMISER_PHENOTYPE_DATA_VERSION \
					matchbox-0.1.0.jar &
	else
		java -jar -Dallow.no-gene-in-common.matches=$ALLOW_NO_GENE_IN_COMMON_MATCHES \
			-Dexomiser.data-directory=$EXOMISER_DATA_DIR  \
			-Dspring.data.mongodb.host=$MONGODB_HOSTNAME  \
			-Dspring.data.mongodb.port=$MONGODB_PORT \
			-Dspring.data.mongodb.username=$MONGODB_USERNAME  \
			-Dspring.data.mongodb.password=$MONGODB_PASSWORD  \
			-Dspring.data.mongodb.database=$MONGODB_DATABASE \
			-Dserver.port=$SERVER_PORT \
			-Dexomiser.phenotype.data-version=$EXOMISER_PHENOTYPE_DATA_VERSION \
			matchbox-0.1.0.jar &
	fi
fi



sleep 10000000000