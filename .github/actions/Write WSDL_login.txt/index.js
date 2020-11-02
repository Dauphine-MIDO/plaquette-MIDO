const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	const nameToGreet = 'Ol';
	const API_username = process.env.API_username;
	console.log(`Hello ${nameToGreet}!`);
	fs.writeFile('WSDL_login.txt', 'API_username', (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
