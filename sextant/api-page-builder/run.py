#! /usr/bin/env python3
import argparse
import os
from build_pages import build_all_pages, read_yaml

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-i", "--input", help="input configuration yaml file"
    )
    parser.add_argument(
        "-o", "--output", help="output dir containing the output html files; will be created if non existent; files will be replaced if needed"
    )
    parser.add_argument(
        "--host",
        help="on which host should sextant scripts be loaded from; this should include a scheme (https:// or http://) or start with //",
    )

    parser.add_argument(
        "-v",
        dest="verbose",
        action="store_true"
    )
    args = parser.parse_args()

    if not os.path.exists(args.input):
        print("{} does not exists. Exiting".format(args.input))
        exit()

    if not os.path.exists(args.output):
        os.mkdir(args.output)

    conf = read_yaml(args.input)
    result = build_all_pages(conf["sites"], args.output, conf["live_host"], args.host)

    print(
        "Finished with {} error(s), output files are located at {}".format(
            result, args.output
        )
    )
