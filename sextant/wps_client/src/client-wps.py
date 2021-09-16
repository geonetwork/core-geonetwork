#!/usr/bin/python
import getopt
import os
import sys
from owslib.wps import WebProcessingService, monitorExecution
import urllib.request
import smtplib
from email.message import EmailMessage
from config import *


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
    -v, --verbose set flag for verbose output - optional (defaults to False)

Examples
--------

python3 client-wps.py -d /tmp/toto.csv --verbose --url https://www.ifremer.fr/services/wps3/surval --xml ../sample/wps.xml
"""
        % sys.argv[0]
    )


def send_mail(to_email, subject, message, server=SMTP_SERVER, from_email=FROM_EMAIL):
    print("failed will send mail")
    msg = EmailMessage()
    msg["Subject"] = subject
    msg["From"] = from_email
    msg["To"] = ", ".join(to_email)
    msg.set_content(message)
    server = smtplib.SMTP(server, SMTP_PORT)
    server.ehlo()
    try:
        server.starttls()
    except:
        print("SMTP does not support ttls")
    server.send_message(msg)
    try:
        server.login(SMTP_USER, SMTP_PASSWORD)
    except:
        print("Enable to login to SMTP server")
    server.quit()


# check args
if len(sys.argv) == 1:
    usage()
    sys.exit(1)

print("ARGV      :", sys.argv[1:])

try:
    options, remainder = getopt.getopt(
        sys.argv[1:],
        "u:x:d:i:v:e",
        ["url=", "xml=", "destination=", "identifier=", "verbose", "email="],
    )
except getopt.GetoptError as err:
    print(str(err))
    usage()
    sys.exit(2)

print("OPTIONS   :", options)

url = None
identifier = None
xml = None
verbose = False
destination = None
email = None

for opt, arg in options:
    if opt in ("-u", "--url"):
        url = arg
    elif opt in ("-d", "--destination"):
        destination = arg
    elif opt in ("-e", "--email"):
        email = arg
    elif opt in ("-x", "--xml"):
        xml = open(arg, "rb").read()
    elif opt in ("-i", "--identifier"):
        identifier = arg
    elif opt in ("-v", "--verbose"):
        verbose = True
    else:
        assert False, "Unhandled option"

# required arguments for all requests
if url is None or destination is None or email is None:
    usage()
    sys.exit(3)

# instantiate client
wps = WebProcessingService(url, verbose=verbose, skip_caps=True)

if xml is None:
    print('\nERROR: missing mandatory "-x (or --xml)" argument')
    usage()
    sys.exit(5)
# default IS ASYNCHRONOUS
execution = wps.execute(None, [], request=xml)

# monitor the process
monitorExecution(execution)
if execution.errors:
    text_error = "{} error(s) when processing WPS \n:".format(len(execution.errors))
    for error in execution.errors:
        text_error += "Error code : {} with message {} \n".format(
            error.code, error.text
        )

    text_error += "This is an automatic email"
    send_mail(to_email=[email], subject="WPS: Error with process", message=text_error)

# show status
print("percent complete", execution.percentCompleted)
print("status message", execution.statusMessage)

for output in execution.processOutputs:
    print(
        "identifier=%s, dataType=%s, data=%s, reference=%s"
        % (output.identifier, output.dataType, output.data, output.reference)
    )
    if output.reference:
        urllib.request.urlretrieve(output.reference, destination)
