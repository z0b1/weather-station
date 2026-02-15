#!/bin/bash
# DuckDNS Update Script for z0b1 Weather Station
# Replace YOUR_DOMAIN and YOUR_TOKEN with your actual details from duckdns.org

DOMAIN="YOUR_DOMAIN"
TOKEN="YOUR_TOKEN"

echo url="https://www.duckdns.org/update?domains=$DOMAIN&token=$TOKEN&ip=" | curl -k -o ~/duckdns/duck.log -K -
