#!/bin/sh

lein uberjar
docker build -t mbragg02/pause-detector .
