# 🚀 LegacyLifter – AI-Powered Java Code Modernization & Technical Debt Eraser

> **LegacyLifter** is a production-grade, event-driven platform that ingests legacy Java codebases (Java 8/11 era), performs deep static AST analysis, calculates technical debt across 4 dimensions, automatically refactors code to **Java 21/25 standards** (Virtual Threads, Records, Pattern Matching, Stream.toList()), auto-generates regression JUnit 5 test suites, and opens automated GitHub Pull Requests.

---

## 🌟 Key Features & Engine Architecture

### 1. ⚡ Deep Static AST Analysis Engine
- Uses **JavaParser (v3.26)** & **Spoon (v11.1)** for AST-level parsing.
- 9 AST pattern detectors identifying outdated idioms:
  - `RawTypeDetector`: Unparameterized collections (`List list = new ArrayList()`)
  - `AnonymousClassDetector`: Single-method inner classes replaceable by Lambdas
  - `OldConcurrencyDetector`: Platform thread pools & heavy synchronization replaceable by Java 21 **Virtual Threads** (`Executors.newVirtualThreadPerTaskExecutor()`)
  - `BlockingIoDetector`: Legacy file streams (`FileInputStream`, `FileOutputStream`)
  - `MissingRecordDetector`: Boilerplate POJOs converted to **Java Records** (JEP 395)
  - `MissingSealedClassDetector`: Abstract classes converted to **Sealed Types** (JEP 409)
  - `OldSwitchDetector`: Legacy switch statements converted to **Pattern Matching Switch** (JEP 441)
  - `StreamImprovementDetector`: `.collect(Collectors.toList())` simplified to Java 16+ `.toList()`
  - `OptionalMisuseDetector`: Unsafe direct `Optional.get()` calls

### 2. 🤖 RAG-Grounded AI Modernization Engine
- Powered by **Spring AI + LangChain4j + PGVector**.
- RAG Knowledge Base indexed with official JDK migration guides & JEPs.
- Automatic Strategy pattern fallback when offline.
- Generates side-by-side Git unified code diffs with confidence scoring & risk assessment.

### 3. 📊 Technical Debt Scoring Engine
- 4-Dimension scoring model (0 – 100):
  1. **Maintainability Score** (35% weight)
  2. **Modernity Score** (25% weight)
  3. **Security Score** (20% weight)
  4. **Performance Score** (20% weight)
- Assigns letter grades (`A+`, `A`, `B`, `C`, `D`, `F`) & tracks historical trend data points for dashboard line charts.

### 4. 🧪 Smart Test Generation Engine
- Auto-generates **JUnit 5 + AssertJ** unit test suites targeting modernized Java code.
- Evaluates **PIT Mutation Testing** coverage estimates.

### 5. 🐙 Automated GitHub Integration & Event Pipeline
- Automatically creates branches, commits modernized code, and opens rich GitHub Pull Requests.
- Decoupled domain event pipeline (`AnalysisCompleted` → `DebtScoring` → `Modernization` → `TestGeneration`).

### 6. 🎨 React 18 SPA Dashboard
- Built with **React 18 + Vite + TypeScript + Vanilla CSS Dark Glassmorphic Design System + Lucide Icons + Recharts**.

---

## 🛠️ Technology Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 21 (targeting Java 25 features) |
| **Framework** | Spring Boot 3.3.5 |
| **Concurrency** | Java 21 Virtual Threads (`Thread.ofVirtual()`) |
| **Static Analysis** | JavaParser 3.26.2, Spoon 11.1.0 |
| **AI / RAG** | Spring AI 1.0.0-M4, LangChain4j 0.35.0, PGVector |
| **Database** | PostgreSQL 16 + Flyway Migrations |
| **Cache & Bus** | Redis 7, Apache Kafka 3.7 |
| **Frontend** | React 18, Vite, TypeScript, Recharts |
| **Observability** | Micrometer, Prometheus, Actuator |

---

## 🚀 Quick Start (Local Development)

### Prerequisites
- JDK 21+
- Maven 3.9+
- Node.js 18+ & npm
- Docker & Docker Compose

### 1. Clone Repository & Start Services
```bash
git clone https://github.com/Deva242006/Legacy-Lifter.git
cd Legacy-Lifter
docker-compose up -d postgres redis kafka
```

### 2. Run Spring Boot Backend
```bash
cd backend
mvn clean spring-boot:run
```
> OpenAPI Swagger Docs available at: `http://localhost:8080/swagger-ui.html`

### 3. Run React Frontend Dashboard
```bash
cd frontend
npm install
npm run dev
```
> React Dashboard available at: `http://localhost:3000`

---

## 📄 License
Distributed under the MIT License.
Developed with ❤️ for Modernizing Enterprise Java.
