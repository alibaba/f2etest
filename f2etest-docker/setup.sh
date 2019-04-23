#!/bin/bash
mysql < /home/f2etest-web/f2etest.sql
sleep 2
nginx
sleep 2
pm2 start app.js --name f2etest-web

