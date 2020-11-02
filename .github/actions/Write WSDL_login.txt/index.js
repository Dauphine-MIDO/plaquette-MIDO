const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	const nameToGreet = 'Ol';
	console.log('Hello ${nameToGreet}!');
	fs.writeFile('helloworld.txt', 'Hello World!', (err) => {
		  if (err) throw err;
	});
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
