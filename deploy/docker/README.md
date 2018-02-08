# <i>matchbox Docker build process (beta)</i>

We are still in the process of designing a best practices build and deployment process using Docker and Kubernetes, so please consider this a work in progress. 

Please be careful to not check in Docker files with private secure
usernames, passwords, and tokens etc. 

Please also remember to change any default passwords built into system before production!


## Build:

<i>matchbox</i> requires a reference dataset to help build it's phenotype scoring model. This dataset can be downloaded separately and mounted to the Docker container or you can rely on Docker to take care of all these details!

1. [Build via having the reference data mounted into the container](with_data_mounted_to_container/README.md)

2. [Rely on Docker to get the reference data into the container (Easiest)](with_data_in_container/README.md)

 