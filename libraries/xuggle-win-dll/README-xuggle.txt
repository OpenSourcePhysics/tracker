xuggle-xuggler-w64.jar is from Olivier Ayache 2021.01.16 personal communication with Bob

It is for Xuggler 5.7.0 
The three dlls in this directory must be on the Windows PATH. 
I put them in windows/system32, myself.

When Tracker is started, a call to IContainer.make() from media.xuggle.DiagnosticsForXuggle 
will be executed. Successful initiation is indicated by creation of a 23.5 MB xuggleXXXXXX.dll 
temp file in $USER$\AppData\Local\Temp\xuggle and no Exception or Error thrown.

BH 2021.01.16