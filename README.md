[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square)]()
[![Version](https://img.shields.io/badge/version-1.2.0-brightgreen.svg?style=flat-square)]()
[![API](https://img.shields.io/badge/API-14%2B-orange.svg?style=flat-square)]()
[![Berlin](https://img.shields.io/badge/Made%20in-Berlin-red.svg?style=flat-square)]()

# payleven mPOS SDK

This project enables an Android API to communicate with the payleven Classic (Chip & PIN) and Plus (NFC) card reader to accept debit and credit card payments. Learn more about the card readers on one of payleven's country [websites](https://payleven.com/).
From version 1.1.0 onwards, the payleven mPOS SDK provides an API to process full and partial refunds. Additionally, the SDK issues a receipt image of both sale and refund payments that contain the bare minimum of receipt details. If you have any questions or require further assistance, please contact <a href="mailto:developer@payleven.com">developer@payleven.com</a>.

### Prerequisites
1. Register on one of payleven's country [websites](https://payleven.com/) to get a merchant account and a card reader.
2. Request an API key by registering for the mPOS SDK on the [payleven developer page](https://service.payleven.com/uk/developer).
3. System requirements: Android API 14 or later for both mPOS SDK and the mPOS Sample App.
4. A payleven card reader, Classic or Plus.

### Table of Contents
* [Installation](#installation)
  * [Repository](#repository)
  * [Dependencies](#dependencies)
  * [GSON](#gson)
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
* [Documentation](#documentation)
* [mPOS SDK Sample App](#mpos-sdk-sample-app)

### Installation

#### Repository
Depending on your chosen build environment, ensure to include the payleven repository to the list of build repositories in either Gradle or Maven.

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
Depending on your chosen build environment, ensure to include the following dependencies from the payleven repository in either Gradle or Maven.

###### Gradle
In the module's gradle.build file, inside the dependencies section:
 ```groovy
 //Use the specific library version here
 compile 'de.payleven.payment:mpos:1.2.0@jar'
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
   <version>1.2.0</version>
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
#### Eclipse integration
Though we strongly recommend using Gradle and Android Studio, we have also provided a sample for
Eclipse users.
Before importing _sample_eclipse_ project into Eclipse, you must perform the following steps:
 1. Download latest GSON library from http://mvnrepository.com/artifact/com.google.code.gson/gson and copy it into _sample_eclipse/sample/libs_ folder.
 2. Download latest Payleven mPOS SDK from https://download.payleven.de/maven/de/payleven/payment/mpos and copy it into _sample_eclipse/sample/libs_ folder.
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
The following services and receivers must also be added to the same AndroidManifest.xml file: 
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
 private PaymentRequest startPayment(BigDecimal amount, Currency currency, double lattitude, double longitude) {
     //Generated unique payment id
     String generatedPaymentId = "unique_payment_id";
     //Current location of the device
     GeoLocation location = new GeoLocation(lattitude, longitude);
     
     return new PaymentRequest(amount, currency, generatedPaymentId, location);
   }
 ```

#### Handle payment
From mPOS SDK v1.2.0 a new callback `onPaymentProgressStateChanged` is provided. This callback sends different payment progress states during a payment. The payment progress state may also be obtained from the `PaymentTask` instance. E.g. `paymentTask.getPaymentProgressState()`.

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
                state = getString(R.string.payment_started);
                break;
            case REQUEST_PRESENT_CARD:
                state = getString(R.string.present_card);
                break;
            case REQUEST_INSERT_CARD:
                state = getString(R.string.insert_card);
                break;
            case CARD_INSERTED:
                state = getString(R.string.card_inserted);
                break;
            case REQUEST_ENTER_PIN:
                state = getString(R.string.enter_pin);
                break;
            case PIN_ENTERED:
                state = getString(R.string.pin_entered);
                break;
            case CONTACTLESS_BEEP_OK:
                state = getString(R.string.beep_ok);
                break;
            case CONTACTLESS_BEEP_FAILED:
                state = getString(R.string.beep_failed);
                break;
            case REQUEST_SWIPE_CARD:
                state = getString(R.string.swipe_card);
                break;
            case FINISHED:
                state = getString(R.string.payment_finished);
                break;
            case REQUEST_ENTER_PIN:
                state = getString(R.string.enter_pin);
                break;
            case NONE:
                state = getString(R.string.none);
                break;
            default:
                break;
        }
  }
 ```
 

### Refund
You can refund a payment conducted via the mPOS SDK partially or in full.

#### Start refund
Initialize the refund payment request and create a refund task. For a refund you need:

- **paymentId**: String to specify the original sale payment's ID that is supposed to be refunded. In the [Start payment](#start-payment) section, it is called "generatedPaymentId".
- **generatedRefundId**: String to uniquely specify the refund.
- **amount**: Decimal number indicating the amount to be refunded, which cannot be higher than the original payment's amount.
- **currency**: 3 letter ISO character (e.g. EUR) that is identical to the original sale payment's currency.

 ```java
 private RefundRequest startRefund(String paymentId, BigDecimal amount, Currency currency,
                                                    String description) {
     //Generated unique refund id
     String generatedRefundId = "unique_refund_id"
     
     return new RefundRequest(generatedRefundId, paymentId, amount, currency, description);
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
The SDK issues a receipt image of sale and refund payments that contains the bare minimum of receipt details. Please keep in mind to extend the image with the merchants name, address and a respective receipt ID. In case you wish to create your own receipt by using a set of raw payment data, please contact <a href="mailto:developer@payleven.com">developer@payleven.com</a>.

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
      
### Documentation
[API Reference](http://payleven.github.io/mPOS-SDK-Android/1.2.0/javadoc/)

### mPOS SDK Sample App
The mPOS SDK includes a sample app illustrating how the SDK can be integrated. Within this sample app is possible to select a card reader, make payments and refund them. It also contains a Signature View where the user can sign in case the payment requires a signature.
Please note that the location is hardcoded and needs to be changed depending on the country the user is conducting the payment.
