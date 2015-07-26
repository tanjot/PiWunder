#!/usr/bin/env python3

import sys
import requests
#import dateutil.parser
import datetime
import matplotlib
import matplotlib.pyplot as plt

ISO_DATE_FORMAT = "%Y-%m-%dT%H:%M:%S.%fZ"
ACCESS_TOKEN='99e0b62535a0b09d9063ec6d888a8764c582bbfd50cbf70f5210271a5959'
CLIENT_ID='bdcd526c482653a17732'

def get_meter_readings():
    headers = {'X-Access-Token': ACCESS_TOKEN, 'X-Client-ID': CLIENT_ID}
    payload = {'task_id':1254711834}
    req = requests.get(
            'https://a.wunderlist.com/api/v1/task_comments',
            params=payload,
            headers=headers)
    out = req.json()
    return_value = {}
    if req.status_code == 200:
        for item in out:
            reading = float(item['text'])
            created_at = item['created_at']
            #created_at = dateutil.parser.parse(created_at)
            created_at =  datetime.datetime.strptime(created_at, ISO_DATE_FORMAT)
            return_value[created_at] = reading
        return return_value
    elif req.status_code == 403:
        print(req.json()['error']['message'])


def plot(meter_readings):
    dates = list(meter_readings.keys())
    readings = list(meter_readings.values())
    print(dates, '\n', readings)
    matdates = matplotlib.dates.date2num(dates)
    plt.plot_date(matdates, readings, label="Foo")
    plt.show()


def main():
    meter_readings = get_meter_readings()
    plot(meter_readings)
    return 0


if __name__ == '__main__':
    sys.exit(main())
