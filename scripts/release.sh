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
cd ui &&\
  npm run generate-doc &&\
  npm run build &&\
  cd ..

echo "==> Tagging.."
git commit $versionFile -m ':up: version '$version
git tag -a v$version -m 'Version '$version

echo "==> Generating backend image.."
docker build -t $backendImage:$version .

echo "==> Generating frontend image.."
cd ui && docker build -t $frontendImage:$version . && cd ..

echo "==> pushing to dockerhub.."
docker push $backendImage:$version
docker push $frontendImage:$version

echo "==> updating compose file.."
sed -iE "s/thmmote\/webmodelica:\w+\.\w+\.[\w-]+/thmmote\/webmodelica:$version/" deployment/docker-compose.prod.yml
sed -iE "s/thmmote\/webmodelica-ui:\w+\.\w+\.[\w-]+/thmmote\/webmodelica-ui:$version/" deployment/docker-compose.prod.yml

echo "==> pushing to github.."
git commit deployment/*.yml -m "use new container versions"
git push origin master
git push origin --tags
