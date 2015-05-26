for file in Shared/src/codeday/squareassault/protobuf/*.proto
do
	echo Building $file
	protoc -I=Shared/src/ --java_out=Shared/src $file
done
echo Done.
