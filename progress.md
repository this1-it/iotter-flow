docker compose -f ~/docker/docker-iotter/docker-compose.yml up -d


nvm alias default 14

npm install --save-dev @babel/plugin-proposal-object-rest-spread

mvn -pl iotter-flow-ui -Pproduction package



Use Maven from the repo root.

  Full rebuild (all modules):
  mvn clean install

  UI module only:

  mvn -pl iotter-flow-ui clean compile
  mvn -pl iotter-flow-ui clean package

  Run the UI after build:

  mvn -pl iotter-flow-ui clean vaadin:prepare-frontend

  mvn -pl iotter-flow-ui spring-boot:run
