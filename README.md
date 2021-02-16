# Java Virtual Greenscreen for OBS

Rough proof of concept of a virtual greenscreen that can be 
added to broadcast software such as OBS or XSplit as a browser source.

The trained Portrait Segmentation network was pulled from the following repository

https://github.com/anilsathyan7/Portrait-Segmentation

and converted to the onnx format. Its license is provided in the ```licenses``` directory. 

##Usage

Running the provided jar file will open a GUI showing the processed camera feed, as well as a selection of configuration options:

* Camera: Select which webcam device to process. Can be changed without restarting the server.
* Resolution: A list of supported resolutions for the webcam device. Can be changed without restarting the server.
* Web Port: Local port the web source server will bind to.
* Websocket Port: Local port the websocket server will bind to.
* Host: Host address to bind to. NOTE: This program is not built to be securely operated remotely. Keep as ```localhost``` unless there is a strong reason not to.

##Building from source

A standalone jar can be generated with the command 

```mvn clean package assembly:single```

A zipped release (see [github releases page](https://github.com/ForOhForError/Java-Virtual-Greenscreen/releases)) can be generated with the command

```./package_release.sh```

This build process should function with any up to date JDK, but has only been tested on OpenJDK 15.0.2