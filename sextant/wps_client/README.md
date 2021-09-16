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

```python3 client-wps.py --email julien.waddle@camptocamp.com -d /tmp/toto.csv --verbose --url https://www.ifremer.fr/services/wps3/surval --xml ../sample/wps_fail.xml``` 
