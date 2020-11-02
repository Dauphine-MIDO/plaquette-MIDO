const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	const start = encodeURI(process.env.API_USERNAME);
	console.log(`Start: ${start}.`);
	console.log(`Start inline: ${encodeURI(process.env.API_USERNAME)}.`);
	fs.writeFile('WSDL_login.txt', `encodeURI(https://process.env.API_USERNAME:process.env.API_PASSWORD@*)`, (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
