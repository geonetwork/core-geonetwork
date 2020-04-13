from api_validator.build_api_from_source_code import Validator
import os

input_yaml = "../../data/input.yaml"
output_dir = "../../data/"
prefix_base_url = ""
prefix_sextant_url = ""


@pytest.fixture(scope="session", autouse=True)
def set_up():
    # Will be executed before the first test
    copyfile("project.qgs", "base_project.qgs")
    yield
    # Will be executed after the last test
    copyfile("base_project.qgs", "project.qgs")
    os.remove("base_project.qgs")


def test_access_site():
    v = Validator(input_yaml, output_dir, prefix_base_url, prefix_sextant_url)

    assert True


def test_find_href_src():
    assert True


def test_replace_values():
    assert True


def test_no_404_errors():
    assert True
