export MSOD_DISABLE_SESSION_ID_CHECK="True"
MSOD_STAND_ALONE_STREAMING_SERVER="True"
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
rm -f logs/spectrumbrowser.log
mkdir logs
#gunicorn -w 4 -k flask_sockets.worker flaskr:app  -b '0.0.0.0:8000' --debug --log-file - --error-logfile -
gunicorn -w 4 -k flask_sockets.worker flaskr:app  -b '0.0.0.0:8000' --debug --log-file - --error-logfile -&
pid=$!
disown $pid
echo $pid > .gunicorn.pid
if [ "$MSOD_STAND_ALONE_STREAMING_SERVER" == "True" ]
then
    python DataStreaming.py&
    pid=$!
    disown $pid
    echo $pid > .datastreaming.pid
fi



