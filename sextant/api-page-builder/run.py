#! /usr/bin/env python3
import yaml
from api_validator.build_api_from_source_code import ApiPageBuilder
import argparse
import os

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-i", "--input", help="input configuration yaml file"
    )
    parser.add_argument(
        "-o", "--output", help="output dir containing the output html files; will be created if non existent; files will be replaced if needed"
    )
    parser.add_argument(
        "-v",
        dest="verbose",
        action="store_true"
    )
    args = parser.parse_args()

    if not os.path.exists(args.input):
        print("{} does not exists. Exiting....".format(args.output))
        exit()

    if not os.path.exists(args.output):
        os.mkdir(args.output)

    with open(args.input) as f:
        conf = yaml.safe_load(f)

    v = ApiPageBuilder(conf.sites, args.output, conf.live_host, conf.test_host)
    result = v.batch()

    print(
        "Finished with {} error(s), output files are located at {}".format(
            result, args.output
        )
    )
