# CoPilot
Project for the course *Software Engineering 2* at Mälardalen University.

# Configure the project to start running it:
First of all you need a Firebase account to manage the project so follow the next instructions:

Open http://firebase.google.com/ in your browser -> Click on the blue button "start" -> add project -> choose the name of your project -> accept the terms and create the project.

Next you should select Android app and complete the form with this data: 

Android package name:

	com.mdh.ivanmuniz.copilotapp

Other Name (optional):

	CoPilot

We recomend that you use SHA1, but it is not necessary.

We have to download the configuration.

Copy configuration in this folder CoPilotApp/app.

Next you will need to open Android Studio, and select "Open an existing Android Studio project". You select the folder named CoPilotApp inside the CoPilot that you have downloaded, and then run the app.

In Firebase's website, we select Authentication from the menu on the left, and then Configure new log in. After that, we select "email/password" and abilitate the option. The second option does not need to be abilitated. Then save.

### Firebase cloud function installation
For a detailed guide, follow this guide: https://firebase.google.com/docs/functions/get-started

You require the following tools:

* Node.js framework
* npm 
* Firebase CLI

1. First you need to have the Node.js framework installed (v6 or v8 required), which is available here
- https://nodejs.org/en/

	*Optional* 1.1 The previous install should include npm as well, otherwise npm can be found here
	- https://www.npmjs.com/

2. After these tools are installed, open the local command line and run the following command:
```
npm install -g firebase-tools
```
3. After this installation is complete run the following command:
```
firebase login
```
4. This should bring you to a login screen for Firebase, login and select the correct database.
5. Browse to the location of the directory with the Cloud functions *(/Cloud/ directory in the github)*
6. Run the following command to deploy the functions to the database
```
firebase deploy --only functions
```
**If any errors are encountered with this process, consult the firebase guide linked above.**


# Git folder structure 
**This repository consists of the following folders:**
- *CoPilotApp*, containing the source code of the app.
- *Documents*, which consists of the following subfolders:
  - *Deliverables*, where all documentation deliverables will be stored.
  - *Weekly Presentations*, containing the PDF vesrsions of the slides from the weekly meetings with the steering group.
  - *Group Presentations*, containing the PDF versions of the group presentations slides.
  
There is also a wiki for further explanations on every topic, where information will be added as we develop the app.

# Authors and contact.
- Tommy Ernsund
  - LinkedIn: https://www.linkedin.com/in/TommyErnsund
- Viking Forsman
- Joaquín García Benítez
  - LinkedIn: https://www.linkedin.com/in/jotagarciaz/
- Iván Muñiz
  - LinkedIn: https://www.linkedin.com/in/ivanmuniz96
  - Email: <ivanmr_96@live.com.mx>
- Mathias Svensson Karlsson
  - Email: <svenssonkarlsson.mathias@gmail.com>
- Clara Torre García-Barredo
  - LinkedIn: https://www.linkedin.com/in/clara-torre-garcia-barredo/
  - Email: <clara.torreg@alumnos.unican.es>
