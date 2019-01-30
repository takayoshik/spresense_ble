#include "MK71251.h"
#include <string.h>
#include <Camera.h>

MK71251 mk71251;

void CamCB(CamImage img)
{

  /* Check the img instance is available or not. */

  if (img.isAvailable())
    {

      /* If you want RGB565 data, convert image data format to RGB565 */

      img.convertPixFormat(CAM_IMAGE_PIX_FMT_RGB565);

    }
}

void setup() {
  /* begin() without parameters means that
   * number of buffers = 1, 30FPS, QVGA, YUV 4:2:2 format */

  Serial.println("Prepare camera");
  theCamera.begin();


  /* Start video stream.
   * If received video stream data from camera device,
   *  camera library call CamCB.
   */

  Serial.println("Start streaming");
  theCamera.startStreaming(true, CamCB);

  /* Auto white balance configuration */

  Serial.println("Set Auto white balance parameter");
  theCamera.setAutoWhiteBalanceMode(CAM_WHITE_BALANCE_DAYLIGHT);
 
  /* Set parameters about still picture.
   * In the following case, QUADVGA and JPEG.
   */

  Serial.println("Start streaming");
  theCamera.setStillPictureImageFormat(
     CAM_IMGSIZE_QVGA_H,
     CAM_IMGSIZE_QVGA_V,
     CAM_IMAGE_PIX_FMT_JPG);
  
  // put your setup code here, to run once:
  mk71251.init();
  sleep(2);
  theCamera.takePicture();
  sleep(1);
  
}

#define DATA_SIZE 1024 * 4

uint8_t data[DATA_SIZE];
size_t length = 0;
void push() {
  if (0 < length) {
    int rc = mk71251.write(data, length);
    if (rc != length) {
      printf("Data transfer error %d != %d\n", length, rc);
    }
    
    length = 0;
  }
}

void pop(uint8_t val) {
  data[length] = val;
  length ++;
  if (length == DATA_SIZE) {
    push();
  }
}
void send(uint8_t *buff, size_t size) {
  int send_size = 0;
  pop(0x2C);
  pop(0x02);
  for (int n = 0; n < size; n ++) {
    if (buff[n] != 0x2B /* + */ && buff[n] != 0x2C /* , */) {
      pop(buff[n]);
    } else {
      pop(0x2C);
      if (buff[n] == 0x2C) {
        pop(0x00);
      } else {
        pop(0x01);
      }
    }
  }
  push();
  pop(0x2C);
  pop(0x03);
}

void loop() {
  ble_printf("Loop!!!\n");
  CamImage img = theCamera.takePicture();
  ledOff(PIN_LED3);
  
  if (img.isAvailable() && mk71251.available())
    {

      /* Save to SD card as the finename */

      /*File myFile = theSD.open(filename, FILE_WRITE);
      myFile.write(img.getImgBuff(), img.getImgSize());
      myFile.close();*/
      send(img.getImgBuff(), img.getImgSize());
      printf("Send jpeg size = %08d\n", img.getImgSize());
      ledOn(PIN_LED3);
    }
}
