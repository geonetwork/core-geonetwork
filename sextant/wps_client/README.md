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
--fromemail toto@toto.fr
```


## Build A executable

```
python3 -m venv clientWPS # generate venv

source clientWPS/bin/activate  # activate venv
pip3 install --upgrade pip
pip3 install .
pip3 install -r requirements.txt # get dependencies
pyinstaller --windowed --onefile --paths venv/lib/python3.6/site-packages wps_client/client_wps_surval.py
pyinstaller --windowed --onefile --paths venv/lib/python3.6/site-packages wps_client/client_wps.py
```

Then launch using :
```
./client_wps --email julien.waddle@camptocamp.com -d /home/jwaddle/Documents/sextant/sextant-geonetwork-7/sextant-geonetwork/geonetwork/sextant/wps_client/src --url https://www.ifremer.fr/services/wps3/surval --xml ./sample/wps.xml --timeout 20 --smtphost localhost --smtpport 255 --fromemail toto@toto.fr
```
