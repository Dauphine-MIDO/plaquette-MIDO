#${ACCESS_TOKEN} is a personnal access token from GitHub with the repo autorisations 
set -e
REPO="receive_plaquette"
FILE_LOG=out.log
FILE_PDF=out.pdf
DEPLOY_REPO="https://${ACCESS_TOKEN}@github.com/barnabegeffroy/${REPO}.git" 
function main {
	get_current_doc
	build_doc
	if [ -z "$TRAVIS_PULL_REQUEST" ]; then
	   	echo "except don't publish doc for pull requests"
    else 
		deploy
	fi  
}

function get_current_doc { 
	echo "getting latest doc"
	git clone --depth 1 $DEPLOY_REPO ../${REPO}
}

function build_doc { 
	echo "trying to build doc"
	if	mvn exec:java -Dexec.mainClass=io.github.oliviercailloux.plaquette_mido_soap.M1ApprBuilder; then
		echo "Build succeeded"
	else
   		echo "Build failed"
	fi
}

function deploy {
	echo "deploying changes"
	FILES="${FILE_LOG}"
	if test -f "${FILE_PDF}"; then
		FILES="${FILES} ${FILE_PDF}"		
	fi
	mv -f ${FILES} ../${REPO}
	cd ../${REPO}
	git config --global user.name "Travis CI"
    git config --global user.email barnabe.geffroy@psl.eu
	git add ${FILES}
	git status
	git commit -m "Lastest doc built on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to github"
	git push $DEPLOY_REPO master
}

main