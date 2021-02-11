#!/bin/bash
mvn clean package assembly:single
rm Java-Greenscreen.zip
zip -r Java-Greenscreen README.md LICENSE licenses data
cd target
zip ../Java-Greenscreen.zip Java-Greenscreen.jar
