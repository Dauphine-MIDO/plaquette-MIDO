const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	const nameToGreet = 'Ol';
	const API_username = encodeURI(process.env.API_username);
	console.log(`Writing ${API_username}.`);
	fs.writeFile('WSDL_login.txt', `${API_username}`, (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
