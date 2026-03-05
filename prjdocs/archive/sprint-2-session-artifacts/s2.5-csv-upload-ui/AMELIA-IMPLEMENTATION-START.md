---
title: "🚀 AMELIA IMPLEMENTATION START - S2.5 API Verification + S2.4"
titleFR: "🚀 AMELIA IMPLÉMENTATION DÉMARRAGE - Vérification API S2.5 + S2.4"
date: "2026-02-28"
time: "13:00 CET"
recipient: "Amelia"
phase: "IMPLEMENTATION"
status: "STARTING NOW"
---

# 🚀 AMELIA IMPLEMENTATION START - Begin Now

**To:** Amelia  
**From:** Bob (Scrum Master)  
**Date:** 28 février 2026 @ 13:00 CET  
**Status:** 🟢 **IMPLEMENTATION STARTS NOW**

---

## 🎯 THIS IS YOUR IMPLEMENTATION CHECKLIST

Follow this step-by-step to start your work NOW (not waiting for 17 mars).

---

## ✅ STEP 1: Prepare Environment (15 mins)

```bash
# Go to project root
cd /home/seplos/mockfact

# Verify Maven project builds
mvn clean install -DskipTests

# Verify backend runs
mvn spring-boot:run &

# Wait 5 seconds for server to start
sleep 5

# Verify it responds
curl -s http://localhost:8080/api/domains | jq .
# Should return list of domains ✅
```

---

## ✅ STEP 2: Find & Verify S2.2 Type Detection Endpoint

**Location to check:**
- Backend code: `src/main/java/com/movfact/controller/`
- Look for: `TypeDetectionController.java` or similar

**Verify endpoint exists:**

```bash
# Check if endpoint responds
curl -X POST http://localhost:8080/api/domains/1/detect-types \
  -F "file=@/tmp/test.csv" \
  -v

# Should see:
# HTTP/1.1 200 OK
# or
# HTTP/1.1 400 Bad Request (if file invalid)
```

**Expected Response Structure:**
```json
{
  "data": {
    "columns": [
      {
        "name": "customer_id",
        "type": "PERSONAL_ID",
        "confidence": 0.95,
        "detector": "PersonalDataDetector"
      }
    ],
    "processedRows": 100,
    "successRate": 0.98
  },
  "message": "Type detection completed"
}
```

---

## ✅ STEP 3: Create Benchmark Test Files

Create test CSV files for performance benchmarking:

```bash
# Create temp directory for test files
mkdir -p /tmp/s2.5-benchmarks

# Create 100-row test CSV
python3 << 'EOF'
import csv

# Test 1: 100 rows
with open('/tmp/s2.5-benchmarks/test-100.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['customer_id', 'amount', 'date', 'email'])
    for i in range(100):
        writer.writerow([f'C{10000+i}', f'{1000+i*10}.50', f'2024010{i%10}', f'user{i}@example.com'])

print("✅ Created test-100.csv (100 rows)")
EOF

# Create 1000-row test CSV
python3 << 'EOF'
import csv

with open('/tmp/s2.5-benchmarks/test-1000.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['customer_id', 'amount', 'date', 'email', 'phone', 'address'])
    for i in range(1000):
        writer.writerow([
            f'C{10000+i}',
            f'{1000+i*10}.50',
            f'202401{(i//10)%31:02d}',
            f'user{i}@example.com',
            f'+1-555-{i%10000:04d}',
            f'{i} Main St, City, State'
        ])

print("✅ Created test-1000.csv (1000 rows)")
EOF

# Create 10K-row test CSV
python3 << 'EOF'
import csv

with open('/tmp/s2.5-benchmarks/test-10k.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['customer_id', 'amount', 'date', 'email', 'phone', 'address'])
    for i in range(10000):
        writer.writerow([
            f'C{10000+i}',
            f'{1000+i*10}.50',
            f'202401{(i//100)%31:02d}',
            f'user{i}@example.com',
            f'+1-555-{i%10000:04d}',
            f'{i} Main St, City, State'
        ])

print("✅ Created test-10k.csv (10000 rows)")
EOF

# Verify files created
ls -lh /tmp/s2.5-benchmarks/
```

---

## ✅ STEP 4: Run Performance Benchmarks

**Benchmark 1: Small file (100 rows)**

```bash
echo "⏱️  Testing 100-row file..."
time curl -X POST http://localhost:8080/api/domains/1/detect-types \
  -F "file=@/tmp/s2.5-benchmarks/test-100.csv" \
  -s | jq '.data | {processedRows, successRate}'

# Expected: < 500ms ✅
```

**Benchmark 2: Medium file (1000 rows)**

```bash
echo "⏱️  Testing 1000-row file..."
time curl -X POST http://localhost:8080/api/domains/1/detect-types \
  -F "file=@/tmp/s2.5-benchmarks/test-1000.csv" \
  -s | jq '.data | {processedRows, successRate}'

# Expected: < 2 seconds ✅
```

**Benchmark 3: Large file (10K rows)**

```bash
echo "⏱️  Testing 10K-row file..."
time curl -X POST http://localhost:8080/api/domains/1/detect-types \
  -F "file=@/tmp/s2.5-benchmarks/test-10k.csv" \
  -s | jq '.data | {processedRows, successRate}'

# Expected: < 5 seconds ✅
```

---

## ✅ STEP 5: Verify CORS Configuration

**Check CORS headers in response:**

```bash
curl -X OPTIONS http://localhost:8080/api/domains/1/detect-types \
  -v 2>&1 | grep -i "access-control"

# Should see:
# Access-Control-Allow-Origin: *
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
```

**If CORS headers missing, fix in Backend:**

Find `@Configuration` class in `src/main/java/com/movfact/config/`

Add CORS configuration:
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .maxAge(3600);
            }
        };
    }
}
```

---

## ✅ STEP 6: Create Performance Report

Save your results:

```bash
cat > /tmp/s2.5_api_performance.txt << 'EOF'
===== S2.5 API PERFORMANCE REPORT =====
Date: 28 février 2026
Backend: localhost:8080
Endpoint: POST /api/domains/{domainId}/detect-types

TEST RESULTS:
Test 1 (100 rows):    XXXms   [✅ PASS / ❌ FAIL]
Test 2 (1000 rows):   XXXms   [✅ PASS / ❌ FAIL]
Test 3 (10K rows):    XXXms   [✅ PASS / ❌ FAIL]

CORS Headers:         [✅ PRESENT / ❌ MISSING]
Response Format:      [✅ CORRECT / ❌ INCORRECT]
Error Handling:       [✅ WORKING / ❌ NOT WORKING]

OVERALL STATUS:       ✅ READY FOR INTEGRATION
Issues Found:         NONE
Recommendations:      API ready for frontend integration

Verified By: Amelia
EOF

cat /tmp/s2.5_api_performance.txt
```

---

## ✅ STEP 7: Start S2.4 JSON Export Implementation

After verifying S2.5 API is working, start S2.4:

**Create new controller for export (S2.4):**

```bash
# Navigate to controller directory
cd src/main/java/com/movfact/controller/

# Create file: DataExportController.java
```

**Content for DataExportController.java:**

```java
package com.movfact.controller;

import com.movfact.dto.ApiResponse;
import com.movfact.service.DataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/domains")
@CrossOrigin(origins = "*")
public class DataExportController {

    @Autowired
    private DataExportService exportService;

    /**
     * Export domain as JSON
     * S2.4: JSON Export Engine
     */
    @PostMapping("/{domainId}/export/json")
    public ResponseEntity<ApiResponse<String>> exportDomainAsJson(
            @PathVariable Integer domainId) {
        
        try {
            String json = exportService.exportDomainAsJson(domainId);
            return ResponseEntity.ok(
                new ApiResponse<>(json, "Domain exported as JSON successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(null, "Export failed: " + e.getMessage())
            );
        }
    }

    /**
     * Export domain as JSON (with metadata)
     */
    @PostMapping("/{domainId}/export/json/with-metadata")
    public ResponseEntity<ApiResponse<String>> exportDomainJsonWithMetadata(
            @PathVariable Integer domainId) {
        
        try {
            String json = exportService.exportDomainJsonWithMetadata(domainId);
            return ResponseEntity.ok(
                new ApiResponse<>(json, "Domain exported with metadata")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(null, "Export failed: " + e.getMessage())
            );
        }
    }
}
```

**Create service for export (S2.4):**

```bash
cd src/main/java/com/movfact/service/
# Create file: DataExportService.java
```

**Content for DataExportService.java:**

```java
package com.movfact.service;

import com.movfact.entity.Domain;
import com.movfact.repository.DomainRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataExportService {

    @Autowired
    private DomainRepository domainRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Export domain as JSON string
     */
    public String exportDomainAsJson(Integer domainId) throws Exception {
        Domain domain = domainRepository.findById(domainId)
            .orElseThrow(() -> new Exception("Domain not found"));
        
        return objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(domain);
    }

    /**
     * Export with metadata
     */
    public String exportDomainJsonWithMetadata(Integer domainId) throws Exception {
        Domain domain = domainRepository.findById(domainId)
            .orElseThrow(() -> new Exception("Domain not found"));
        
        // Create wrapper with metadata
        var wrapper = new java.util.HashMap<>();
        wrapper.put("metadata", new java.util.HashMap<>() {{
            put("exportDate", new java.util.Date().toString());
            put("domainId", domainId);
            put("exportFormat", "JSON");
        }});
        wrapper.put("data", domain);
        
        return objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(wrapper);
    }
}
```

**Create test file for S2.4:**

```bash
cd src/test/java/com/movfact/controller/
# Create file: DataExportControllerTest.java
```

**Content for DataExportControllerTest.java:**

```java
package com.movfact.controller;

import com.movfact.entity.Domain;
import com.movfact.repository.DomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class DataExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DomainRepository domainRepository;

    @Test
    public void testExportDomainAsJson() throws Exception {
        // Create test domain if needed
        Domain testDomain = new Domain();
        testDomain.setName("Test Domain");
        testDomain.setDescription("Test Description");
        Domain saved = domainRepository.save(testDomain);

        // Test export endpoint
        mockMvc.perform(post("/api/domains/" + saved.getId() + "/export/json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(jsonPath("$.message").value("Domain exported as JSON successfully"));
    }

    @Test
    public void testExportNonexistentDomain() throws Exception {
        mockMvc.perform(post("/api/domains/99999/export/json"))
            .andExpect(status().isBadRequest());
    }
}
```

---

## ✅ STEP 8: Build & Test S2.4

```bash
# From project root
cd /home/seplos/mockfact

# Run tests for S2.4
mvn clean test -Dtest=DataExportControllerTest

# Expected output:
# Tests run: 2, Failures: 0, Errors: 0
```

---

## 🎯 VERIFICATION CHECKLIST

- [ ] S2.5 API endpoint responds (200 OK)
- [ ] CORS headers present
- [ ] Benchmark 1 (100 rows): < 500ms ✅
- [ ] Benchmark 2 (1000 rows): < 2 sec ✅
- [ ] Benchmark 3 (10K rows): < 5 sec ✅
- [ ] Response format matches spec
- [ ] Error handling works (400, 500 codes)
- [ ] Performance report created
- [ ] S2.4 controller created
- [ ] S2.4 service created
- [ ] S2.4 tests passing (2/2)

---

## 🔗 NEXT: MESSAGE SALLY

Once S2.5 API verified, message Sally:

```
✅ S2.5 API Verified!

Status:
- Endpoint responding: YES ✅
- Performance: GOOD ✅ (< 5 sec)
- CORS: CONFIGURED ✅
- Ready for integration: YES ✅

Sally you can START building CSVUploadPanel now!
```

---

## 💡 TROUBLESHOOTING

**If endpoint not found:**
- Check TypeDetectionController.java exists
- Verify Maven build successful (`mvn clean install`)
- Restart backend: `pkill -9 java && mvn spring-boot:run`

**If CORS error:**
- Check CorsConfig.java exists
- Add `@CrossOrigin` annotation to controller
- Restart backend

**If performance too slow:**
- Check S2.2 implementation (type detection algorithm)
- Run with profiler: `mvn -Dspring-boot.run.jvmArguments="-Xmx2g"`

---

**Status:** ✅ **READY TO IMPLEMENT NOW**  
**Timeline:** Complete S2.5 verification today + start S2.4  
**Next:** Sally begins CSVUploadPanel implementation

