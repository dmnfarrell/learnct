# learnct

## Introduction
This software was written as part of an investigation in CT image reconstruction. 
It is presented as a simple tool that might be useful to users wishing to gain an intuitive
understanding of image reconstruction by playing with the software. The simulation uses simple
parallel beam geometry and 2D data only.

## Details
* Preset phantom objects generate projection data that can be backprojected
* Adjustable settings include a selection of filters, change in number of viewing angles, 
data bins, reconstruction field of view
* Animate back projection
* Save and load images
* Re-project 8 or 16 bit images
* Save & Load projection data
* Perform simple image filtering operations, plus image windowing controls
* Visualize the projection data and sinograms

## Installation

There are several ways to run this software :

The simplest is to just double-click on the file 'learnct_v0.3.jar', 
if you have windows explorer set up to open the jar file using java or type the following at the
console/terminal window inside this directory:

```java -jar learnct_v0.3.jar```

(this assumes that the path to the java jre is setup. If not, you will have to type the full 
location of the java.exe file).

Use java -Xmx256m -jar learnct_v0.3.jar to allocate more memory for runtime.

If you don't have a standalone JRE installed, but already have ImageJ installed,you can use the JRE
from that eg. C:\Program Files\ImageJ\jre\bin\java -jar learnct_v0.3.jar Java Runtime 
is required to run this application. It can be downloaded at http://www.java.com/en/download/manual.jsp
or see http://openjdk.java.net/


## ImageJ
Note that the ImageJ file opener API is used for opening images, so these classes are included in the jar file. 
Hence the application can open jpg, png, tif or dicom format images, amongst others. A plugin for ImageJ, 
using the same source code, that performs some of the same tasks can be downloaded at 
http://rsb.info.nih.gov/ij/plugins/radon-transform.html

For more information on the theory underlying image reconstruction by back projection 
see the following references:

* http://en.wikipedia.org/wiki/Tomographic_reconstruction
* Kak & Slaney (1988), Principles of Computerized Tomographic Imaging, IEEE Press, ISBN 0-87942-198-3.
