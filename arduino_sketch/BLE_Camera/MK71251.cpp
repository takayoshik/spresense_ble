/*****************************************************************************
  MK71251.cpp

  Copyright (c) 2018 ROHM Co.,Ltd.

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
******************************************************************************/

#include "Arduino.h"
#include "MK71251.h"

#include <sched.h>

#define STACKSIZE 8192
#define PRIORITY  100


MK71251::MK71251(void)
{

}

#define RECV_BUFFER_SIZE 128
#define RECV_INTERVAL_USEC 1000 * 10

#define ROHM_ERROR_MSG      "ERROR"
#define ROHM_NO_CARRIER_MSG "NO CARRIER"
#define ROHM_CONNECT_MSG    "CONNECT"

static int isConnected = 0;

static void reStandby(void) {
  sleep(1);
  
  // Flush
  Serial2.write("\r\n");
  
  // back to AT mode
  Serial2.write("+++AT\r\n");

  // start advertising
  Serial2.write("ATD\r\n");
}

static void reply_rcv(char *msg) {
  ble_printf("Received message = %s\n", msg);
  if (!strcmp(msg, ROHM_CONNECT_MSG)) {
    ble_printf("Connected...\n");
    sleep(1);
    isConnected = 1;
    ledOn(PIN_LED2);
  } else if (!strcmp(msg, ROHM_NO_CARRIER_MSG)) {
    ble_printf("Disconnected...\n");
    isConnected = 0;
    sleep(5);
    reStandby();
    ledOff(PIN_LED2);
  } else if (!strcmp(msg, ROHM_ERROR_MSG)) {
    ble_printf("Got error\n");
  }
}

static int recv_task(int argc, char *argv[]) {
  char ch     = '\0';
  char pre_ch = '\0';
  char recv_str[RECV_BUFFER_SIZE];
  int pos;

  while (1) {
    for (pos = 0, ch = '\0', pre_ch = '\0'; pos < RECV_BUFFER_SIZE; )
    {
      ch = Serial2.read();

      if (ch != '\0' && ch != 255) {
        if (pre_ch == '\r' && ch == '\n') {
          pos = pos - 1;
          recv_str[pos] = '\0';
          break;
        } else {
          recv_str[pos] = ch;
          pre_ch = ch;
          pos ++;
        }
      }
      usleep(RECV_INTERVAL_USEC);
    }
    if (pos != 0) {
      reply_rcv(recv_str);
    }
  }
}

byte MK71251::init(void)
{

  // configure Output for GPIO3 (High: HCI mode, Low: AT command mode)
  pinMode(PIN_D21, OUTPUT);
  digitalWrite(PIN_D21, LOW);

  // reset BLE chip
  delay(500);
  pinMode(PIN_D20, OUTPUT);
  digitalWrite(PIN_D20, LOW);
  delay(10);
  digitalWrite(PIN_D20, HIGH);
  delay(20);

  ledOn(PIN_LED0);

  Serial2.begin(57600);

  printf("MK71251-02: waiting for CTS:LOW...\n");

  while (1) {
    int cts = digitalRead(PIN_D27);
    if (!cts) break;
  }

  ledOn(PIN_LED1);

  printf("MK71251-02: CTS:LOW read succesfully\n");

  // define advertising data
  // 020106 -> Flags: LE General Discoverable Mode, BR/EDR Not Supported
  // 05030f18180a -> Complete List of 16bit Service UUIDs: 180f, 180a
  // 09084c61706973446576 -> Complete Local Name: LapisDev
  Serial2.write("ATS150=02010605030f180a1809084c61706973446576\r\n");
  //Serial2.write("ATS150=02010605030f180a18090A5350524553454E5345454545\r\n");

  // set public address
  Serial2.write("ATS127=00208BAA5501\r\n");

  // set mac address to public address
  Serial2.write("ATS102=0\r\n");
  
  // set connection interval time
  //Serial2.write("ATS105=28\r\n");
  
  // set MTU size
  //Serial2.write("ATS129=17\r\n");
  
  // start advertising
  Serial2.write("ATD\r\n");

  printf("MK71251-02: waiting for connection...\n");

  while (1) {
    int ret = waitConnect();
    if (ret) break;
  }
  isConnected = 1;
  
  ledOn(PIN_LED2);

  printf("MK71251-02: connected succesfully\n");

  task_create("ble_recv_task", PRIORITY, STACKSIZE, recv_task, NULL);

  printf("BLE Camera start\n");
  
  return (0);
}

byte MK71251::write(unsigned char *data)
{
  int rc;
  rc = Serial2.write(*data);
  if (rc != 0) {
    return (0);
  }
  else {
    return (-1);
  }
}

int MK71251::write(const uint8_t* data, size_t size)
{
  int rc;
  rc = Serial2.write(data, size);
  return rc;
}

byte MK71251::read(unsigned char *data)
{
  char c;
  if ((c = Serial2.read()) && c != 255) {
    *data = c;
    return (0);
  }
  else {
    return (-1);
  }
}

int MK71251::available(void)
{
  return isConnected;
}

int MK71251::waitConnect(void)
{
  char c = 0;
  char ret[128];
  int n;

  for (n = 0; n < 128 && c != '\n';)
  {
    c = Serial2.read();
    if (c != 0 && c != 255) {
      ret[n] = c;
      n++;
    }
  }
  ret[n - 2] = '\0';
  int cmp = strcmp(ret, "CONNECT");
  return cmp == 0 ? 1 : 0;
}
