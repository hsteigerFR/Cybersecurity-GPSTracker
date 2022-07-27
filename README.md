# Cybersecurity - GPS Tracker

Authors : Hugo Steiger, Celian Muller-Machi, Baptiste Brunet de la Charie

This project was led as part of the "Security of Cyberphysical Systems" course at Mines Nancy Computer Science departement. The goal of the project was to exploit the vulnerabilities of a system as well as their user's. My team chose to work on an Android GPS Tracker malware that can act with being seen by the user. Social engineering and email identity theft (for phishing) were also studied as part of the project. Here is an illustration of how the malware works  :

![Strategy](https://user-images.githubusercontent.com/106969232/180282909-3de55630-6f57-40a1-ba79-b8be774cf5cf.JPG)

Once the malware app is activated on the phone, a foreground service will be created and remain active even if the main app is closed or the phone turned off. It will not be found in the "active apps" menu. In the given code, the main app gets closed automatically after the service is created. A background service does not have offer this opportunity. Nevertheless, a foreground service is visible in the Android notification toolbar and an icon represents it at the upper left of the screen : it is an Android security measure. Still, the notification can blend in as it can replace any commonly active foreground service, and the upper left icon can be made completly transparent.

The foreground service will regularly call the phone geolocation service to retrieve the last GPS coordinates of the phone and send it to a Ngrok public address through the HTTP protocol. The Ngrock public address will be bound to a local Python server through port 5000, and the coordinates will be saved on the hacker's computer. Many phones can be infected and send their coordinates at once : the Python server will be able to differenciate each phone connected and the save the follow up in a .csv.

As part of the Android security protocol, some permissions must be enabled by the user to make this strategy work properly, or the geolocation will not work on the long run. This is where social engineering comes in. The code introduced in this repo can be added to the code of a "clasic" app, that will genuinely ask for the appropriate permissions, and make it a malware. The only hint the user may have is the low battery life of the phone once the GPS trakcer is activated, and the geolocation icon on the upper right of the phone.

<img src="https://user-images.githubusercontent.com/106969232/180289339-b606fc80-e2f2-4dcc-9ad5-d1519fba71d0.png" width="300" height="345">

HOW TO USE :
- git pull this repo
- Replace "bin/ngrok.exe.txt" by a working instance of ngrok.exe
- Run "launch.bat" : it will open ngrok.exe and the local Python server -> everything that is sent on ngrok.exe public HTTP address will be interpreted by the Python server
- Open "GPS Tracker" as an Android Studio project and let it Gradle build
- Go to GPS Tracker/app/src/main/java/com/example/test/ForegroundService.java and replace the URL (line 48) by ngrok.exe HTTPS public address. It will not work without https.
- Build the app and put the .apk on your phone
- Go to the app details and set the following permissions :
*Location Access for this App :* Allow all the time; *Allow background activity :* On
- Run the app, it should close and the server will start receiving the phone coordinates thanks to the foreground service
- To end the service, go to the app details and force stop

Apk compiled with Android Studio | Arctic Fox 2020.3.1 Patch 3, on Windows 10 (x64) - Version 10.0.19043 Build 19043. Python version used to run the server :  Python 3.9.1. Used Ngrok.exe version : 2.3.40. Phone model and Android version : Galaxy A50, Android 11.
