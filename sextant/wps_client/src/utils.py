import smtplib
from email.message import EmailMessage
from xml.etree import ElementTree as ET


def get_output_name_from_xml(xml_path):
    tree = ET.fromstring(xml_path)
    ns = {"wps": "http://www.opengis.net/wps/1.0.0"}
    output_file_name = (
        list(tree.iterfind(".//wps:Output", namespaces=ns))[0].getchildren()[0].text
    )
    return output_file_name


def send_mail(
    to_email,
    subject,
    message,
    smtp_host=None,
    from_email=None,
    smtp_port=25,
    smtp_user=None,
    smtp_pass=None,
):
    msg = EmailMessage()
    msg["Subject"] = subject
    msg["From"] = from_email
    msg["To"] = ", ".join(to_email)
    msg.set_content(message)
    server = smtplib.SMTP(smtp_host, smtp_port)
    server.ehlo()
    try:
        server.starttls()
    except:
        print("[warning] SMTP does not support ttls")
    if smtp_user and smtp_pass:
        try:
            server.login(smtp_user, smtp_pass)
        except:
            print("Enable to login to SMTP server")
    server.send_message(msg)
    server.quit()
