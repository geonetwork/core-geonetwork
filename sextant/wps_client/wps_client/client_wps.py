#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
from utils_wps import process_wps_execute, fetch_wps_output_file, get_args

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

outputs = process_wps_execute(
    xml,
    url,
    email,
    smtp_host,
    smtp_user,
    smtp_pass,
    smtp_port,
    from_email,
    timeout,
    verbose,
)
for output in outputs:
    fetch_wps_output_file(
        output,
        email,
        destination,
        smtp_host=smtp_host,
        smtp_user=smtp_user,
        smtp_pass=smtp_pass,
        smtp_port=smtp_port,
        from_email=from_email,
    )
