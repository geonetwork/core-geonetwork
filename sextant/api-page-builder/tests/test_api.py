from build_pages import build_page, read_yaml, _replace_urls
import os
import pytest
import shutil

input_yaml = "tests/test.yaml"
input_dir = "tests/input/"
expected_output_dir = "tests/expected/"
sextant_live_host = "ifremer"
sextant_test_host = "http://localhost:8080"

@pytest.fixture(scope="session", autouse=True)
def set_up():
    # Will be executed before the first test

    yield

    # Will be executed after the last test

def test_read_yaml():
    conf = read_yaml(input_yaml)
    assert len(conf["sites"]) == 1
    assert conf["live_host"] == "https://ifremer:8080"
    assert conf["test_host"] == "http://localhost:8080"

def test_replace_urls():
    files = os.listdir(input_dir)

    for file in files:
        with open(os.path.join(input_dir, file), "r") as f:
            input_html = f.read()
        with open(os.path.join(expected_output_dir, file), "r") as f:
            expected_html = f.read()
        html_modified = _replace_urls(input_html, "http://mysite.org/page/1", sextant_live_host, sextant_test_host)
        assert expected_html == html_modified

"""
def test_sextant_replaced():
    v = ApiPageBuilder(
        input_yaml, output_dir, prefix_sextant_url, prefix_sextant_url_replace,
    )
    url = "/app/api_validator/tests/test.html"
    v.base_site_url_replace_by = "api_validator"
    page = open(url)
    count = 0
    v.write_to_html("test", v.replace_urls(page.read()))
    f = open("{}/test.html".format(output_dir))
    for line in f:
        if '<script src="http://sextantapi/' in line:
            count += 1
    assert count == 2


def test_find_script_src():
    v = ApiPageBuilder(
        input_yaml, output_dir, prefix_sextant_url, prefix_sextant_url_replace,
    )
    url = "/app/api_validator/tests/test.html"
    v.base_site_url_replace_by = "api_validator"
    page = open(url)
    count = 0
    v.write_to_html("test", v.replace_urls(page.read()))
    f = open("{}/test.html".format(output_dir))
    for line in f:
        if '="api_validator' in line:
            count += 1
    assert count == 45
 """
