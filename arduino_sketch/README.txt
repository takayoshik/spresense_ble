ROHM MK7125-002 BLE peripheral application for arduino sketch

0. How to change MAC address
	1. Open MK71251.cpp
	2. Change MAC_ADDRESS text
		Serial2.write("ATS127=00208BAA5501\r\n");
		->   Serial2.write("ATS127=00208BAA55XX\r\n");
		(XX = Board No.)

1. How to import project into Android studio
	1. Copy "MK71251-02" directory to <Your document directory>/Arduino/libraries
