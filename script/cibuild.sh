#${ACCESS_TOKEN} is a personnal access token from GitHub with the repo autorisations 
set -e
REPO="receive_plaquette"
FILE_LOG="out.log"
FILE_PDF="out.pdf"
DEPLOY_REPO="https://${ACCESS_TOKEN}@github.com/barnabegeffroy/${REPO}.git" 
BUILT_EXIT_CODE=1

function main {
	clean
	get_current_doc
	build_doc
	if [ -z "$TRAVIS_PULL_REQUEST" ]; then
	   	echo "except don't publish doc for pull requests"
    else 
		deploy
	fi  
}

function clean { 
	echo "cleaning docs"
	if [ -f "${FILE_LOG}" ]; then rm -f "${FILE_LOG}"; fi 
	if [ -f "${FILE_PDF}" ]; then rm -f "${FILE_PDF}"; fi 
}

function get_current_doc { 
	echo "getting latest doc"
	git clone --depth 1 $DEPLOY_REPO "../${REPO}"
}

function build_doc { 
	echo "trying to build doc"
	if	mvn exec:java -Dexec.mainClass=io.github.oliviercailloux.plaquette_mido_soap.M1ApprBuilder; then
		echo "Build succeeded"
		BUILT_EXIT_CODE=0
	else
   		echo "Build failed"
		BUILT_EXIT_CODE=1
	fi
}

function deploy {
	echo "deploying changes"
	mv -f "${FILE_LOG}" "../${REPO}"
	if test -f "${FILE_PDF}"; then
		mv -f "${FILE_PDF}" "../${REPO}"
	fi
	cd "../${REPO}"
	git config user.name "Travis CI"
    git config user.email barnabe.geffroy@psl.eu
	git add "${FILE_LOG}"
	if test -f "${FILE_PDF}"; then
		git add "${FILE_PDF}"		
	fi
	git status
	git commit -m "Lastest doc built on travis build $TRAVIS_BUILD_NUMBER auto-pushed to github (exit code ${BUILT_EXIT_CODE})"
	git push $DEPLOY_REPO master
	exit ${BUILT_EXIT_CODE}
}

main

