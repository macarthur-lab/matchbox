'''
1. Insert test dataset via ~/insert endpoint
2. Match against and inserts
3. Delete inserts using ~/delete endpoint

'''

import sys
import os
from argparse import ArgumentParser
import json
import requests

__all__ = []
__version__ = 0.1


#set this value to 1 for profiling tool performance
PROFILE=0

ACCESS_TOKEN=""
MME_NODE_ACCEPT_HEADER='application/vnd.ga4gh.matchmaker.v0.1+json'
MME_CONTENT_TYPE_HEADER='application/x-www-form-urlencoded'
MME_SERVER_HOST='http://localhost:8080'
MME_ADD_INDIVIDUAL_URL = MME_SERVER_HOST + '/patient/add'
MME_DELETE_INDIVIDUAL_URL = MME_SERVER_HOST + '/patient/delete'
'''
    matches in local MME database ONLY, won't search in other MME nodes
'''
MME_LOCAL_MATCH_URL = MME_SERVER_HOST + '/match'      
'''
    matches in EXTERNAL MME nodes ONLY, won't search in LOCAL MME database/node
'''
MME_EXTERNAL_MATCH_URL = MME_SERVER_HOST + '/patient/match'
DATA_FILE='data/test.json'


def main(argv=None):
  """
  Starts application
  """
  try:
    program_version_message=get_usage_msgs()
    parser = ArgumentParser(description='A simple tool regression test API.\n\n'+'Version: ' + program_version_message)
    parser.add_argument('-V', '--version', action='store_true', help='display version information')
    args = parser.parse_args()
  except Exception as e:
    print 'error parsing command-line arguments.\n\n',e,'\n\n'
    sys.exit()
  if args.version:
    print __version__
    sys.exit()
  start()
  
def start():
    """
    Start processing
    """
    test_patients=get_test_data()
    inserted_ids=insert_test_data_into_db(test_patients)
    if len(inserted_ids)==50:
        print "\n\ninsertion passed."
    else:
        print "\n\ninserting wasn't complete, inserted number:",len(inserted_ids)
    #deleted_ids=delete_test_data_in_db(inserted_ids)
    print "\n\n"

def insert_test_data_into_db(patients):
    """
    Inserts a series of test patients into DB and returns a list of inserted IDs
    """
    inserted_ids=[]
    try:
        headers={
                 "X-Auth-Token":ACCESS_TOKEN,
                 "Accept":MME_NODE_ACCEPT_HEADER,
                 "Content-Type":MME_CONTENT_TYPE_HEADER
                 }
        for patient in patients:
            payload = {"patient":patient}
            req = requests.post(MME_ADD_INDIVIDUAL_URL, 
                              data=json.dumps(payload),
                              headers=headers)
            print "\t\t----inserting",patient['id'],req.text
            if json.loads(req.text)["status_code"]==200:
                inserted_ids.append(patient['id'])
        return inserted_ids
    except Exception as e:
        print 'error inserting test patients',e
        sys.exit()
        
        
def delete_test_data_in_db(ids_to_delete):
    """
    Deletes a series of test patients from DB and returns a list of deleted IDs
    """
    deleted_ids=[]
    try:
        headers={
                 "X-Auth-Token":ACCESS_TOKEN,
                 "Accept":MME_NODE_ACCEPT_HEADER,
                 "Content-Type":MME_CONTENT_TYPE_HEADER
                 }
        for id in ids_to_delete:
            payload = {"id":id}
            req = requests.post(MME_DELETE_INDIVIDUAL_URL, 
                              data=json.dumps(payload),
                              headers=headers)
            print "\t\t----deleting",patient['id'],req.text
            if json.loads(req.text)["status_code"]==200:
                deleted_ids.append(patient['id'])
        return deleted_ids
    except Exception as e:
        print 'error deleting test patients',e
        sys.exit()
  
  
def get_test_data():
    """
    Returns JSON test data
    """
    try:
        with open(DATA_FILE,'r') as json_in:
            j = json.load(json_in)
        return j
    except Exception as e:
        print "error loading test JSON",e
        sys.exit()





def get_usage_msgs():
  """
  Returns a composed usage message
  """
  program_version = "v%s" % __version__
  program_version_message = program_version
  return (program_version_message)
  
  
if __name__ == "__main__":
    if PROFILE:
        import cProfile
        import pstats
        profile_filename = 'benchmark_regression_tests.txt'
        cProfile.run('main()', profile_filename)
        statsfile = open("profile_stats.txt", "wb")
        p = pstats.Stats(profile_filename, stream=statsfile)
        stats = p.strip_dirs().sort_stats('cumulative')
        stats.print_stats()
        statsfile.close()
        sys.exit(0)
    sys.exit(main())