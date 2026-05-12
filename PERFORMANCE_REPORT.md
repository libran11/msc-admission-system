# Performance Evaluation Report — MSc Admission System

## 1. Test Environment

| Component | Specification |
|-----------|---------------|
| **CPU** | <!-- e.g. Intel Core i7-12700H @ 2.7GHz --> |
| **RAM** | <!-- e.g. 16 GB DDR5 --> |
| **Disk** | <!-- e.g. NVMe SSD 512GB --> |
| **OS** | <!-- e.g. Windows 11 --> |
| **Java** | OpenJDK 21.0.x |
| **Spring Boot** | 4.0.6 |
| **Database** | MySQL 8.x |
| **Test Tool** | Apache JMeter 5.6.3 |
| **Test Date** | <!-- YYYY-MM-DD --> |

> **Prerequisite**: Start the application with `mvn spring-boot:run` (MySQL must be running).
> The DataSeeder will automatically populate the database on first launch.

---

## 2. Test Scenarios

| Scenario | Concurrent Users | Ramp-up | Duration | Description |
|----------|-----------------|---------|----------|-------------|
| **TG1: Concurrent Create** | 50 | 5s | Until complete | 50 users simultaneously submit applications |
| **TG2: Full Lifecycle** | 30 | 3s | Until complete | Each user: Create → Submit → Start Review → Approve |
| **TG3: Mixed Read Workload** | 20 | 2s | 10 iterations | Random mix of GET by ID, by applicant, by status, review history |

### Run Commands

```bash
# Run all scenarios
jmeter -n -t performance/msc-admission-performance.jmx -l results.csv -e -o report/

# Run individual scenarios using properties
jmeter -n -t performance/msc-admission-performance.jmx -JThreadGroup1.threads=50 -l results_tg1.csv
jmeter -n -t performance/msc-admission-performance.jmx -JThreadGroup2.threads=30 -l results_tg2.csv
jmeter -n -t performance/msc-admission-performance.jmx -JThreadGroup3.threads=20 -l results_tg3.csv
```

---

## 3. Results

### TG1: Concurrent Create (50 users)

| Metric | Value |
|--------|-------|
| Total Requests | |
| Average Response Time | ms |
| Median (p50) | ms |
| p90 | ms |
| p95 | ms |
| p99 | ms |
| Throughput | req/s |
| Error Rate | % |

### TG2: Full Lifecycle (30 users)

| Metric | Value |
|--------|-------|
| Total Requests | |
| Average Response Time | ms |
| Median (p50) | ms |
| p90 | ms |
| p95 | ms |
| p99 | ms |
| Throughput | req/s |
| Error Rate | % |

### TG3: Mixed Read Workload (20 users)

| Metric | Value |
|--------|-------|
| Total Requests | |
| Average Response Time | ms |
| Median (p50) | ms |
| p90 | ms |
| p95 | ms |
| p99 | ms |
| Throughput | req/s |
| Error Rate | % |

---

## 4. Observations

<!-- 
Document key findings here:
- Which endpoint is the slowest?
- Are there any bottlenecks in status transitions?
- How does the system behave under concurrent load?
-->

### 4.1 Response Time Analysis

### 4.2 Throughput Analysis

### 4.3 Error Analysis

---

## 5. Recommendations

<!-- 
Based on the observations above, suggest improvements:
1. Database indexing (e.g., add indexes on status, applicantId)
2. Query optimization
3. Caching strategies
4. Connection pool tuning
5. Pagination for list endpoints
-->
