Converting Thai properties files from Thammarong Eadkhong to latin with escapes:

--rename Thammarong's files using extension utf rather than properties
--in the Command Prompt window, enter (without quotes) eg:  "thaiconv -r tracker_th_TH.utf -out 0 > tracker_th_TH.properties" to convert to TIS-620
--copy eg tracker_th_TH.properties file to C:\Users\Doug\Eclipse\workspace_develop\tracker\build\properties 
--run the Tracker Develop "encode_thai_properties" ant target to convert the TIS-620 .properties files to escaped latin .props files


thaiconv
(c) Lyndon Hill, 2014.

Introduction

This is a program to convert Thai text files between formats.

Help is available by invoking $ thaiconv -h

Up to date information and instructions regarding this program can be found at
http://www.lyndonhill.com/Projects/thaiconv.html

Licence

thaiconv is compiled from original code exclusively owned and controlled by
Lyndon Hill. thaiconv is free, no charge may be made whatsoever for copying,
transferring or distributing. Whenever thaiconv is distributed, this ReadMe.txt
file must be included in its entirety without modification or addition.

thaiconv is supplied without any warranty, explicit or implied. Use at your
own risk.

28.02.2014
