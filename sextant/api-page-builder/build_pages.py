import os
import requests
from bs4 import BeautifulSoup
import yaml
from urllib.parse import urlsplit, urljoin

## returns the amount of errors, 0 if everything went fine
def build_all_pages(sites, output_dir_path, sextant_live_host, sextant_test_host):
    print(
        "Replacing sextant host '{}' with '{}'".format(
            sextant_live_host, sextant_test_host
        )
    )

    errors = 0
    for site_name in sites:
        print("Working on {}".format(site_name))
        output_file = os.path.join(output_dir_path, site_name + ".html")
        errors += build_page(sites[site_name], output_file, sextant_live_host, sextant_test_host)

    return errors

## returns 1 if error
def build_page(site_url, output_file_path, sextant_live_host, sextant_test_host):
    html = _load_page_html(site_url)
    if not html:
        print("{} not accessible, and has been ignored".format(site_url))
        return 1

    html_modified = _replace_urls(html, site_url, sextant_live_host, sextant_test_host)

    f = open(output_file_path, "w")
    f.write(html_modified)
    return 0

def _load_page_html(url):
    try:
        r = requests.get(url)
    except Exception as error:
        print("Site {url} nt accessible: {error}".format(url=url, error=error))
        return None
    return r.text

def _replace_urls(html, site_url, sextant_live_host, sextant_test_host):
    site_host = "{0.scheme}://{0.netloc}/".format(urlsplit(site_url))
    print("Site host is '{}'".format(site_host))

    base_site_url_lookup = ["/", "./"]

    soup = BeautifulSoup(html, features="html5lib")
    for base in soup.findAll("base"):
        base.unwrap()
    for a in soup.findAll("a"):
        if a["href"].startswith("//"):
            continue
        if a["href"].startswith(".."):
            a["href"] = _to_absolute_url(a["href"], site_url)
        if a["href"].startswith(tuple(base_site_url_lookup)):
            for i in base_site_url_lookup:
                a["href"] = a["href"].replace(i, site_host, 1)
    for img in soup.findAll("img"):
        if img["src"].startswith("//"):
            continue
        if img["src"].startswith(".."):
            img["src"] = _to_absolute_url(img["src"], site_url)
        if img["src"].startswith(tuple(base_site_url_lookup)):
            for i in base_site_url_lookup:
                img["src"] = img["src"].replace(i, site_host, 1)
    for link in soup.findAll("link"):
        if link["href"].startswith("//"):
            continue
        if link["href"].startswith(".."):
            link["href"] = _to_absolute_url(link["href"], site_url)
        if link["href"].startswith(tuple(base_site_url_lookup)):
            for i in base_site_url_lookup:
                link["href"] = link["href"].replace(
                    i, site_host, 1
                )
    for script in soup.findAll("script"):
        if script.has_attr("src"):
            if script["src"].startswith("//") and sextant_live_host != "//":
                continue
            #  here we look only for sextant files
            if script["src"].startswith(".."):
                script["src"] = _to_absolute_url(script["src"], site_url)
            if _is_sextant_url(script["src"], sextant_live_host) and not script[
                "src"
            ].startswith("/"):
                script["src"] = _to_test_sextant_url(script["src"], sextant_test_host)
            # do not deal with external scripts
            elif script["src"].startswith("http") or script["src"].startswith(
                "https"
            ):
                continue
            # all src files that are not sextant with relative paths
            else:
                for i in base_site_url_lookup:
                    script["src"] = script["src"].replace(
                        i, site_host, 1
                    )
    result = str(soup.prettify()) + "\n"
    return result

def _to_absolute_url(relative_url, site_url):
    return urljoin(site_url, relative_url)

def _is_sextant_url(live_url, sextant_live_host):
    return live_url.find(sextant_live_host) != -1

def _to_test_sextant_url(sextant_live_url, sextant_test_host):
    live_url_parts = urlsplit(sextant_live_url)
    test_url_parts = urlsplit(sextant_test_host)
    scheme = test_url_parts.scheme if test_url_parts.scheme != "" else live_url_parts.scheme
    return "{2}://{1.netloc}{0.path}".format(live_url_parts, test_url_parts, scheme)

def read_yaml(input_file):
    with open(input_file) as f:
        return yaml.safe_load(f)
