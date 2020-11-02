const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');

try {
	const nameToGreet = 'Ol';
	console.log(`Hello ${nameToGreet}!`);
	fs.writeFile('helloworld.txt', 'Hello World!');
	console.log('Written.');
} catch (error) {
	core.setFailed(error.message);
}
