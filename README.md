# Java Virtual Greenscreen for OBS

Rough proof of concept of a virtual greenscreen that can be 
added to broadcast software such as OBS or XSplit as a browser source.

It currently takes a webcam feed for video input and produces a video feed at ```localhost:7777```

The trained Portrait Segmentation network was pulled from the following repository

https://github.com/anilsathyan7/Portrait-Segmentation

and converted to the onnx format. Its license is provided in the ```licenses``` directory. 