# How to deploy Spring Boot Application on Digital Ocean using Docker Swarm and GitHub Actions

## Create Spring Boot Application
Example configuration:  
- **Lombok**
- **Spring Web**
- **Spring Data JPA**
- **H2 Database**

## Create `requests.http` file - it simplifies interaction with your web application
Example file content:

```http request
### App info
GET localhost:8080/api/info


### All tasks
GET localhost:8080/api/tasks


### Create task
POST localhost:8080/api/tasks
Content-Type: application/json

{
  "description": "Simple task"
}
```

# Add `Jib` to your project - this tool enables containerize your Java application
Your `pom.xml` file should look similar to this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.rafal-nowak</groupId>
    <artifactId>task-tracker</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>task-tracker</name>
    <description>task-tracker</description>
    <properties>
        <java.version>17</java.version>
        <docker.username>rafalnowak444</docker.username>
        <docker.image.name>task-tracker</docker.image.name>
        <docker.image.tag/>
        <myTestSourceDirectory>./src/test</myTestSourceDirectory>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <testSourceDirectory>${myTestSourceDirectory}</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>com.google.cloud.tools</groupId>
                <artifactId>jib-maven-plugin</artifactId>
                <version>3.3.1</version>
                <configuration>
                    <from>
                        <image>eclipse-temurin:17</image>
                        <platforms>
                            <platform>
                                <architecture>arm64</architecture>
                                <os>linux</os>
                            </platform>
                            <platform>
                                <architecture>amd64</architecture>
                                <os>linux</os>
                            </platform>
                        </platforms>
                    </from>
                    <to>
                        <image>docker.io/${docker.username}/${docker.image.name}:${docker.image.tag}</image>
                        <tags>
                            <tag>latest</tag>
                        </tags>
                    </to>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```

# Invoke `mvn -ntp -B verify -Ddocker.image.tag=1 jib:build` command to build Docker Image of your application locally

# Create `docker-compose.yml` file

```yaml
services:
  task-tracker:
    ports:
      - 8080:8080
    environment:
      - APP_DESCRIPTION=Docker run app
    image: rafalnowak444/task-tracker:03.06.2024.19.44.20
```

# Build CI pipeline using GitHub Actions
Your `main-backend-ci.yml` file should look similar to this:
```yaml
name: CI - Build Main Backend

on:
  workflow_dispatch:
  pull_request:
    branches:
      - main
    paths:
      - backend/main/**

jobs:
  build:
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: ./backend/main
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'maven'
      - name: Build and run Unit/Integration Tests with Maven
        run: mvn -ntp -B verify
```

# Build CD pipeline using GitHub Actions
Your `main-backend-cd.yml` file should look similar to this:
```yaml
name: CD - Deploy Main Backend

on:
  workflow_dispatch:
  push:
    branches:
      - main
    paths:
      - backend/main/**

jobs:
  deploy:
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: ./backend/main

    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'maven'
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ vars.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_ACCESS_TOKEN }}
      - name: Set build number
        id: build-number
        run: echo "BUILD_NUMBER=$(date '+%d.%m.%Y.%H.%M.%S')" >> $GITHUB_OUTPUT
      - name: Build Package Push with Maven
        run: mvn -ntp -B verify -Ddocker.image.tag=${{steps.build-number.outputs.BUILD_NUMBER}} jib:build
```
Remember to create necessary variables and secrets related to Actions in your GitHub repository

# Create Droplets on Digital Ocean Platform
Select `Droplets` from the left menu  
Press `Create Droplet` button  
Choose `Amsterdam` Region  
Choose `Ubuntu` OS
Choose `Regular` CPU
Choose `$6/mo` option  
Choose `SSH Key` Authentication Method and add/select your SSH keys
Choose `Quantity` `3 Droplets` and give them names `node1`, `node2`, `node3`  

If you need to add SSH Key to existing Droplet go to:  
`https://docs.digitalocean.com/products/droplets/how-to/add-ssh-keys/`

# Install Docker Swarm on your Droplets
Go to: `https://get.docker.com/`  
And use this instructions:  
1. download the script

   $ `curl -fsSL https://get.docker.com -o install-docker.sh`

2. verify the script's content

   $ `cat install-docker.sh`

3. run the script with --dry-run to verify the steps it executes

   $ `sh install-docker.sh --dry-run`

4. run the script either as root, or using sudo to perform the installation.

   $ `sudo sh install-docker.sh`


# Create `docker-stack.yml` file
It should look similar to this:
```yaml
version: '3.8'

services:
  app:
    image: rafalnowak444/task-tracker:${TAG}
    ports:
      - "8080:8080"
    deploy:
      replicas: 3
      update_config:
        parallelism: 2
        delay: 10s
      restart_policy:
        condition: on-failure
    networks:
      - webnet

networks:
  webnet:
```


# Improve CD pipeline - add interaction with Docker Swarm
Your `main-backend-cd.yml` file should look similar to this:
```yaml
name: CD - Deploy Main Backend

on:
  workflow_dispatch:
  push:
    branches:
      - main
    paths:
      - backend/main/**

jobs:
  deploy:
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: ./backend/main

    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'maven'
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ vars.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_ACCESS_TOKEN }}
      - name: Set build number
        id: build-number
        run: echo "BUILD_NUMBER=$(date '+%d.%m.%Y.%H.%M.%S')" >> $GITHUB_OUTPUT
      - name: Build Package Push with Maven
        run: mvn -ntp -B verify -Ddocker.image.tag=${{steps.build-number.outputs.BUILD_NUMBER}} jib:build
      - name: Prepare deployment
        run: |
          sed -i 's/\${TAG}/'${{ steps.build-number.outputs.BUILD_NUMBER }}'/g' ../devops/docker-stack.yml
      - name: Deploy to Docker Swarm
        env:
          DIGITAL_OCEAN_SSH_PRIVATE_KEY: ${{ secrets.DIGITAL_OCEAN_SSH_PRIVATE_KEY }}
          DIGITAL_OCEAN_SSH_USER: ${{ vars.DIGITAL_OCEAN_SSH_USER }}
          DIGITAL_OCEAN_SSH_HOST: ${{ vars.DIGITAL_OCEAN_SSH_HOST }}
        run: |
          echo "${{ secrets.DIGITAL_OCEAN_SSH_PRIVATE_KEY }}" > key.pem
          chmod 600 key.pem
          scp -o StrictHostKeyChecking=no -i key.pem ../devops/docker-stack.yml ${{ vars.DIGITAL_OCEAN_SSH_USER }}@${{ vars.DIGITAL_OCEAN_SSH_HOST }}:/tmp/docker-stack.yml
          ssh -o StrictHostKeyChecking=no -i key.pem ${{ vars.DIGITAL_OCEAN_SSH_USER }}@${{ vars.DIGITAL_OCEAN_SSH_HOST }} << EOF
            docker stack deploy -c /tmp/docker-stack.yml myapp
          EOF
          rm key.pem
```
Remember to create necessary variables and secrets related to Actions in your GitHub repository

# Now you are able to deploy your application to Docker Swarm cluster hosted on Digital Ocean platform automatically