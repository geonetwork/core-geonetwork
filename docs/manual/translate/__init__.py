"""Top-level package for translate script."""
# message/__init__.py

__app_name__ = "translate"
__version__ = "0.1.0"

(
    SUCCESS,
    DIR_ERROR,
    FILE_ERROR,
    API_READ_ERROR,
    API_READ_ERROR,
    JSON_ERROR,
    ID_ERROR,
) = range(7)

ERRORS = {
    DIR_ERROR: "config directory error",
    FILE_ERROR: "config file error",
    API_READ_ERROR: "REST API read error",
    API_READ_ERROR: "REST API write error",
    ID_ERROR: "id error",
}

