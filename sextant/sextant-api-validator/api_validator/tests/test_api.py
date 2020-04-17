from api_validator.build_api_from_source_code import ApiPageBuilder
import os
import pytest
import shutil

input_yaml = "/app/api_validator/tests/test.yaml"
output_dir = "/app/api_validator/tests/tests/"
prefix_sextant_url = "http://ifremer:8080/"
prefix_sextant_url_replace = "http://sextantapi/"


@pytest.fixture(scope="session", autouse=True)
def set_up():
    # Will be executed before the first test
    try:
        shutil.rmtree(output_dir)
    except:
        pass
    os.mkdir(output_dir)
    yield
    # Will be executed after the last test
    shutil.rmtree(output_dir)


def test_read_yaml():
    v = ApiPageBuilder(
        input_yaml, output_dir, prefix_sextant_url, prefix_sextant_url_replace,
    )
    assert len(v.inputs) == 1


def test_access_site():
    v = ApiPageBuilder(
        input_yaml, output_dir, prefix_sextant_url, prefix_sextant_url_replace,
    )
    v.base_site_url_replace_by = "api_validator"
    url = "/app/api_validator/tests/test.html"
    page = open(url)
    assert True == v.write_to_html("test", v.replace_urls(page.read()))


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
