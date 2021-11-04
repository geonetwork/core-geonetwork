#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import requests
from utils_wps import process_wps_execute, fetch_wps_output_file, get_args
from utils import send_mail

MIN_FILE_SIZE = 100  # bytes


def is_file_output_size_correct(output, email, **kwargs):
    if output.reference:
        response = requests.head(output.reference, allow_redirects=True)
        size = int(response.headers.get("content-length", -1))
        if size < MIN_FILE_SIZE:
            send_mail(
                to_email=[email],
                smtp_host=kwargs["smtp_host"],
                smtp_pass=kwargs["smtp_pass"],
                smtp_user=kwargs["smtp_user"],
                smtp_port=kwargs["smtp_port"],
                from_email=kwargs["from_email"],
                subject="WPS: Error with Size".format(),
                message="File should be > {} but is {}.\n"
                "To Download directly visit {}".format(
                    MIN_FILE_SIZE, size, output.reference
                ),
            )
            return False
    return True


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
    if is_file_output_size_correct(
        output,
        email,
        smtp_host=smtp_host,
        smtp_user=smtp_user,
        smtp_pass=smtp_pass,
        smtp_port=smtp_port,
        from_email=from_email,
    ):
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
