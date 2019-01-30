/*****************************************************************************
  MK71251.h
  
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

#ifndef _MK71251_H_
#define _MK71251_H_

#define ENABLE_DEBUG

#ifdef ENABLE_DEBUG
#define ble_printf(format, ...) printf(format, ##__VA_ARGS__)
#else
#define ble_printf(format, ...)
#endif
class MK71251
{
	public:
		MK71251(void);
		byte init(void);
    byte write(unsigned char *data);
    int write(const uint8_t* data, size_t size);
		byte read(unsigned char *data);
    int available(void);
	private:
		int waitConnect(void);
};

#endif // _MK71251_H_
