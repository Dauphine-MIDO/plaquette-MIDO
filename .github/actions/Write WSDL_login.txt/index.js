const core = require('@actions/core');
const github = require('@actions/github');

try {
  // `who-to-greet` input defined in action metadata file
  const nameToGreet = 'Ol';
  console.log(`Hello ${nameToGreet}!`);
} catch (error) {
  core.setFailed(error.message);
}

