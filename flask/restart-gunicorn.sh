kill -9 $(cat .gunicorn.pid)
kill -9 $(cat .memcached.pid)
kill -9 $(cat .datastreaming.pid)
export MSOD_STAND_ALONE_STREAMING_SERVER=0
sleep 5
ps cax | grep memcached > /dev/null
if [ $? -eq 0 ]; then
  echo "memcached is running. run stop-gunicorn.sh"
  exit 0
fi
memcached&
pid=$!
echo $pid > .memcached.pid
disown $pid
ps cax | grep gunicorn > /dev/null
if [ $? -eq 0 ]; then
  echo "Process is running. run stop-gunicorn.sh"
  exit 1
fi
rm -f .gunicorn.pid
#gunicorn -w 4 -k flask_sockets.worker flaskr:app  -b '0.0.0.0:8000' --debug --log-file - --error-logfile -
gunicorn -w 4 -k flask_sockets.worker flaskr:app  -b '0.0.0.0:8000' --debug --log-file - --error-logfile -&
pid=$!
disown $pid
echo $pid > .gunicorn.pid
if [ $MSOD_STAND_ALONE_STREAMING_SERVER -eq 1 ]; then
    python DataStreaming.py&
    pid=$!
    disown $pid
    echo $pid > .datastreaming.pid
fi



