// Firebase functionality
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

const userTable = 'user';

// Listener for user creation
exports.onUserCreated = functions.auth.user().onCreate((user) => {
  // Add additional user data to database
  const uid = user.uid;
  return addUserData( uid );
});

// Listener for user deletion
exports.onUserRemoved = functions.auth.user().onDelete((user) => {
  // Remove additional data from database
  const uid = user.uid;
	return removeUserData( uid );
});

// Sends a goodbye email to the given user.
function addUserData( uid ) {
	
	var db = admin.firestore();
	var docRef = db.collection( userTable ).doc( uid );
	
  var dataSet = docRef.set({
		admin: false,
		type: 0, 
		favorite: []
  });

	return Promise.all([dataSet]);
}

function removeUserData( uid ) {
	var db = admin.firestore();
	var deleteDoc = db.collection( userTable ).doc( uid ).delete();
	
	// Make sure to null all user id references in the path collection
	
	return Promise.all([deleteDoc]);
}