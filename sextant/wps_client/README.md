# WPS Execute

## About / Synopsis

* Using Python enables you to execute WPS processes
* Output will be saved to specified destination
* If WPS fails, an email will be sent to the provided email 


## Installation

```pip3 install -r requirements.txt```

## Requires a SMTP SERVER

For debugging 
```sudo python3 -m smtpd -c DebuggingServer -n localhost:255```

Adjust the variables in config.py for the SMTP server

## Usage

```python3 basic_wps.py
--email julien.waddle@camptocamp.com
-d /tmp/
--url https://www.ifremer.fr/services/wps3/surval
--xml ../sample/wps.xml
--timeout 20
--smtphost localhost
--smtpport 255
--fromemail toto@toto.fr```
