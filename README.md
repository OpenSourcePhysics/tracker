tracker
=======

Video analysis and modeling tool built on the Open Source Physics (OSP) framework.

This code requires the OSP Core Library available in the <a href="https://github.com/OpenSourcePhysics/osp" target="_blank">OpenSourcePhysics/osp</a> repository.

Optional video engine support (FFMPeg on Win/OSX/linux, QuickTime on Win/OSX) is available in the <a href="https://github.com/OpenSourcePhysics/video-engines" target="_blank">OpenSourcePhysics/video-engines</a> repository. Without a video engine Tracker will only open images (JPEG, PNG) and animated GIFs.

Note: Tracker includes classes to handle apple events that to compile require the Apple Java Extensions library (AppleJavaExtensions.jar) which can be downloaded <a href="http://www.cabrillo.edu/~dbrown/tracker/osx_services/AppleJavaExtensions.jar" target="_blank">here</a>.


Installation
============

DEB-based:

```
sudo sh -c "echo 'deb http://download.opensuse.org/repositories/home:/NickKolok:/osptracker/xUbuntu_16.04/ /' > /etc/apt/sources.list.d/home:NickKolok:osptracker.list"
wget -nv https://download.opensuse.org/repositories/home:NickKolok:osptracker/xUbuntu_16.04/Release.key -O Release.key
sudo apt-key add - < Release.key
sudo add-apt-repository ppa:jonathonf/ffmpeg-4
sudo apt-get update
sudo apt-get install osptracker
```

RPM-based:
see https://software.opensuse.org//download.html?project=home%3ANickKolok%3Aosptracker&package=osptracker
