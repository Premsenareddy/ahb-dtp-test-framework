# AHB DTP Test Automation Framework

![Java](https://img.shields.io/badge/Java-17-blue)
![Micronaut](https://img.shields.io/badge/Micronaut-Framework-1f8dd6)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A)
![Docker](https://img.shields.io/badge/Docker-Ready-0db7ed)
![Performance](https://img.shields.io/badge/Performance-Gatling-orange)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-success)

A modular, scalable **API Test Automation Framework** built using **Java 17**, **Micronaut**, **Gradle**, and integrated tooling such as **MockServer**, **Kubernetes Port Forwarding**, and **Gatling** for performance testing.

This framework demonstrates enterprise-grade QA automation practices:

- Clean layered architecture  
- Environment-based execution  
- BDD/Gherkin-style modelling  
- API journey validations  
- CI-ready project structure  
- Reusable utilities & data builders  
- K8s port-forward workflow for backend services  

---

## 📁 **Project Structure**

ahb-dtp-test-framework
│
├── src/ # Test code (APIs, journeys, validations)
├── assets/ # Images, test screenshots (optional)
├── mockserver/ # Service virtualization configs
├── kube/ # Kubernetes manifests & port-forward setup
├── scripts/ # Execution helpers
├── setup/ # Dev/QE environment setup
├── secret/ # Placeholder (contains no secrets)
│
├── Dockerfile # Containerized runtime
├── build.gradle # Gradle build config
├── gradle.properties
├── skaffold.yaml # CI/CD orchestration
└── README.md

yaml
Copy code

---

## 🏗 **High-Level Architecture**

+---------------------------+
| Test Runner |
| (Gradle / JUnit5) |
+-------------+-------------+
|
+-------------+-------------+
| Test Layers & Helpers |
| (BDD, Steps, Validators) |
+-------------+-------------+
|
+-------------+-------------+
| API Interaction Layer |
| (Micronaut HTTP Clients) |
+-------------+-------------+
|
+---------------------------+
| External Services / APIs |
+---------------------------+
|
+---------------------------+
| MockServer / K8s Port FW |
| (Service Virtualization) |
+---------------------------+

yaml
Copy code

---

## ▶️ **Running Tests**

### **1️⃣ Setup Port Forwarding**

Expose backend services locally:

```bash
./startPortForward.sh obp-dev
# or
./startPortForward.sh obp-cit
2️⃣ Run API Test Suites
Dev environment:

bash
Copy code
MICRONAUT_ENVIRONMENTS=dev ./gradlew clean test
CIT environment:

bash
Copy code
MICRONAUT_ENVIRONMENTS=cit ./gradlew clean test
🧪 BDD / Gherkin Test Modelling
Scenarios follow a BDD-friendly format:

pgsql
Copy code
TEST   – Scenario summary
GIVEN  – Preconditions
WHEN   – Action executed
THEN   – Assertions
DONE   – End of test
🧿 Mock Server
For downstream API simulation:

bash
Copy code
cd mockserver
./runMockServer.sh
Used for:

Unstable dependencies

Negative testing

Edge cases

Integration isolation

📈 Performance Testing (Gatling)
Ensure E2E tests pass first:

bash
Copy code
MICRONAUT_ENVIRONMENTS=dev ./gradlew clean test --tests "uk.co.company.journey.*"
Then run Gatling:

bash
Copy code
./gradlew clean gatlingRun-perf.simulation.PaymentLoadTest
Produces detailed HTML performance reports.

🧪 Test Strategy Overview
✔ API Testing
Status codes

JSON body validation

Business rules

Negative cases

Journey validations

✔ Integration Testing
K8s service-to-service flows

MockServer for dependencies

Environment toggles

✔ Performance Testing
Load & stress tests

Latency & throughput

Regression performance

✔ Security / Contract Testing (Optional)
JWT token handling

Contract mocks

Response schema validation

🛠 Tech Stack Summary
Category	Tools
Language	Java 17
Framework	Micronaut
Build	Gradle
CI/CD	GitHub Actions, Skaffold
Containers	Docker
Virtualization	MockServer
Performance	Gatling
Orchestration	Kubernetes

🤝 Contributions & Usage
This framework highlights:

Enterprise-class QA engineering

Real-world API automation structure

Performance engineering techniques

Clean architecture principles

Feel free to use or adapt for learning, interviews, or portfolio enhancement.

📬 Contact
Premsena Reddy Anumandla
Senior QA Automation Engineer

GitHub: @Premsenareddy
LinkedIn: https://www.linkedin.com/in/premsena-anumandla-a802b4179/
