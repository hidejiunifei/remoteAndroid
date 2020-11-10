# remoteAndroid

Arduino used Uno  
otg adapter used  
Using the otg the code can also be deployed by ArduinoDroid  
uses-permission used android.permission.RECORD_AUDIO  
Additional configuration for project gradle:  

    allprojects {  
        repositories {  
            google()  
            jcenter()  
            maven { url "https://jitpack.io" }  
        }  
    }  

Additional configuration for app gradle:  

implementation "com.github.felHR85:UsbSerial:4.5"  

Serial configuration:  

BaudRate = 9600  
DataBits = UsbSerialInterface.DATA_BITS_8  
StopBits = UsbSerialInterface.STOP_BITS_1  
Parity = UsbSerialInterface.PARITY_NONE  
FlowControl = UsbSerialInterface.FLOW_CONTROL_OFF  

To receive every word separately on Arduino I replaced the spaces with \n:  

sendData(string.replace(' ','\n'))  
