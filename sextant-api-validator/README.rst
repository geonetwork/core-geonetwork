from api_validator.build_api_from_source_code import Validator
input_yaml = '../data/sites.yaml'
output_dir = '/tmp/'
output_dir = '/tmp/'
prefix_sextant_url = "https://sextant.ifremer.fr"
prefix_base_url = "/"
v = Validator(input_yaml, output_dir, prefix_base_url, "http://ceybien.com/", prefix_sextant_url, "http://geo:8080")
v.workflow()
