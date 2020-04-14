#! /usr/bin/env python3
from api_validator.build_api_from_source_code import Validator
import argparse

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
        "--psl",
        "--prefix_basesite_lookup",
        help="output dir containing the output html files",
    )
    parser.add_argument(
        "--psr",
        "--prefix_basesite_replaces",
        help="prefix that will be looked for and replace for base site files",
    )
    parser.add_argument(
        "-v",
        dest="verbose",
        action="store_true",
        help="string that will replace the prefix",
    )
    args = parser.parse_args()
    import ipdb

    ipdb.set_trace()
    v = Validator(args.input, args.output, args.psl, args.psr, args.psxl, args.psxr)
    result = v.batch()

    print(
        "Finished with {} errors, output is located at {}".format(result, args.output)
    )
