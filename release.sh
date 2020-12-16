mkdir -p release/src
cat pom-prod.xml | grep -v remove-before-sending > release/pom.xml
cp -r src/main release/src/
cd release
zip -r version-$(date +"%m_%d_%Y-%H:%M").zip src/ pom.xml
