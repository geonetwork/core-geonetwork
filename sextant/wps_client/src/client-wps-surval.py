#!/usr/bin/python
import sys
from main import get_args
from main import process_wps_execute

(
    xml,
    url,
    destination,
    email,
    smtp_host,
    smtp_user,
    smtp_pass,
    smtp_port,
    from_email,
    timeout,
    verbose,
) = get_args(sys.argv)
process_wps_execute(
    xml,
    url,
    destination,
    email,
    smtp_host,
    smtp_user,
    smtp_pass,
    smtp_port,
    from_email,
    timeout,
    True,
    verbose=True,
)
