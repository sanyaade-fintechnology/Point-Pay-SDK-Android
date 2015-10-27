[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square)]()
[![Version](https://img.shields.io/badge/version-1.1.0-brightgreen.svg?style=flat-square)]()
[![API](https://img.shields.io/badge/API-14%2B-orange.svg?style=flat-square)]()
[![Berlin](https://img.shields.io/badge/Made%20in-Berlin-red.svg?style=flat-square)]()

# payleven mPOS SDK

This project provides an Android API to communicate with the payleven Chip & PIN card reader in order to accept debit and credit card payments. With version 1.1.0, the project also provides an Android API for refund payments. Learn more about the Chip & PIN card reader and payment options on one of payleven's regional [websites](https://payleven.com/). If you have any further questions please contact us by sending an email to developer@payleven.com.
### Prerequisites
1. Register at [payleven](http://payleven.com) in order to get personal merchant credentials and a card reader.
2. Request an API key by registering at the [payleven developer page](https://service.payleven.com/uk/developer).

### Table of Contents
* [Installation](#installation)
  * [Requirements](#requirements)
  * [Repository](#repository)
  * [Dependencies](#dependencies)
  * [GSON](#gson)
  * [Eclipse integration](#eclipse-integration)
* [Usage](#usage)
  * [Permissions](#permissions)
  * [Services](#services)
  * [Bluetooth pairing](#bluetooth-pairing)
  * [Getting Started](#getting-started)
    * [Authenticate your app](#authenticate-your-app)
    * [Select the card reader](#select-the-card-reader)
    * [Prepare card reader for payment](#prepare-card-reader-for-payment)
    * [Handle payment](#handle-payment)
    * [Example of a card reader chip payment](#example-of-a-card-reader-chip-payment)
    * [Example of a card reader swipe payment](#example-of-a-card-reader-swipe-payment)
    * [Refund payment](#refund-payment)
  * [Documentation](#documentation)
  * [mPOS Sample App](#mpos-sample-app)

### Installation
#### Requirements
* **mPOS SDK**: Android API 14 or later.
* **mPOS Sample App**: Android API 14 or later.

#### Repository
Include payleven repository to the list of build repositories:
###### Gradle
 ```groovy
 repositories {
     maven{
         url 'https://download.payleven.de/maven'
     }
 }
 ```
  
###### Maven
 ```xml
 <repositories>
         ...
     <repository>
         <id>payleven-repo</id>
         <url>https://download.payleven.de/maven</url>
     </repository>
 </repositories>
 ```
  
#### Dependencies

###### Gradle
 ```groovy
 //Use the specific library version here
 compile 'de.payleven.payment:mpos:1.1.0@jar'
 //These are helper payleven libraries.
 compile 'de.payleven:psp-library:1.0.0@aar'
 compile 'de.payleven:psp-library-core:1.0.0'
 ```
  
###### Maven
 ```xml
 <dependency>
   <groupId>de.payleven.payment</groupId>
   <artifactId>mpos</artifactId>
   <version>1.1.0</version>
   <type>aar</type>
 </dependency>
 <dependency>
   <groupId>de.payleven</groupId>
   <artifactId>psp-library</artifactId>
   <version>1.0.0</version>
   <type>aar</type>
 </dependency>
 <dependency>
    <groupId>de.payleven</groupId>
    <artifactId>psp-library-core</artifactId>
    <version>1.0.0</version>
    <type>jar</type>
  </dependency>
 ```


#### GSON
When using payleven GSON library is also required:
###### Gradle
 ```groovy
 compile 'com.google.code.gson:gson:2.3'
 ```
  
###### Maven
 ```xml
 <dependency>
   <groupId>com.google.code.gson</groupId>
   <artifactId>gson</artifactId>
   <version>2.3</version>
 </dependency>
 ```
#### Eclipse integration
Though we strongly recommend using gradle and Android Studio, we also provided the sample for
eclipse users.
Before importing _sample_eclipse_ project into eclipse you must perform the following steps:
 1. Download latest GSON library from http://mvnrepository.com/artifact/com.google.code.gson/gson and copy it into _sample_eclipse/sample/libs_ folder.
 2. Download latest Payleven mPOS SDK from https://download.payleven.de/maven/de/payleven/payment/mpos and copy it into _sample_eclipse/sample/libs_ folder.
 3. Download Payleven PSP library from https://download.payleven.de/maven/de/payleven/psp-library-core and copy it into _sample_eclipse/psp-library/libs_ folder

### Usage
#### Permissions
Add the following permissions to be able to pay with payleven card reader:
 ```xml
 <uses-permission android:name="android.permission.BLUETOOTH" />
 <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 <uses-permission android:name="android.permission.INTERNET" />
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  ```
    
#### Services
Also the following services and receivers must be added: 
 ```xml
 <service android:name="de.payleven.payment.PaylevenCommunicationService"
     android:exported="false"
     android:process=":payleven"/>
 
 <!-- Required for bluetooth communication with the terminal -->
 <receiver android:name="com.adyen.adyenpos.receiver.BluetoothState">
     <intent-filter>
         <action android:name="android.bluetooth.adapter.action.STATE_CHANGED"/>
         <action android:name="android.bluetooth.device.action.UUID"/>
     </intent-filter>
 </receiver>
 
 <!-- Required for bluetooth communication with the terminal -->
 <service android:name="com.adyen.adyenpos.service.TerminalConnectIntentService" />
 ```
#### Bluetooth pairing
Before proceeding with the integration and testing make sure you have paired the card reader in the bluetooth settings on your Android device.
 1. Make sure the device is charged and turned on.
 2. Press '0' key on the card reader for 5 sec and make sure the card reader has entered the pairing mode (there will be a corresponding sign on the screen).
 3. Go to the bluetooth settings of your Android device and turn on the 'device scanning' mode.
 4. Select the discovered payleven card reader and follow the instructions on both devices to finish the pairing process.

#### Getting started
##### Authenticate your app
Use the API key received on the payleven developers portal together with merchant credentials to autheticate your app and get an instance of `Payleven` object.
 ```java
 public class MainActivity extends Activity {
  private Payleven mPaylevenApi;
  ...
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      PaylevenFactory.registerAsync(getApplicationContext(),
              "<MERCHANT_EMAIL>", 
              "<MERCHANT_PASSWORD>", 
              "<YOUR_API_KEY>",
              new PaylevenRegistrationListener() {
                  @Override
                  public void onRegistered(Payleven payleven) {
                      mPaylevenApi = payleven;
                  }
                  @Override
                  public void onError(PaylevenError error) {
                      //Process the error
                  }
              });
      ...
   }
 }
 ```
  
##### Select the card reader
Once `Payleven` instance is created you need to select the card reader for your future payments:

 ```java
 List<PairedDevice> devices = mPaylevenApi.getPairedDevices()
 //Display the list of devices
 ...
 //Get the selected device from the list
 PairedDevice paireDevice = devices.get(selectedIndex);
 ```

##### Prepare card reader for payment
 ```java
 private void prepareDevice(PairedDevice pairedDevice) {
     mPaylevenApi.prepareDevice(pairedDevice, new DevicePreparationListener() {
                 @Override
                 public void onDone(Device preparedDevice) {
                     startPayment(preparedDevice,
                             new BigDecimal("1.0"),
                             Currency.getInstance("EUR"));
                 }

                 @Override
                 public void onError(PaylevenError error) {
                     //Show error message
                 }
             }
     );
   }
 ```
  
##### Handle payment
Initialize the actual payment request. For security purposes you must provide the user's current location in the PaymentRequest.

 ```java
 private void startPayment(Device device, BigDecimal amount, Currency currency) {
     //Generated unique payment id
     String generatedPaymentId = "unique_payment_id";
     //Current location of the device
     GeoLocation location = new GeoLocation(lattitude, longitude);
     PaymentRequest paymentRequest = new PaymentRequest(amount, currency,
                                                 generatedPaymentId, location);
     PaymentTask paymentTask = mPaylevenApi.createPaymentTask(paymentRequest, device);
     paymentTask.startAsync(new PaymentListener() {
         @Override
         public void onPaymentComplete(PaymentResult paymentResult) {
             //Handle payment result
         } 

         @Override
         public void onSignatureRequested(SignatureResponseHandler signatureHandler) {
                 //Provide signature image to SignatureResponseHandler
                 //This method is called when a bank card requires 
                 //signature verification instead of a pin
         }

         @Override
         public void onError(PaylevenError error) {
             //Show error message
         }
     });
   }
 ```

##### Example of a card reader chip payment
```
Pan = 7445
PanMaximumLength = 16
PanSequence = 00
PosEntryMode = ICC
CardBrandId = mastercard_mastercard
CardBrandName = MasterCard
Cvm = SIGNATURE
Aid = A0000000041020
AuthCode = 030720
ExpiryYear = 2017
ExpiryMonth = 3
```

##### Example of a card reader swipe payment
```
Pan = 7445
PanMaximumLength = 16
PosEntryMode = MAGNETIC_READER
CardBrandId = mastercard_mastercard
CardBrandName = MasterCard
AuthCode = 010150
ExpiryYear = 2017
ExpiryMonth = 3
```

##### Refund payment
Allows to refund a previous payment. Initialize the refund payment request and create a refund task.

Please note that:
- **paymentId**: Identifier of the initial payment which is to be refunded, called `generatedPaymentId` in the [Handle payment](#handle-payment) section.
- **generatedRefundId**: Identifier of the refund payment.

 ```java
 private void doRefund(String paymentId, BigDecimal amount, Currency currency,
                                                    String description) {
     //Generated unique refund id
     String generatedRefundId = "unique_refund_id";
     RefundRequest request = new RefundRequest(generatedRefundId, paymentId, amount,
                                                    currency, description);
     RefundTask refundTask = mPaylevenApi.createRefundTask(request);
     refundTask.startAsync(new RefundListener() {
         @Override
         public void onError(PaylevenError error) {
             //Show error message
         }

         @Override
         public void onRefundComplete(RefundResult refundResult) {
             //Handle refund result
         }
     });
   }
 ```
      
#### Documentation
[API Reference](http://payleven.github.io/mPOS-SDK-Android/1.1.0/javadoc/)

#### mPOS Sample App
The mPOS SDK includes a sample app that shows how the SDK can be integrated. From this sample app is possible to choose a card reader, make payments and refund them. It contains as well a Signature View where the user can sign in case the transaction requires a signature.
Note that the location is hardcoded and needs to be changed depending on the country the user is paying from.
