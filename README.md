# Mobile Shopping Assistant - sample

[Mobile Shopping Assistant](https://github.com/GoogleCloudPlatform/MobileShoppingAssistant-sample)
demonstrates how to build a mobile backend that will power Android and iOS
native applications and expose an API using Google Cloud Endpoints.

A [tutorial](https://cloud.google.com/solutions/mobile/how-to-build-mobile-app-with-app-engine-backend-tutorial/)
is available on the Google Cloud Platform documentation.

### Author
Google Cloud Developers Relations.

### Disclaimer
This sample application is not an official Google product.

# Google App Engine backend and Android client

## Project setup, installation, and configuration

This sample application is designed to work with Android Studio. It was
tested with Android Studio 1.1.0, the Google App Engine SDK 1.9.18 and Android
SDK 21.

For styling purposes, it uses the CheckStyle-IDEA plugin. To install it,
go to Preferences > Plugins > Browse Repositories > search for "CheckStyle-IDEA"
and "install plugin".

Open the project in Android Studio (select the root folder MobileAssistantAndroidAppEngine 
for the location of the project, as the android client and the backend are
combined in one single project). The first time you open it, Android Studio
will try to resolve all dependencies using Gradle. When the status bar is
no longer indicating Gradle work, verify that both the Android and the Backend 
configurations are ready.

If the Android application configuration is missing,
choose Run > Edit Configurations > Click on the + sign > Select Android
Application. In the editor, give the configuration a name and associate it
with the module "app". If the Backend configuration is missing, choose Run >
Edit Configurations > Click on the + sign > Select App Engine DevAppServer.
In the editor, give the configuration a name, associate it with the module
"backend" and check "synchronize with build.gradle configuration".

First, run the backend configuration. This will launch a local dev server on
your computer, and also generate all the Google App Engine Endpoints client
libraries that will be used by the Android application. Once the backend is
running, you can run the Android application in the Emulator.

When the application starts, you can notice two things:

1.  No store appears. This is normal, we have not inserted any data in the
datastore yet.
2.  The debug console will throw 401 Unauthorized errors. This is because the
local devserver does not support authentication, and some backend methods do
require it.

Now follow the instructions in [MobileAssistantAndroidAppEngine/backend/MobileAssistant-Data/README.md](MobileAssistantAndroidAppEngine/backend/MobileAssistant-Data/README.md)
to insert dummy data (Place, Offer and Recommendation objects) inside the local datastore. Once 
done, access the page at [http://localhost:8080/admin/buildsearchindex](http://localhost:8080/admin/buildsearchindex)
to build the places index. Now if you hit the "Refresh Store List" button 
inside the application, the stores should display. Click on them to access
the offers and recommendations.

## Deploying

To deploy on App Engine, you need to create a new project in the
[Google Cloud Console](https://console.developers.google.com/),
and then configure a few credentials in the backend and the
Android application.

1.  Create the new project.
2.  Follow [Android documentation](http://developer.android.com/google/gcm/gs.html)
to obtain a *GCM_API_KEY* (Note: In the 'Enabling the GCM Service' step choose
 'Google Cloud Messaging for Android') and follow the
 [Cloud Endpoints documentation](https://developers.google.com/appengine/docs/java/endpoints/auth#creating-client-id)
 to obtain the 3 Client IDs *ANDROID_CLIENT_ID*, *IOS_CLIENT_ID*,
 *WEB_CLIENT_ID*. For the Android Client ID the package name is
 'com.google.sample.mobileassistant' and for the iOS Client ID the bundle is
 'com.google.sample.MobileAssistantIOS'.
3.  Open [MobileAssistantAndroidAppEngine/backend/src/main/webapp/WEB-INF/appengine-web.xml](MobileAssistantAndroidAppEngine/backend/src/main/webapp/WEB-INF/appengine-web.xml)
and enter your *APPLICATION_ID* within the application XML element and the
*GCM_API_KEY* in the property "gcm.api.key" XML element.
4.  Open [MobileAssistantAndroidAppEngine/backend/src/main/java/com/google/sample/mobileassistantbackend/Constants.java](MobileAssistantAndroidAppEngine/backend/src/main/java/com/google/sample/mobileassistantbackend/Constants.java)
and insert the API keys and IDs required.
5.  Open [MobileAssistantAndroidAppEngine/android/config.gradle](MobileAssistantAndroidAppEngine/android/config.gradle) and insert *SENDER_ID*,
*WEB_CLIENT_ID*, the URL to your deployed application,
which is [https://YOUR-APPLICATION-ID.appspot.com/\_ah/api/](https://YOUR-APPLICATION-ID.appspot.com/_ah/api/),
and turn *SIGN_IN_REQUIRED* to "true".
6.  Build the backend, then select Build > Deploy module to App Engine and
follow the instructions. You will need to login from Android Studio into the
Google account used in the first step.
7.  Insert data into the backend by following instructions in
[MobileAssistantAndroidAppEngine/backend/MobileAssistant-Data/README.md](MobileAssistantAndroidAppEngine/backend/MobileAssistant-Data/README.md), using the URL of the deployed backend and the right Google credentials
to login.
8.  Run the Android application.

# iOS client

The Mobile Shopping Assistant iOS Client demonstrates how to build an iOS client
that mirrors the functionality of the Android client by leveraging the same
Google App Engine backend with the help of
[Google APIs Client Library for Objective-C](https://code.google.com/p/google-api-objectivec-client).


## Project setup, installation, and configuration
This sample source code and project is designed to work with Xcode 6.2.  The
application was tested on iOS 8.2.

### Prerequisites

Follow the instructions for
*Google App Engine backend and Android client* and deploy the backend to
Google App Engine.
Note that the Bundle ID for this iOS client is *com.google.sample
.MobileAssistantIOS*.

Make sure you have correctly imported sample data to the
deployed backend following
[MobileAssistantAndroidAppEngine/backend/MobileAssistant-Data/README.md](MobileAssistantAndroidAppEngine/backend/MobileAssistant-Data/README.md).

### Set up Mobile Assistant iOS Client Xcode Project

#### Open MobileAssistantIOS project in Xcode
Open a new Finder window and navigate to the directory you have extracted the
Mobile Assistant iOS Client.  Double click on the `MobileAssistantIOS.xcodeproj`
file. It will  open the project in Xcode automatically.

#### Configure Mobile Assistant Backend to Recognize the iOS Client App
1. The Mobile Assistant Backend was configured to use a particular
*IOS_CLIENT_ID* in step 2 of the Prerequisites.

2. Look up the client secret, that corresponds to the *IOS_CLIENT_ID* in the
backend, from [Google API Console](http://code.google.com/apis/console),
update the following constants in the `ShopsTableViewController.m` file in
the MobileAssistantIOS project:

* kKeyClientID (in line 35)
* kKeyClientSecret (in line 36)

3. In the [MobileAssistantIOS/API/GTLServiceShoppingAssistant.m](MobileAssistantIOS/API/GTLServiceShoppingAssistant.m) file,
replace the string "{{{YOUR APP ID}}}" with the Application ID of where the
Mobile Assistant Backend was deployed.

4. Follow the steps at https://cloud.google.com/appengine/docs/java/endpoints/consume_ios, 
part *Adding required files to your iOS project* first item *If you are not
using the Google+ iOS SDK*.

### Build and Execute the MobileAssistantIOS Project

1. Follow the steps [to set the simulator location to San Francisco, CA](https://developer.apple.com/library/ios/recipes/xcode_help-scheme_editor/Articles/simulating_location_on_run.html#//apple_ref/doc/uid/TP40010402-CH10).

2. On the top left corner of the toolbar in Xcode, select `MobileAssistantIos
> iPhone 8.2 Simulator`.  Click the `Run` button to execute the app.

3. Switch to the `iOS Simulator` application from Xcode.  You can now
interact with the MobileAssistantIOS Client App.

* Since this application is location-sensitive, to work with existing data
in the Mobile Assistant Backend, set the location to `{Latitude: 37.2222,
Longtitude: -122.1111}` via the menu `Debug > Location > Custom Location…`
* If prompted, click "OK" to allow MobileAssitantIOS app to access your 
current location.
* The application may ask for your Google Account information. 
Sign in and consent to allow the application to `view your email address` 
and `know who you are on Google`.
* On the first screen, click any store location.  On the next screen, 
the application will display different recommendations and offers based on 
different store location.

## Take a Closer Look at MobileAssistantIOS Client App
In `ShopsTableViewController.m` file, set breakpoints to the following methods:

* fetchAllShops
* fetchOffersForPlaceID
* fetchRecommendationsForPlaceID

These methods are responsible for making the requests to the Mobile Assistant 
Backend via the Google APIs Client Library for Objective-C.

## Optional Reference
1. Click [here](https://developers.google.com/appengine/docs/java/endpoints/consume_ios#configuring-your-web-app) 
to learn more about generating iOS client library for Google Cloud Endpoint.


## Contributing changes

* See [CONTRIBUTING.md](CONTRIBUTING.md)


## Licensing

* See [LICENSE](LICENSE)
