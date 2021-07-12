# grep the version from the mix file
VERSION=$(shell ./version.sh)


# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help


# DOCKER TASKS
build: ## Build the image
	docker build . -t sextant/api-page-builder

test: ## Run the tests in the docker image
	docker run sextant/api-page-builder pytest /app/tests

run-default: ## Run the builder using default conf
	docker run \
    -v ${PWD}/conf/sample.yaml:/app/conf/sample.yaml \
    -v ${PWD}/output:/out \
    sextant/api-page-builder \
    ./run.py -i /app/conf/sample.yaml -o /out --host "http://localhost:8080"
