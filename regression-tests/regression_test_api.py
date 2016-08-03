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
    print "TODO"
    
    
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