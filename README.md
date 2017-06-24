# SafeTravels
SafeTravels was developed with road tripping in mind, but it could be used in any case where the user would like to send location updates to somebody with a smart phone. Other use cases could include monitoring your childs location, making sure a loved one arrives home safely, or just for fun!

The application allows the user to enter a mobile contact from their contacts list and a time interval to send out SMS messages with location updates. Updates will continue until the user clicks 'STOP' or until the application is killed.

### Key Functionality
*  Continues sending location updates while app is in the background so there is no need to keep it open after you start. Keep your eyes on the road!!!
*  A custom LocationService class was implemented to update the location only when necessary in order to maximize battery life.
*  Sends location updates as SMS messages with a google maps URL so anybody with a smart phone can receive the updates.
*  Simple, lightweight, and very easy to use

### Demo
![splashscreen](https://user-images.githubusercontent.com/14061153/27511861-f5a46288-58e3-11e7-8bb4-5baecd05bdb6.png)
![mainscreen](https://user-images.githubusercontent.com/14061153/27511863-facd662e-58e3-11e7-9461-f8b404b476ea.png)
![runningtrackerscreen](https://user-images.githubusercontent.com/14061153/27511864-fe218e68-58e3-11e7-98b9-10c0237037c7.png)
![stoppedscreen](https://user-images.githubusercontent.com/14061153/27511867-01f2dc72-58e4-11e7-8316-6fbd55e9bceb.png)
