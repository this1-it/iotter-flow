docker compose -f ~/docker/docker-iotter/docker-compose.yml up -d


nvm alias default 22

npm install --save-dev @babel/plugin-proposal-object-rest-spread

mvn -pl iotter-flow-ui -Pproduction package

readlink -f "$(which java)"

update-java-alternatives -l


Use Maven from the repo root.

  Full rebuild (all modules):
  mvn clean install

  UI module only:

  mvn -pl iotter-flow-ui clean compile

  mvn -pl iotter-flow-ui -am -DskipTests clean compile
 
  mvn -pl iotter-flow-ui -am clean package

  Run the UI after build:

  mvn -pl iotter-flow-ui clean vaadin:prepare-frontend

  mvn -pl iotter-flow-ui spring-boot:run


mvn -pl iotter-flow-ui spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

mvn -pl iotter-flow-rest -am -DskipTests compile

mvn -pl iotter-flow-rest spring-boot:run 


 mvn -pl iotter-flow-ui-core install && mvn -pl iotter-flow-ui spring-boot:run 


[mcp_servers.chrome-devtools]
command = "npx"
args = ["-y", "chrome-devtools-mcp@latest", "--headless", "--no-usage-statistics"]

$vaadin-flow-migration migrate package iotter-flow-ui/src/main/java/it/thisone/iotter/ui/networkgroups



mvn -pl iotter-flow-rest spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

mvn -pl iotter-flow-rest -am spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"


 mvn -pl iotter-flow-ui -am clean package

rm -rf iotter-flow-ui/target/dev-bundle && mvn -pl iotter-flow-ui spring-boot:run

https://console.developers.google.com/


find out if Vaadin 8 reflection-based listener registration are still in place and prepare list for replacing with proper Vaadin Flow ComponentEvent + ComponentEventListener + Registration
 pattern.
