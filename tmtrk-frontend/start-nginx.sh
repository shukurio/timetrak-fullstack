#!/bin/sh
# Replace PORT placeholder in nginx config
if [ -n "$PORT" ]; then
  sed -i "s/listen 80;/listen $PORT;/g" /etc/nginx/conf.d/default.conf
  sed -i "s/listen \[::\]:80;/listen [::]:$PORT;/g" /etc/nginx/conf.d/default.conf
fi

# Start nginx
nginx -g 'daemon off;'