[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square)]()
[![Version](https://img.shields.io/badge/version-1.2.1-brightgreen.svg?style=flat-square)]()
[![API](https://img.shields.io/badge/API-14%2B-orange.svg?style=flat-square)]()
[![Berlin](https://img.shields.io/badge/Made%20in-Berlin-red.svg?style=flat-square)]()

# payleven Point Pay SDK

This project enables an Android API to communicate with the payleven Classic (Chip & PIN) and Plus (NFC) card reader to accept debit and credit card payments. Learn more about the card readers on one of payleven's country [websites](https://payleven.com/).
From version 1.1.0 onwards, the payleven Point Pay SDK provides an API to process full and partial refunds. Additionally, the SDK issues a receipt image of both sale and refund payments that contain the bare minimum of receipt details. If you have any questions or require further assistance, please contact <a href="mailto:developer@payleven.com">developer@payleven.com</a>.

> Note: 
> The product has been renamed to payleven Point Pay SDK from mPOS SDK. Any references within the documentation or classes are relevant to payleven Point Pay SDK.

### Prerequisites
1. Register on one of payleven's country [websites](https://payleven.com/) to get a merchant account and a card reader.
2. Request an API key by registering for the Point Pay SDK on the [payleven developer page](https://service.payleven.com/uk/developer).
3. System requirements: Android API 14 or later for both Point Pay SDK and the Point Pay Sample App.
4. A payleven card reader, Classic or Plus.

### Table of Contents
* [Installation](#installation)
  * [Repository](#repository)
  * [Dependencies](#dependencies)
  * [GSON](#gson)
  * [OkHttp](#okhttp)
  * [Eclipse integration](#eclipse-integration)
  * [Permissions](#permissions)
  * [Services](#services)
* [Getting Started](#getting-started)
  * [Login](#login)
  * [Bluetooth pairing](#bluetooth-pairing)
  * [Select a device](#select-a-device)
  * [Prepare device for payment](#prepare-device-for-payment)
* [Payment](#payment)
  * [Start payment](#start-payment)
  * [Handle payment](#handle-payment)
* [Refund](#refund)
  * [Start refund](#start-refund)
  * [Handle refund](#handle-refund)
* [Receipts](#receipts)
* [Point Pay SDK Sample App](#point-pay-sdk-sample-app)
* [Documentation](#documentation)


### Installation

#### Repository
Include payleven repository to the list of build repositories in Gradle or Maven, depending on the build environment you work with.
###### Gradle
In the module's gradle.build file:
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
Include the following dependencies from payleven repository in Gradle or Maven depending on the build environment you work with.

###### Gradle
In the module's gradle.build file, inside the dependencies section:
 ```groovy
 //Use the specific library version here
 compile 'de.payleven.payment:mpos:1.2.1@jar'
 //These are helper payleven libraries.
 compile 'de.payleven:psp-library:1.2.0@aar'
 compile 'de.payleven:psp-library-core:1.2.0'
 ```
  
###### Maven
In the project pom.xml file:
 ```xml
 <dependency>
   <groupId>de.payleven.payment</groupId>
   <artifactId>mpos</artifactId>
   <version>1.2.1</version>
   <type>aar</type>
 </dependency>
 <dependency>
   <groupId>de.payleven</groupId>
   <artifactId>psp-library</artifactId>
   <version>1.2.0</version>
   <type>aar</type>
 </dependency>
 <dependency>
    <groupId>de.payleven</groupId>
    <artifactId>psp-library-core</artifactId>
    <version>1.2.0</version>
    <type>jar</type>
  </dependency>
 ```


#### GSON
Include GSON library for object serialization/deserialization handling.
###### Gradle
In the module's gradle.build file, inside the dependencies section:
 ```groovy
 compile 'com.google.code.gson:gson:2.4'
 ```
  
###### Maven
In the project pom.xml file:
 ```xml
 <dependency>
   <groupId>com.google.code.gson</groupId>
   <artifactId>gson</artifactId>
   <version>2.4</version>
 </dependency>
 ```
 
#### OkHttp
Include OkHttp library as the http client used by the SDK.
###### Gradle
In the module's gradle.build file, inside the dependencies section:
 ```groovy
 compile 'com.squareup.okhttp:okhttp:2.7.5'
 ```
  
###### Maven
In the project pom.xml file:
 ```xml
 <dependency>
   <groupId>com.squareup.okhttp</groupId>
   <artifactId>okhttp</artifactId>
   <version>2.7.5</version>
 </dependency>
 ```
 
#### Eclipse integration
Though we strongly recommend using Gradle and Android Studio, we also provided the sample for
Eclipse users.
Before importing _sample_eclipse_ project into Eclipse you must perform the following steps:
 1. Download latest GSON library from http://mvnrepository.com/artifact/com.google.code.gson/gson and copy it into _sample_eclipse/sample/libs_ folder.
 2. Download latest payleven Point Pay SDK from https://download.payleven.de/maven/de/payleven/payment/mpos and copy it into _sample_eclipse/sample/libs_ folder.
 3. Download Payleven PSP library from https://download.payleven.de/maven/de/payleven/psp-library-core and copy it into _sample_eclipse/psp-library/libs_ folder

#### Permissions
Add the following permissions to the AndroidManifest.xml file to be able to pay with payleven's card reader:
 ```xml
 <uses-permission android:name="android.permission.BLUETOOTH" />
 <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 <uses-permission android:name="android.permission.INTERNET" />
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  ```
    
#### Services
Also the following services and receivers must be added to the same AndroidManifest.xml file: 
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

### Getting started
#### Login
 To fetch connected devices, start or refund a payment you must be logged into payleven SDK. Use the API key received from payleven, together with your payleven merchant account (email address & password) to authenticate your app and get an instance of `Payleven` object. Check out our Sample Integration to see how easily you can observe the Login state.
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
 
#### Bluetooth pairing
Before proceeding with the integration and testing, make sure you have paired the card reader in the bluetooth settings on your Android device.
 1. Make sure the device is charged and turned on.
 2. Press '0' key on the card reader for 5 seconds and check that the card reader has entered the pairing mode (there will be a corresponding sign on the screen).
 3. Go to the bluetooth settings of your Android device and turn on the 'device scanning' mode.
 4. Select the "discovered" payleven card reader and follow the instructions on both devices to finish the pairing process.
 
#### Select a device
Once `Payleven` instance is created you need to select the card reader to process payments:

 ```java
 List<PairedDevice> devices = mPaylevenApi.getPairedDevices()
 //Display the list of devices
 ...
 //Get the selected device from the list
 PairedDevice paireDevice = devices.get(selectedIndex);
 ```

#### Prepare device for payment
After a device is selected it needs to be prepared to accept payments. We will run all required preparations and security checks for you. All you have to do is outlined below. Before triggering a new payment you should always get the prepared device from the successful callback "onDone".
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
  
### Payment
#### Start payment
Initialize the actual payment request. For security purposes you must provide the user's current location in the PaymentRequest. The identifier parameter allows you to reference this particular payment for a potential refund in the future. We strongly advise you to save this value in your Backend.

 ```java
 private PaymentRequest startPayment(BigDecimal paymentAmount, Currency paymentCurrency,
                                     double lattitude, double longitude) {
     //Generated unique payment identifier
     String generatedPaymentIdentifier = "unique_payment_identifier";
     //Current location of the device
     GeoLocation paymentLocation = new GeoLocation(lattitude, longitude);
     
     return new PaymentRequest(paymentAmount, paymentCurrency, generatedPaymentIdentifier,
                               paymentLocation);
   }
 ```

#### Handle payment
From v1.2.0 a new callback `onPaymentProgressStateChanged` is provided. This callback sends different payment progress states during a payment. The payment progress state may also be obtained from the `PaymentTask` instance. E.g. `paymentTask.getPaymentProgressState()`.

 ```java
 private void handlePayment(PaymentRequest paymentRequest, Device device) {
     PaymentTask paymentTask = mPaylevenApi.createPaymentTask(paymentRequest, device);
     paymentTask.startAsync(new PaymentListener() {
         @Override
         public void onPaymentComplete(PaymentResult paymentResult) {
             //Handle payment result
         }
         
         @Override
         public void onPaymentProgressStateChanged(PaymentProgressState paymentProgressState) {
             //Optional: handle payment progress state
         }

         @Override
         public void onSignatureRequested(SignatureResponseHandler signatureHandler) {
                 //Provide signature image to SignatureResponseHandler
                 //This method is called when a card requires 
                 //signature verification instead of a pin
         }

         @Override
         public void onError(PaylevenError error) {
             //Show error message
         }
     });
   }
 ```
 
 Optionally, you can offer a more comprehensive user experience by reading the following payment progress states.
  ```java
  @Override
  public void onPaymentProgressStateChanged(PaymentProgressState paymentProgressState) {
      String paymentState = null;
      
      switch (paymentProgressState) {
            case STARTED:
                paymentState = getString(R.string.payment_started);
                break;
            case REQUEST_PRESENT_CARD:
                paymentState = getString(R.string.present_card);
                break;
            case REQUEST_INSERT_CARD:
                paymentState = getString(R.string.insert_card);
                break;
            case CARD_INSERTED:
                paymentState = getString(R.string.card_inserted);
                break;
            case REQUEST_ENTER_PIN:
                paymentState = getString(R.string.enter_pin);
                break;
            case PIN_ENTERED:
                paymentState = getString(R.string.pin_entered);
                break;
            case CONTACTLESS_BEEP_OK:
                paymentState = getString(R.string.beep_ok);
                break;
            case CONTACTLESS_BEEP_FAILED:
                paymentState = getString(R.string.beep_failed);
                break;
            case REQUEST_SWIPE_CARD:
                paymentState = getString(R.string.swipe_card);
                break;
            case NONE:
                paymentState = getString(R.string.none);
                break;
            default:
                break;
        }
  }
 ```
 

### Refund
You can refund a payment conducted via the Point Pay SDK partially or in full.

#### Start refund
Initialize the refund payment request and create a refund task. For a refund you need:

- **paymentIdentifier**: String to specify the original sale payment's Identifier that is supposed to be refunded. In the [Start payment](#start-payment) section, it is called "generatedPaymentIdentifier".
- **generatedRefundIdentifier**: String to uniquely specify the refund.
- **refundAmount**: Decimal number indicating the amount to be refunded, which cannot be higher than the original payment's amount.
- **refundCurrency**: 3 letter ISO character (e.g. EUR) that is identical with the original sale payment's currency.

 ```java
 private RefundRequest startRefund(String paymentIdentifier, BigDecimal refundAmount,
                                   Currency refundCurrency, String refundDescription) {
     //Generated unique refund identifier
     String generatedRefundIdentifier = "unique_refund_identifier"
     
     return new RefundRequest(generatedRefundIdentifier, paymentIdentifier, refundAmount,
                              refundCurrency, refundDescription);
 }
 ```
 
#### Handle refund
Once the refund request is initialized, trigger the refund as outlined below.
 ```java
 private void handleRefund(RefundRequest refundRequest) {
     RefundTask refundTask = mPaylevenApi.createRefundTask(refundRequest);
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
 
### Receipts		
Additionally, the SDK issues a receipt image of sale and refund payments that contains the bare minimum of receipt details. Please keep in mind to extend the image with the merchants name, address and a respective receipt ID. In case you wish to create your own receipt by using a set of raw payment data, please contact <a href="mailto:developer@payleven.com">developer@payleven.com</a>.		
		
```java		
private Bitmap generateReceipt(PaymentResult paymentResult, int width, int textSize,		
                              int lineSpacing) {		
   //Create a configuration for the receipt image.		
   ReceiptConfig receiptConfig = new ReceiptConfig.Builder(width, textSize)		
           .setLineSpacing(lineSpacing)		
           .build();		
		
   //Generate the receipt with ReceiptGenerator and the previous configuration.		
   ReceiptGenerator generator = paymentResult.getReceiptGenerator();		
   return generator.generateReceipt(receiptConfig);		
}		
```

### Point Pay SDK Sample App
The Point Pay SDK includes a sample app illustrating how the SDK can be integrated. 
> Note: 
> - The location is hardcoded and needs to be changed according to user's current position.
> - The Package name of the sample app has been registered already and is uniquely associated to the API key used in the project. In case you change the API key to the one you have obtained from payleven during the developer registration, please remember to change also the Package name to the one you have registered.

The sample integration exemplifies the following:
- It allows to select a card reader, conduct card payments and refund them (full or partial amount).
- A signature view that is required if the payment has to be verified with the customer's signature.
- In alignment with the provided payment progress states (see above under "Payment"), **optional** and customizable UI/design elements are offered for integration to show the payment progress.

```java
private void startPayment() {
        PaylevenTools.showDevicePreparation(getActivity(), mPaymentProgressStateView);
}

@Override
public void onPaymentProgressStateChanged(PaymentProgressState paymentProgressState) {
	PaylevenTools.showPaymentProgressAnimation(
                getActivity(),
                mPaymentProgressStateView,
                paymentProgressState.name());		
}		
```
### Documentation
[API Reference](http://payleven.github.io/Point-Pay-SDK-Android/1.2.1/javadoc/)
