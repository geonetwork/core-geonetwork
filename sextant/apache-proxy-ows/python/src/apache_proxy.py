import pathlib
import yaml
import csv
import os
from yaml.loader import SafeLoader
import sys
from pathlib import Path
import argparse

def get_config_from_yaml(filename):
    with open(filename) as f:
        return yaml.load(f, Loader=SafeLoader)


class ApacheProxy:
    def __init__(self, csv_file, conf_file, input_conf_path, header=False):
        self.config_file_path = ""
        self.filedir_path = ""  # /test/titi/(*).yaml
        self.full_path = ""  # /test/titi/
        self.publique_url = ""
        self.interne_url = ""
        self.input_conf_path = input_conf_path
        self.csv = open(csv_file, "w")
        self.file = open(conf_file, "w")
        self.csv_writer = csv.writer(self.csv)
        self.conf_writer = csv.writer(self.csv)
        if header:
            self.csv_writer.writerow(["url_interne", "url_publique"])
        self.publique_urls = []
        self.interne_urls = []

    def workflow(self):
        sites = get_config_from_yaml(self.input_conf_path)
        for site_config in sites["sites"]:
            self.update_conf(site_config)
            file_extension = str(self.get_file_extension())
            print("Extension à rechercher: {}".format(file_extension))
            matches_ = self.get_all_files_matching(file_extension)
            print(
                "{} Fichier(s) trouvé(s) dans le repertoire {}".format(
                    len(matches_), self.full_path
                )
            )
            self.build_lists(matches_)
        if self.publique_urls == []:
            print("Aucun fichier trouvé")
        else:
            self.write_to_csv_and_conf_file()
        self.csv.close()

    def write_to_csv_and_conf_file(self):
        ordered_public_list, ordered_interne_list = (
            list(t) for t in zip(*sorted(zip(self.publique_urls, self.interne_urls)))
        )
        for i, j in enumerate(ordered_interne_list):
            self.csv_writer.writerow([ordered_interne_list[i], ordered_public_list[i]])
            self.file.write(
                "ProxyPass    {} {}".format(
                    ordered_interne_list[i], ordered_public_list[i]
                )
            )
            self.file.write("\n")
            self.file.write(
                "ProxyPassReverse    {} {}".format(
                    ordered_interne_list[i], ordered_public_list[i]
                )
            )
            self.file.write("\n")
            self.file.write("\n")

    def update_conf(self, site_config):
        self.config_file_path = site_config
        self.filedir_path = site_config["filename"]  # /test/titi/(*).yaml
        self.full_path = str(pathlib.Path(self.filedir_path).parent)  # /test/titi/
        self.publique_url = site_config["url_publique"]
        self.interne_url = site_config["url_interne"]

    def get_file_extension(self):
        if self.filedir_path.endswith("(*)"):
            return "*"
        else:
            return pathlib.Path(self.filedir_path).suffix

    def get_all_files_matching(self, extension):
        matches = []
        for root, dirnames, filenames in os.walk(self.full_path):
            for filename in filenames:
                if filename.endswith(extension):
                    matches.append(os.path.join(root, filename))
                elif extension == "*":
                    matches.append(os.path.join(root, filename))
        return matches

    def build_lists(self, matches):
        for match in matches:
            value_to_add = str(
                Path(os.path.relpath(match, self.full_path)).with_suffix("")
            )
            publique_url = self.publique_url.replace("$1", value_to_add)
            interne_url = self.interne_url.replace("$1", value_to_add)
            if interne_url not in self.interne_urls:
                self.interne_urls.append(interne_url)
                self.publique_urls.append(publique_url)
            else:
                print(
                    "{} existe déja et sera considéré comme un doublon, il ne sera pas rajouté dans la conf".format(
                        interne_url
                    )
                )


if __name__ == "__main__":

    parser = argparse.ArgumentParser()
    parser.add_argument("-i", "--input", help="input configuration yaml file")
    parser.add_argument(
        "-o",
        "--output",
        help="output dir containing the output html files; will be created if non existent; files will be replaced if needed",
    )
    parser.add_argument("-c", "--column_name", help="use header")

    args = parser.parse_args()

    if not os.path.exists(args.input):
        print("{} does not exists. Exiting".format(args.input))
        exit()

    if not os.path.exists(args.output):
        os.mkdir(args.output)

    print("Début")
    add_header = False
    if args.column_name:
        print("Rajout des headers")
        add_header = True
    conf_file = args.input
    output_file_name = Path(conf_file).stem
    a = ApacheProxy(
        "/output/{}.csv".format(output_file_name),
        "/output/{}.apache".format(output_file_name),
        conf_file,
        header=add_header,
    )
    a.workflow()
    print("Fin")
