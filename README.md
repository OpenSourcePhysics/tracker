tracker
=======

Video analysis and modeling tool built on the Open Source Physics (OSP) framework.

This code requires the OSP Core Library available in the <a href="https://github.com/OpenSourcePhysics/osp" target="_blank">OpenSourcePhysics/osp</a> repository.

Optional video engine support (Xuggle on Win/OSX/linux, QuickTime on Win/OSX) is available in the <a href="https://github.com/OpenSourcePhysics/video-engines" target="_blank">OpenSourcePhysics/video-engines</a> repository. Without a video engine Tracker will only open images (JPEG, PNG) and animated GIFs.

Note: branch "forOSX" includes additional classes to handle apple events when running on OSX. Compiling this branch requires the Apple Java Extensions library (AppleJavaExtensions.jar) which can be downloaded <a href="http://www.cabrillo.edu/~dbrown/tracker/osx_services/AppleJavaExtensions.jar" target="_blank">here</a>.
