#! /usr/bin/env python3
import sys, getopt
from api_validator import Validator

def main(argv):
  print(argv)
  try:
    opts, args = getopt.getopt(argv, "hi:o:", ["idir=", "odir=", "psextant=", "psite="])
  except getopt.GetoptError:
    print("./run.py -i <input_yaml> -o <output_dir> psextant <psextant>, psite <psite>")
    sys.exit(2)
  for opt, arg in opts:
    if opt == "-h":
      print("test.py -i <input_yaml> -o <output_dir>")
      sys.exit()
    elif opt in ("-i", "--iyaml"):
      input_yaml = arg
    elif opt in ("-o", "--odir"):
      output_dir = arg
    elif opt in ("-psextantl"):
      psextantl = arg
    elif opt in ("-psextantr"):
      psextantr = arg
    elif opt in ("-psitel"):
      psitel = arg
    elif opt in ("-psiter"):
      psiter = arg
  v = Validator(input_yaml, output_dir, psitel, psiter, psextantl, psextantr)
  result = v.workflow()
  print(result)


if __name__ == "__main__":
  main(sys.argv[1:])

