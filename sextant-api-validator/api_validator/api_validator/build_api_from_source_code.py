import requests
from bs4 import BeautifulSoup
import yaml


class Validator:
    def __init__(
        self,
        input_file,
        output_dir,
        base_site_url_lookup,
        base_site_url_replace_by,
        sextant_url_lookup,
        sextant_url_replace_by,
    ):
        self.inputs = self.read_yaml(input_file)
        self.output_dir = output_dir
        self.base_site_url_lookup = base_site_url_lookup
        self.base_site_url_replace_by = base_site_url_replace_by
        self.sextant_url_lookup = sextant_url_lookup
        self.sextant_url_replace_by = sextant_url_replace_by

    def read_yaml(self, input_file):
        with open(input_file) as f:
            return yaml.safe_load(f)

    def get_html(self, site_name):
        url = self.inputs[site_name]
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
            "Replacing all urls prefix '{}' with '{}' for base urls".format(
                self.base_site_url_lookup, self.base_site_url_replace_by
            )
        )
        print(
            "Replacing sextant urls prefix '{}' with '{}' for base urls".format(
                self.sextant_url_lookup, self.sextant_url_replace_by
            )
        )
        ignored = 0
        for site_name in self.inputs:
            print("Working on {}".format(site_name))
            html = self.get_html(site_name)
            if not html:
                print("{} not accessible, and has been ignored")
                ignored += 1
                continue
            self.write_to_html(site_name, self.replace_urls(html))
        return ignored

    def replace_urls(self, html):
        soup = BeautifulSoup(html, features="html5lib")
        for a in soup.findAll("a"):
            if a["href"].startswith("//"):
                continue
            if a["href"].startswith(self.base_site_url_lookup):
                a["href"] = a["href"].replace(
                    self.base_site_url_lookup, self.base_site_url_replace_by, 1
                )
        for img in soup.findAll("img"):
            if img["src"].startswith("//"):
                continue
            if img["src"].startswith(self.base_site_url_lookup):
                img["src"] = img["src"].replace(
                    self.base_site_url_lookup, self.base_site_url_replace_by, 1
                )
        for link in soup.findAll("link"):
            if link["href"].startswith("//"):
                continue
            if link["href"].startswith(self.base_site_url_lookup):
                link["href"] = link["href"].replace(
                    self.base_site_url_lookup, self.base_site_url_replace_by, 1
                )

        for script in soup.findAll("script"):
            if script.has_attr("src"):
                if script["src"].startswith("//"):
                    continue
                if script["src"].find(self.sextant_url_lookup) != -1:
                    script["src"] = script["src"].replace(
                        self.sextant_url_lookup, self.sextant_url_replace_by, 1
                    )
                else:
                    script["src"] = script["src"].replace(
                        self.base_site_url_lookup, self.base_site_url_replace_by, 1
                    )
        result = str(soup)
        return result

    @staticmethod
    def get_source_code(url):
        try:
            r = requests.get(url)
        except Exception as error:
            print("Site {url} no accessible: {error}".format(url=url, error=error))
            return None
        return r.text
