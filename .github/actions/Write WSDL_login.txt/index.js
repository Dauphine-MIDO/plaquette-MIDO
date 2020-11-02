const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	console.log(`Writing ${API_username}.`);
	fs.writeFile('WSDL_login.txt', `encodeURI(https://${API_username}:${API_password}@*)`, (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
