'''
1. Insert test dataset via ~/insert endpoint
2. Match against and inserts
3. Delete inserts using ~/delete endpoint

'''

import sys
import os
from argparse import ArgumentParser


__all__ = []
__version__ = 0.1


#set this value to 1 for profiling tool performance
PROFILE=0


def main(argv=None):
  '''starts application'''
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
    default_access_toke="854a439d278df4283bf5498ab020336cdc416a7d"
    MME_NODE_ACCEPT_HEADER='application/vnd.ga4gh.matchmaker.v0.1+json'
    MME_CONTENT_TYPE_HEADER='application/x-www-form-urlencoded'
    MME_SERVER_HOST='http://localhost:8080'
    MME_ADD_INDIVIDUAL_URL = MME_SERVER_HOST + '/individual/add'
    #matches in local MME database ONLY, won't search in other MME nodes
    MME_LOCAL_MATCH_URL = MME_SERVER_HOST + '/match'      
    #matches in EXTERNAL MME nodes ONLY, won't search in LOCAL MME database/node
    MME_EXTERNAL_MATCH_URL = MME_SERVER_HOST + '/individual/match'
    
    
'''
returns a usage string
'''
def get_usage_msgs():
  '''returns a composed usage message'''
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