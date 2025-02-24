
./dart_corp_code.sh

kotlinc -include-runtime main.kt -d main.jar
java -jar main.jar
