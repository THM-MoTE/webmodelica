#!/bin/bash
# generates a new release, call it like this: bash scripts/release.sh <version>

versionFile="./project/version.txt"
backendImage="thmmote/webmodelica"
frontendImage="thmmote/webmodelica-ui"
version=$1

echo "==> Start releasing: $version"

echo "==> Writing into $versionFile"
echo $version > $versionFile

echo "==> Compiling backend.."
sbt ";clean;compile;universal:packageXzTarball"

echo "==> Building frontend.."
cd ui && npm run build

echo "==> Tagging.."
git commit $versionFile -m ':up: version '$version
git tag -a v$version -m 'Version '$version

echo "==> Generating backend image.."
docker build -t $backendImage:$version .

echo "==> Generating frontend image.."
docker build -t $frontendImage:$version ui

echo "==> pushing to remotes.."
git push origin master
git push origin --tags
docker push $backendImage:$version
docker push $frontendImage:$version
