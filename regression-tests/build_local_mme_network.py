'''
 Builds a local pseudo MME network using 2 matchbox systems
'''

import subprocess
import sys
import datetime
import time
import os

__all__ = []
__version__ = 0.1


def main(argv=None):
    """
    Starts application
  """
    num_instances=2
    root_path = os.path.dirname(os.path.realpath(__file__))
    timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y_%m_%d')
    local_ip_address = find_local_ip()
    '''
    setup the HTTPS MME nodes that mimic a MME network
    '''
    for instance,directories in buid_dir_struct(root_path,timestamp,num_instances).iteritems():
        start_instance(local_ip_address,instance,directories)
        os.chdir(root_path)
    '''
    start a local HTTP instance (HTTP is easier to curl, and good enough for test) 
    and point to HTTPS instances we just created
    '''
    start_local_master_node()
     

def start_local_master_node():
    '''
    Master node to act as our local gateway to the MME network that was created
    '''
    pass



def start_instance(local_ip_address,instance,directories):
    '''
    Starts a mongodb instance and its matchbox instance
    '''
    print 'working on:',directories['prefix'],directories
    err=None
    os.chdir(directories['matchbox_dir'])
    if not os.path.exists('matchbox'):
        p = subprocess.Popen(['git','clone','-b','dev','https://github.com/macarthur-lab/matchbox'],stdout=subprocess.PIPE)
        (output, err) = p.communicate()
    else:
        print '----WARNING:','that repo already exists, not cloning a new copy:',directories['matchbox_dir']
    if err is None:
            mongo_port = start_dockerized_mongodb(instance,directories)
            start_dockerized_matchbox(local_ip_address,mongo_port,instance,directories)
    return


def start_dockerized_matchbox(local_ip_address,mongo_port,instance,directories):
    '''
    Starts a single HTTPS matchbox instance
    Args:
        mongo_port: the port of the MongoDB instance slated for this matchbox instance
        instance: int representing this instance
        dictories: information on path and prefixes for this instance
    '''
    instance_port = 8443 + instance
    existing_dockerfile = 'matchbox/deploy/docker/with_data_in_container/Dockerfile'
    updated_dockerfile = 'matchbox/deploy/docker/with_data_in_container/Dockerfile.net'
    with open(existing_dockerfile,'r') as di:
        dockerfile_lines = di.readlines()
    di.close()
    with open (updated_dockerfile,'w') as do:
        l=0
        while l<len(dockerfile_lines):
            line=dockerfile_lines[l]
            if "env MONGODB_HOSTNAME" in line:
                do.write(line.strip()+local_ip_address)
            elif "MONGODB_DATABASE" in line:
                do.write(line.strip()+"mme_primary")
            elif "env MONGODB_PORT" in line:
                do.write(line.strip().split("=")[0]+'='+mongo_port)
            elif "env USE_HTTPS" in line:
                do.write(line.strip().split("=")[0]+'='+'true\n')
                l+=1
                while "keypass $HTTPS_SSL_KEY_PASSWORD" not in dockerfile_lines[l]:
                    if "env SERVER_PORT" in dockerfile_lines[l]:
                        do.write(dockerfile_lines[l].replace("#","").split('=')[0]+'='+str(instance_port)+'\n')
                    else:
                        do.write(dockerfile_lines[l].replace("#",""))
                    l+=1
                do.write(dockerfile_lines[l].replace("#",""))
            else:
                do.write(line.strip())
            do.write('\n')
            l+=1
    do.close()
    image_name = directories['prefix'] + '_img' 
    os.chdir('matchbox/deploy/docker/with_data_in_container/')
    p = subprocess.Popen(['docker','build','-t',image_name,'-f',updated_dockerfile.split('/')[-1],'.'],stdout=subprocess.PIPE)
    (output, err) = p.communicate()
    print output,err
    return

        





def start_dockerized_mongodb(instance,directories):
    '''
    Starts a mongodb instance
    Args:
        instance: an int representing the instance number
        directories: a dictionary of various related dirs to this instance
    Returns:
        The mongo port this instance runs on
    '''
    if instance > 9:
        print "----WARNING:","skipping instance (too high!):",instance
        return 
    mongo_port='2701'+str(instance)
    mongo_prefix=directories['prefix']+'_mongo'
    cmd = ['docker','run','--name',mongo_prefix,'-d','-p',mongo_port+':27017', '-v',directories['mongo_data_dir'].split('/')[-1]+':/data/db','mongo']
    p = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    (output, err) = p.communicate()
    if not err:
        return mongo_port
    if "Conflict" in err and mongo_prefix in err:
        print "----WARNING:","skipping creating new mongo container, looks like one is still up:",mongo_prefix
        return mongo_port
    return None


def buid_dir_struct(root_path,timestamp,num_instances):
    '''
    Build a directory structure for network
     -for mongodb data directory
   -for matchbox instance 
    '''
    directories={}
    for i in xrange(0,num_instances):
        prefix = 'matchbox_' + str(i) 
        matchbox_dir= timestamp + '/' + prefix
        mongo_data_dir= matchbox_dir + '/mongo_data_dir_'+prefix 
        directories[i]={'matchbox_dir':matchbox_dir, 'mongo_data_dir':mongo_data_dir,"prefix":prefix,"root_path":root_path}
        if not os.path.exists(matchbox_dir):
            os.makedirs(matchbox_dir)
        else:
            print "----WARNING:",matchbox_dir,"exists, skipping directory creation" 
        if not os.path.exists(mongo_data_dir):
            os.makedirs(mongo_data_dir)
        else:
            print "----WARNING:",mongo_data_dir,"exists, skipping directory creation" 
    return directories


def find_local_ip():
    '''
    Find the IP address of the loca machine
    '''
    p = subprocess.Popen(['ifconfig'],stdout=subprocess.PIPE)
    (output, err) = p.communicate()
    for line in output.split('\n'):
        if 'inet' in line and 'broadcast' in line:
            return line.split(' ')[1]




  
if __name__ == "__main__":
    sys.exit(main())