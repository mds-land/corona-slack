#!/bin/sh
docker build -f Dockerfile.native-image . -t corona-slack || exit 1
echo
echo
echo "To run the docker container execute:"
echo "    $ docker run -p 8080:8080 corona-slack"
