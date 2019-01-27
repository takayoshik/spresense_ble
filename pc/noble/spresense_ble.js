var noble = require('noble');

noble.on('stateChange', function(state) {
  if (state === 'poweredOn') {
    noble.startScanning(["180a"]);
  } else {
    noble.stopScanning();
  }
});

var BLE_READ;
var size = 0;
noble.on('discover', function(peripheral) {
    console.log('on -> discover: ' + peripheral);

    noble.stopScanning();
 
    peripheral.on('connect', function() {
        console.log('on -> connect');
        this.discoverServices();
    });

    peripheral.on('disconnect', function() {
        console.log('on -> disconnect');
    });
 
    peripheral.on('servicesDiscover', function(services) {
 
        for(i = 0; i < services.length; i++) {
 
            if(services[i]['uuid'] == '0179bbd0535148b5bf6d2167639bc867'){
                console.log('on -> service services[' + i + '][uuid] ==' + services[i]['uuid']);
 
                services[i].on('includedServicesDiscover', function(includedServiceUuids) {
                    console.log('on -> service included services discovered [' + includedServiceUuids + ']');
                    this.discoverCharacteristics();
                });
 
                services[i].on('characteristicsDiscover', function(characteristics) {
 
                    for(j = 0; j < characteristics.length; j++) {
                        if (characteristics[j].uuid == '0179bbd1535148b5bf6d2167639bc867') {
                            console.log(characteristics[j]);
                            console.log('on -> characteristics[' + j + '].uuid ==' + characteristics[j].uuid);
                            BLE_READ = characteristics[j];
                        }
                    }

                    BLE_READ.discoverDescriptors(function(error, descriptors) {
                        console.log('length dsc=' + descriptors.length);
                        for (n = 0; n < descriptors.length; n++) {
                            console.log(descriptors[n]);
                            var data = Buffer([0x01, 0x00]);
                            descriptors[n].writeValue(data, function(error) {});
                            descriptors[n].readValue(function(error, data) {
                                console.log("Read" + data);
                            });
                        }
                    });

              //get notify data
              BLE_READ.on('data', function(data, isNotification) {
                var result = "";
                //console.log("Length=" + data.length);
                //for(var i=0; i<data.length; i++){
                  //result += data[i] + ',';
                //}
                size += data.length;
                console.log(size);
              });


                });

 
                services[i].discoverIncludedServices();
            }
        }
 
    });
 
    peripheral.connect();
});
