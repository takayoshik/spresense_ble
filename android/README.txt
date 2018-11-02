ROHM MK7125-002 BLE peripheral module driver for android application

0. How to change MAC address
	1. Open MainActiviti.java
	2. Change MAC_ADDRESS text
		private final String MAC_ADDRESS = "00:20:8B:AA:55:01";
		-> private final String MAC_ADDRESS = "00:20:8B:AA:55:XX";
		(XX = Board No.)

1. How to import project into Android studio
	1. File -> New -> Import project
	2. Select ROHM_BLE_Sample_app

2. How to build
	1. Build -> Rebuild project

3. How to run project
	1. Run -> Run 'app'