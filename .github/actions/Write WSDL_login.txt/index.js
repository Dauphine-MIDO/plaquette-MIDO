const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	const start = encodeURI(process.env.API_username);
	console.log(`Start: ${start}.`);
	console.log(`Start inline: ${encodeURI(process.env.API_username)}.`);
	fs.writeFile('WSDL_login.txt', `encodeURI(https://process.env.API_username:process.env.API_password@*)`, (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
