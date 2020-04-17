#! /usr/bin/env python3
from api_validator.build_api_from_source_code import ApiPageBuilder
import argparse
import os

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-i", "--input", help="input yaml file containing list of sites"
    )
    parser.add_argument(
        "-o", "--output", help="output dir containing the output html files"
    )
    parser.add_argument(
        "--psxl",
        "--prefix_sextant_lookup",
        help="prefix that will be looked for and replace for sextant files",
    )
    parser.add_argument(
        "--psxr", "--prefix_sextant_replace", help="string that will replace the prefix"
    )
    parser.add_argument(
        "-v",
        dest="verbose",
        action="store_true",
        help="string that will replace the prefix",
    )
    args = parser.parse_args()
    if not os.path.exists(args.output) or not os.path.exists(args.input):
        print("{} does not exists. Exiting....".format(args.output))
    else:
        v = ApiPageBuilder(args.input, args.output, args.psxl, args.psxr)
        result = v.batch()

        print(
            "Finished with {} error(s), output files are located at {}".format(
                result, args.output
            )
        )
