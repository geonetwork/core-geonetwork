#!/usr/bin/python
import getopt
import os
from owslib.wps import WebProcessingService, monitorExecution
import urllib.request

from timeout import *
from utils import *
import sys
import requests

MIN_FILE_SIZE = 100  # bytes


def get_args(argv):
    import getopt

    # check args
    if len(argv) == 1:
        usage()
        sys.exit(1)

    print("ARGV      :", argv[1:])

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
        usage()
        sys.exit(2)

    print("OPTIONS   :", options)

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
        print(opt)
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

        # required arguments for all requests
    if url is None or destination is None or email is None or smtp_host is None:
        usage()
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


def usage():
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


def process_wps_execute(
    xml,
    url,
    destination,
    email,
    smtp_host,
    smtp_user=None,
    smtp_pass=None,
    smtp_port=None,
    from_email=None,
    timeout=None,
    check_volume=False,
    verbose=False,
):

    # check if destination is dir, if it is the case get the output value from the xml
    if os.path.isdir(destination):
        destination = "{}/{}".format(destination, get_output_name_from_xml(xml))

    # instantiate client
    wps = WebProcessingService(url, verbose=verbose, skip_caps=True)

    if xml is None:
        print('\nERROR: missing mandatory "-x (or --xml)" argument')
        usage()
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
    print("percent complete", execution.percentCompleted)
    print("status message", execution.statusMessage)

    for output in execution.processOutputs:
        print(
            "identifier=%s, dataType=%s, data=%s, reference=%s"
            % (output.identifier, output.dataType, output.data, output.reference)
        )

        if output.reference:
            response = requests.head(output.reference, allow_redirects=True)
            size = int(response.headers.get("content-length", -1))
            if size < MIN_FILE_SIZE:
                send_mail(
                    to_email=[email],
                    smtp_host=smtp_host,
                    smtp_pass=smtp_pass,
                    smtp_user=smtp_user,
                    smtp_port=smtp_port,
                    from_email=from_email,
                    subject="WPS: Error with Size".format(),
                    message="File should be > {} but is {}.\n"
                    "To Download directly visit {}".format(
                        MIN_FILE_SIZE, size, output.reference
                    ),
                )

            try:
                urllib.request.urlretrieve(output.reference, destination)
            except:
                send_mail(
                    to_email=[email],
                    smtp_host=smtp_host,
                    smtp_pass=smtp_pass,
                    smtp_user=smtp_user,
                    smtp_port=smtp_port,
                    from_email=from_email,
                    subject="WPS: Error ({})".format(e),
                    message="Failed to get download file from {}".format(
                        output.reference
                    ),
                )
