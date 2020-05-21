import requests
from bs4 import BeautifulSoup
import yaml
from urllib.parse import urlsplit
import urllib.parse


class ApiPageBuilder:
    def __init__(
        self, input_file, output_dir, sextant_url_lookup, sextant_url_replace_by,
    ):
        self.inputs = self.read_yaml(input_file)
        self.output_dir = output_dir
        self.base_site_url_lookup = ["/", "./"]
        self.base_site_url_replace_by = None
        self.sextant_url_lookup = sextant_url_lookup
        self.sextant_url_replace_by = sextant_url_replace_by
        self.working_url = None

    def read_yaml(self, input_file):
        with open(input_file) as f:
            return yaml.safe_load(f)

    def get_html(self, site_name):
        url = self.inputs[site_name]
        self.working_url = url
        return self.get_source_code(url)

    def write_to_html(self, site_name, html):
        f = open(
            "{output_dir}/{site_name}.html".format(
                output_dir=self.output_dir, site_name=site_name
            ),
            "w",
        )
        f.write(html)
        return True

    def batch(self):
        print(
            "Will replace sextant urls prefix '{}' with '{}' for sextant urls".format(
                self.sextant_url_lookup, self.sextant_url_replace_by
            )
        )
        ignored = 0
        for site_name in self.inputs:
            print("Working on {}".format(site_name))
            self.base_site_url_replace_by = "{0.scheme}://{0.netloc}/".format(
                urlsplit(self.inputs[site_name])
            )

            print(
                "Replacing relative urls prefix with '{}'".format(
                    self.base_site_url_replace_by
                )
            )
            html = self.get_html(site_name)
            if not html:
                print("{} not accessible, and has been ignored")
                ignored += 1
                continue
            self.write_to_html(site_name, self.replace_urls(html))
        return ignored

    def replace_urls(self, html):
        soup = BeautifulSoup(html, features="html5lib")
        for base in soup.findAll("base"):
            base.unwrap()
        for a in soup.findAll("a"):
            if a["href"].startswith("//"):
                continue
            if a["href"].startswith(".."):
                a["href"] = self.replace_relative_in_path(a["href"])
            if a["href"].startswith(tuple(self.base_site_url_lookup)):
                for i in self.base_site_url_lookup:
                    a["href"] = a["href"].replace(i, self.base_site_url_replace_by, 1)
        for img in soup.findAll("img"):
            if img["src"].startswith("//"):
                continue
            if img["src"].startswith(".."):
                img["src"] = self.replace_relative_in_path(img["src"])
            if img["src"].startswith(tuple(self.base_site_url_lookup)):
                for i in self.base_site_url_lookup:
                    img["src"] = img["src"].replace(i, self.base_site_url_replace_by, 1)
        for link in soup.findAll("link"):
            if link["href"].startswith("//"):
                continue
            if link["href"].startswith(".."):
                link["href"] = self.replace_relative_in_path(link["href"])
            if link["href"].startswith(tuple(self.base_site_url_lookup)):
                for i in self.base_site_url_lookup:
                    link["href"] = link["href"].replace(
                        i, self.base_site_url_replace_by, 1
                    )
        for script in soup.findAll("script"):
            if script.has_attr("src"):
                if script["src"].startswith("//") and self.sextant_url_lookup != "//":
                    continue
                #  here we look only for sextant files
                if script["src"].startswith(".."):
                    script["src"] = self.replace_relative_in_path(script["src"])
                if script["src"].find(self.sextant_url_lookup) != -1 and not script[
                    "src"
                ].startswith("/"):
                    script["src"] = script["src"].replace(
                        self.sextant_url_lookup, self.sextant_url_replace_by, 1
                    )
                # do not deal with external scripts
                elif script["src"].startswith("http") or script["src"].startswith(
                    "https"
                ):
                    continue
                # all src files that are not sextant with relative paths
                else:
                    for i in self.base_site_url_lookup:
                        script["src"] = script["src"].replace(
                            i, self.base_site_url_replace_by, 1
                        )
        result = str(soup)
        return result

    def replace_relative_in_path(self, url):
        return urllib.parse.urljoin(self.working_url, url)

    @staticmethod
    def get_source_code(url):
        try:
            r = requests.get(url)
        except Exception as error:
            print("Site {url} no accessible: {error}".format(url=url, error=error))
            return None
        return r.text
