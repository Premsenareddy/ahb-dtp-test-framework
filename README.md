AHB DTP Test Automation Framework










A modular, scalable API Test Automation Framework built using Java 17, Micronaut, Gradle, and integrated tooling such as MockServer, Kubernetes Port Forwarding, and Gatling for performance tests.

This project demonstrates enterprise-grade automation engineering practices:

Clean layered architecture

Environment-based execution

BDD/Gherkin style modelling

API journey validations

CI-ready project structure

Reusable helpers & test data builders

K8s port-forward workflow for backend services

📁 Project Structure
ahb-dtp-test-framework
│
├── src/                      # Test code (API, journeys, validations)
├── assets/                   # Images, test screenshots (optional)
├── mockserver/               # Service virtualization
├── kube/                     # K8s manifests & configs
├── scripts/                  # Execution helpers
├── setup/                    # Dev/QE environment setup
├── secret/                   # (Placeholder – contains no secrets)
│
├── Dockerfile                # Containerized runtime
├── build.gradle              # Gradle build
├── gradle.properties
└── skaffold.yaml             # CI/CD orchestration

🏗 High-Level Architecture
          +---------------------------+
          |        Test Runner        |
          |     (Gradle / JUnit5)     |
          +-------------+-------------+
                        |
        +---------------+---------------+
        |     Test Layers & Helpers     |
        |   (BDD, Steps, Validators)    |
        +---------------+---------------+
                        |
       +----------------+----------------+
       |     API Interaction Layer       |
       |   (Micronaut HTTP Clients)      |
       +----------------+----------------+
                        |
  +----------------------+----------------------+
  |    External Microservices / Bank APIs       |
  +----------------------+----------------------+
                        |
       +---------------------------------------+
       |     MockServer / Kubernetes Proxy     |
       |   (Service Virtualization / Port FW)  |
       +---------------------------------------+

▶️ Running Tests
1️⃣ Port-forward to the required environment
./startPortForward.sh obp-dev
# or
./startPortForward.sh obp-cit

2️⃣ Run test suites

Dev:

MICRONAUT_ENVIRONMENTS=dev ./gradlew clean test


CIT:

MICRONAUT_ENVIRONMENTS=cit ./gradlew clean test

🧪 BDD / Gherkin Style

Tests follow a clear BDD structure:

TEST    – Summary of scenario
GIVEN   – Preconditions (Mocks, Users, Accounts)
WHEN    – Action executed
THEN    – Assertions / expected behaviour
DONE    – End of test

🧿 Mock Server (Service Virtualization)

Used to isolate dependencies:

cd mockserver
./runMockServer.sh


Useful for:

Negative testing

Edge-case simulation

Dependency unavailability

Contract-based testing

📈 Performance Testing (Gatling)

Run e2e first:

MICRONAUT_ENVIRONMENTS=dev ./gradlew clean test --tests uk.co.company.journey.*


Then execute Gatling simulation:

./gradlew clean gatlingRun-perf.simulation.PaymentLoadTest


Generates HTML performance reports including:

Response time percentiles

Throughput

Error rate

Latency distribution

🧪 Test Strategy Overview
✔ API Testing

Status codes

Payload validation

Business logic checks

Negative & boundary cases

Journey-level flows

✔ Integration Testing

K8s service-to-service interactions

Using MockServer for unstable services

Environment toggles

✔ Performance Testing

Load & stress scenarios

Latency KPIs

Baseline comparisons

✔ Security & Contract Testing

JWT handling

Contract mocks

API schema validation

🛠 Tech Stack
Category	Tools
Language	Java 17
Framework	Micronaut
Build	Gradle
CI/CD	GitHub Actions, Skaffold
Containers	Docker
Virtualization	MockServer
Performance	Gatling
Orchestration	Kubernetes (port-forwarding)
🤝 Contributions & Usage

This repository demonstrates:

Enterprise-grade banking QA engineering

Modern API automation design

Performance engineering practices

Clean, scalable project structure

Great for learning, interviews, or portfolio demonstration.

📬 Contact

Premsena Reddy Anumandla
Senior QA Automation Engineer
GitHub: @Premsenareddy
LinkedIn: https://www.linkedin.com/in/premsena-anumandla-a802b4179/
