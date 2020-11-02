const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	fs.writeFile('WSDL_login.txt', `encodeURI(https://${encodeURI(process.env.API_USERNAME)}:${encodeURI(process.env.API_PASSWORD)}@*)`, (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
