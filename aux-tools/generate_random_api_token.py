import sys
from argparse import ArgumentParser
import uuid

__all__ = []
__version__ = 0.1
__date__ = '2016-08-16'

#set this value to 1 for profiling tool performance
PROFILE=0


def main(argv=None):
  '''starts application'''
  try:
    program_license,program_version_message=get_usage_msgs()
    parser = ArgumentParser(description='A simple tool generate API tokens.\n\n'+'Version: ' + program_version_message)
    parser.add_argument('-V', '--version', action='store_true', help='display version information')
    args = parser.parse_args()
  except Exception as e:
    print 'error parsing command-line arguments\n\n',e,'\n\n'
    sys.exit()
  if args.version:
    print program_version_message
    sys.exit()
 
  start()


def start():
  """
  Start the tool
  """ 
  print uuid.uuid4().hex


def get_usage_msgs():
  """
  Returns a composed usage message
  """
  program_version = "v%s" % __version__
  program_version_message = program_version
  program_license = 'Licensed under the Apache License 2.0'
  return (program_license,program_version_message)
  
  
if __name__ == "__main__":
    if PROFILE:
        import cProfile
        import pstats
        profile_filename = 'benchmark_mongo.benchmark_query_profile.txt'
        cProfile.run('main()', profile_filename)
        statsfile = open("profile_stats.txt", "wb")
        p = pstats.Stats(profile_filename, stream=statsfile)
        stats = p.strip_dirs().sort_stats('cumulative')
        stats.print_stats()
        statsfile.close()
        sys.exit(0)
    sys.exit(main())