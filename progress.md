docker compose -f ~/docker/docker-iotter/docker-compose.yml up -d


nvm alias default 14

npm install --save-dev @babel/plugin-proposal-object-rest-spread

mvn -pl iotter-flow-ui -Pproduction package



Use Maven from the repo root.

  Full rebuild (all modules):
  mvn clean install

  UI module only:

  mvn -pl iotter-flow-ui clean compile

  mvn -pl iotter-flow-ui -am -DskipTests compile
  mvn -pl iotter-flow-ui -am clean package

  Run the UI after build:

  mvn -pl iotter-flow-ui clean vaadin:prepare-frontend

  mvn -pl iotter-flow-ui spring-boot:run


mvn -pl iotter-flow-ui spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
