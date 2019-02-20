# pgs4a
An evolution of the deprecated Pygame Subset for Android.
Pgs4a was made by Tom Rothamel and others, using Kivy's *python-for-android* packager to make Pygame games run on android.
He developed pgs4a until 2013 until he moved on with his other projects.
Meanwhile, I made a couple of edits to pgs4a for it to work nicely in the constantly evolving android environment. It's an easy way to port your Pygame to your mobile devices!

# changelog

### 0.9.6 (last release by Tom)
* This release patches Python to work with .apk files distributed through the Amazon App Store, which are subtly different from standard apk files.
* This release fixes support for pre-2.3 Android devices

### 0.9.7 (my changes)
* immersive full screen mode (api 19+)
* removed all permissions (storage, wakelock)
* two-finger multitouch implementation
* target SDK of project can be configured by user
* added full keyboard and mouse (api 21+) support
* autobackup via google, for specified files (api 23+)
* resizeable activity (api 24+)
* adaptive icon (api 26+)
* additional 64-bit libraries

# instructions
1. Clone this repository (or download it as zip and extract it somewhere)
1. Go into the folder, where you'll find the file *android.py* and some other files and folders. Create a new folder here and put all your game files in it (or copy a folder to this place). From now on, this folder will be refered as *yourproject* in the instructions below.
1. Open a terminal there (right click / in Windows hold shift then right click to open a shell/prompt).
1. Run `python ./android.py installsdk`. This will download and extract the android SDK and Ant.
1. Run `python ./android.py configure yourproject`, where you replace *yourproject* with the name of your own project folder. A json file will be added to your folder.
1. Connect your phone to your PC via USB. Make sure USB debugging is enabled (see developer options).
1. Run `python ./android.py install yourproject release install`. An APK should be created and installed on your phone. It will run your app immediately, in theory.
1. If the app installs, but crashes immediately, use *adb logcat* to find out why. Navigate to *android-sdk/platform-tools* and run `./adb logcat`. Open your app, and when it crashes, press *Ctrl + C* to stop logging, and look for errors. Your project folder has to contain a file called *main.py*, so you may have to rename one of your files and try again.

The original instructions can be found [here](https://github.com/startgridsrc/rapt/blob/master/doc/android-packaging.rst).
These are the original instructions which are old but detailed, and most of it is probably still fine. I plan to to create new instructions later.
