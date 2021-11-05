#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
from owslib.wps import WebProcessingService, monitorExecution
import urllib.request

from timeout import timeout_
from utils import send_mail
import sys


def get_args(argv):
    import getopt

    # check args
    if len(argv) == 1:
        usage()
        sys.exit(1)

    try:
        options, remainder = getopt.getopt(
            argv[1:],
            "u:x:d:e:t:sh:sp:fe:sps:su:v",
            [
                "url=",
                "xml=",
                "destination=",
                "email=",
                "timeout=",
                "smtphost=",
                "smtpport=",
                "fromemail=",
                "smtppass=",
                "smtpuser=",
                "verbose",
            ],
        )
    except getopt.GetoptError as err:
        print(str(err))
        usage(argv)
        sys.exit(2)

    url = None
    xml = None
    verbose = False
    destination = None
    email = None
    from_email = None
    timeout = 2000
    smtp_host = None
    smtp_port = 25
    smtp_user = None
    smtp_pass = None

    for opt, arg in options:
        if opt in ("-u", "--url"):
            url = arg
        elif opt in ("-d", "--destination"):
            destination = arg
        elif opt in ("-e", "--email"):
            email = arg
        elif opt in ("-x", "--xml"):
            xml = open(arg, "rb").read()
        elif opt in ("-sh", "--smtphost"):
            smtp_host = arg
        elif opt in ("-sp", "--smtpport"):
            smtp_port = arg
        elif opt in ("-fe", "--fromemail"):
            from_email = arg
        elif opt in ("-sps", "--smtppass"):
            smtp_pass = arg
        elif opt in ("-su", "--smtpuser"):
            smtp_user = arg
        elif opt in ("-t", "--timeout"):
            timeout = arg
            try:
                timeout = int(timeout)
            except:
                assert False, "Timeout must be an int"
        elif opt in ("-v", "--verbose"):
            verbose = True
        else:
            assert False, "Unhandled option"
    if verbose:
        print("OPTIONS   :", options)
        # required arguments for all requests
    if url is None or destination is None or email is None or smtp_host is None:
        usage(argv)
        sys.exit(3)
    else:
        return (
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
        )


def usage(argv):
    print(
        """

Usage: %s [parameters]
Common Parameters for all request types
-------------------
-u, --url=[URL] the base URL of the WPS - required
-d, --destination file destination - required
-e, --email email in case of error - required
-x, --xml XML file containing pre-made request to be submitted - required
-t, --timeout (s). Timeout for WPS process - optional (defaults to 2000)
-sh, --smtphost. SMTP host
-sp, --smtpport. SMTP port- optional (defaults to 25)
-fe, --fromemail. Email that will be the sender
-sps, --smtppass. SMTP password - optional
-su, --smtpuser. SMTP user - optional
-v, --verbose set flag for verbose output - optional (defaults to False)

Examples
--------

python3 client-wps.py -d /tmp/toto.csv --verbose --url https://www.ifremer.fr/services/wps3/surval --xml ../sample/wps.xml
"""
        % argv[0]
    )


def fetch_wps_output_file(output, email, user_destination, **kwargs):
    # check if destination is dir, if it is the case get the output value from the xml
    if os.path.isdir(user_destination):
        base_name = user_destination + "/" + os.path.basename(output.reference)
        destination = os.path.basename(base_name).split("?")[0]
    else:
        destination = user_destination
    try:
        urllib.request.urlretrieve(output.reference, destination)
    except Exception as e:
        send_mail(
            to_email=[email],
            smtp_host=kwargs["smtp_host"],
            smtp_pass=kwargs["smtp_pass"],
            smtp_user=kwargs["smtp_user"],
            smtp_port=kwargs["smtp_port"],
            from_email=kwargs["from_email"],
            subject="WPS: Error",
            message="Failed to get download file from {} \n"
            "with error ({})".format(output.reference, e),
        )


def process_wps_execute(
    xml,
    url,
    email,
    smtp_host,
    smtp_user=None,
    smtp_pass=None,
    smtp_port=None,
    from_email=None,
    timeout=None,
    verbose=False,
):

    # instantiate client
    wps = WebProcessingService(url, verbose=verbose, skip_caps=True)

    if xml is None:
        print('\nERROR: missing mandatory "-x (or --xml)" argument')
        sys.exit(5)

    # default IS ASYNCHRONOUS
    execution = wps.execute(None, [], request=xml)

    # monitor the process
    with timeout_(seconds=timeout):
        try:
            monitorExecution(execution)
        except TimeoutError as e:
            send_mail(
                to_email=[email],
                smtp_host=smtp_host,
                smtp_pass=smtp_pass,
                smtp_user=smtp_user,
                smtp_port=smtp_port,
                from_email=from_email,
                subject="WPS: timeout ({})".format(e),
                message="WPS process took more than {} s\n"
                "This is an automatic email".format(timeout),
            )

    if execution.errors:
        text_error = "{} error(s) when processing WPS \n:".format(len(execution.errors))
        for error in execution.errors:
            text_error += "Error code : {} with message {} \n".format(
                error.code, error.text
            )

        text_error += "This is an automatic email"
        send_mail(
            to_email=[email],
            smtp_host=smtp_host,
            smtp_pass=smtp_pass,
            smtp_user=smtp_user,
            smtp_port=smtp_port,
            from_email=from_email,
            subject="WPS: Error with process",
            message=text_error,
        )

    # show status
    if verbose:
        print("percent complete", execution.percentCompleted)
        print("status message", execution.statusMessage)

    return execution.processOutputs
