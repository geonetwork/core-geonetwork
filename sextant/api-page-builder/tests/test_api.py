from build_pages import build_page, read_yaml, _replace_urls, _to_absolute_url, _is_sextant_url, _to_test_sextant_url
import os
import pytest
import shutil

input_yaml = "tests/test.yaml"
input_dir = "tests/input/"
expected_output_dir = "tests/expected/"
sextant_live_host = "//sextant.ifremer.fr"
sextant_test_host = "http://localhost:8080"

@pytest.fixture(scope="session", autouse=True)
def set_up():
    # Will be executed before the first test

    yield

    # Will be executed after the last test

def test_read_yaml():
    conf = read_yaml(input_yaml)
    assert len(conf["sites"]) == 1
    assert conf["live_host"] == "sextant.ifremer.fr"
    assert conf["test_host"] == "localhost:8080"

def test_to_absolute_url():
    assert "https://site.com/aa/image.png" == _to_absolute_url("../image.png", "https://site.com/aa/bb/index")
    assert "https://site.com/aa/bb/image.png" == _to_absolute_url("./image.png", "https://site.com/aa/bb/index")
    assert "https://site.com/aa/bb/image.png" == _to_absolute_url("image.png", "https://site.com/aa/bb/index")
    assert "https://site.com/image.png" == _to_absolute_url("/image.png", "https://site.com/aa/bb/index")

def test_is_sextant_url():
    assert True == _is_sextant_url("https://sextant.ifremer.fr/aaa/bbb/sextant.js", "//sextant.ifremer.fr")
    assert True == _is_sextant_url("http://sextant.ifremer.fr:4200/aaa/bbb/sextant.js", "//sextant.ifremer.fr")
    assert True == _is_sextant_url("//sextant.ifremer.fr:4200/aaa/bbb/sextant.js", "//sextant.ifremer.fr")
    assert True == _is_sextant_url("//sextant.ifremer.fr:4200/aaa/bbb/sextant.js", "https//sextant.ifremer.fr")
    assert False == _is_sextant_url("https://sextant.ifremer.fr/aaa/bbb/sextant.js", "http://sextant.ifremer.fr")
    assert False == _is_sextant_url("//sextant-test.ifremer.fr/aaa/bbb/sextant.js", "http://sextant.ifremer.fr")
    assert False == _is_sextant_url("http://sextant.ifremer.fr/aaa/bbb/sextant.js", "http://sextant.ifremer.fr:8080")

def test_to_test_sextant_url():
    assert "http://localhost:8080/aaa/sextant.js" == _to_test_sextant_url("https://sextant.ifremer.fr/aaa/sextant.js", "http://localhost:8080")
    assert "https://localhost:8080/aaa/sextant.js" == _to_test_sextant_url("https://sextant.ifremer.fr/aaa/sextant.js", "//localhost:8080")
    assert "http://localhost/aaa/sextant.js" == _to_test_sextant_url("https://sextant.ifremer.fr/aaa/sextant.js", "http://localhost")
    assert "http://localhost/aaa/sextant.js" == _to_test_sextant_url("//sextant.ifremer.fr/aaa/sextant.js", "http://localhost")

def test_replace_urls():
    files = os.listdir(input_dir)

    for file in files:
        with open(os.path.join(input_dir, file), "r") as f:
            input_html = f.read()
        with open(os.path.join(expected_output_dir, file), "r") as f:
            expected_html = f.read()
        html_modified = _replace_urls(input_html, "http://mysite.org/page/1", sextant_live_host, sextant_test_host)

        # uncomment to check result
        #print(html_modified)

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
