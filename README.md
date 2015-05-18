# BeaconsChart
Visualizes beacons to device distance on chart. 
Beacon communication by org.altbeacon:android-beacon-library. 
It also utilizes com.github.PhilJay:MPAndroidChart and com.jakewharton:butterknife

Usage:
Works with Estimote beacons by default. It can be configured to work with other beacons by edited line:

beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
in BApplication.java file.

e.g. for AltBeacons:
beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

By default application display data of beacons present on FRIENDLY_NAMES list containing beacons Major Ids(id2) of MainActivity.java file. 
To show all beacons nearby, remove check in "known only" menu item of options menu.
