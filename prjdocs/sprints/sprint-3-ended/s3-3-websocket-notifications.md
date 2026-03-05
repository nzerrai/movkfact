# Story S3.3: WebSocket Notifications for Batch Jobs (ENHANCED)

**Sprint:** Sprint 3  
**Points:** 8 (ENHANCED scope, +3 from baseline 5)  
**Epic:** EPIC 3 - Advanced Features & Scalability  
**Type:** Full-Stack Feature (Backend + Frontend)  
**Lead:** Amelia (Backend) + Sally (Frontend)  
**Status:** Review  
**Dependencies:** S3.1 Activity Tracking + S3.2 Spring Batch  

---

## 📋 Objectif

Implémenter WebSocket avec STOMP pour envoyer notifications temps réel aux clients lors de progression et completion des batch jobs. Le système doit supporter persistent job status, graceful reconnection, error notifications, et retry logic. **ENHANCED scope includes:** persistent job status tracking, error retry notifications, graceful reconnection with state recovery, multi-client job subscriptions.

---

## ✅ Acceptance Criteria - Backend

### WebSocket Configuration
- [x] Spring WebSocket configuré dans le backend
- [x] Endpoint WebSocket: `/ws/batch-notifications`
- [x] STOMP protocol supporté
- [x] Client peut se connecter/déconnecter sans perte de données
- [x] Message authentification / sécurité basique (`/ws/**` permis dans SecurityConfig)

### Message Protocol (STOMP)
Messages à envoyer aux clients (format JSON):

1. **Job Started:**
   ```json
   {
     "type": "job_started",
     "jobId": "batch-123",
     "timestamp": "2026-03-02T10:30:00Z",
     "dataSetCount": 5,
     "estimatedDuration": 45
   }
   ```

2. **Job Progress:**
   ```json
   {
     "type": "job_progress",
     "jobId": "batch-123",
     "completed": 3,
     "total": 5,
     "percentage": 60,
     "currentDataSet": "Dataset #3",
     "timestamp": "2026-03-02T10:31:00Z"
   }
   ```

3. **Job Completed:**
   ```json
   {
     "type": "job_completed",
     "jobId": "batch-123",
     "status": "SUCCESS",
     "rowsGenerated": 4500,
     "duration": 45,
     "dataSetIds": [101, 102, 103, 104, 105],
     "timestamp": "2026-03-02T10:31:45Z"
   }
   ```

4. **Job Error:**
   ```json
   {
     "type": "job_error",
     "jobId": "batch-123",
     "errorMessage": "Dataset #2 generation failed after 3 retries",
     "affectedDataSetId": 102,
     "retryAttempts": 3,
     "timestamp": "2026-03-02T10:31:20Z"
   }
   ```

5. **Job Progress Update (Enhanced):**
   ```json
   {
     "type": "job_progress_update",
     "jobId": "batch-123",
     "completed": 4,
     "total": 5,
     "percentage": 80,
     "currentDataSet": "Dataset #4",
     "averageTimePerDataSet": 11.2,
     "estimatedRemainingSeconds": 11,
     "timestamp": "2026-03-02T10:31:30Z"
   }
   ```

### Persistent Job Status (ENHANCED)
- [x] Job status persisted in database (JobStatus table)
- [x] Permet recovery après reconnection
- [x] Statuts: `QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED`
- [x] Détails persistés: jobId, status, progress, errors, createdAt, completedAt
- [x] Query endpoint: `GET /api/batch/{jobId}/full-status` retourne persistent state

### Multi-Client Subscriptions
- [x] Chaque client peut subscribre à `/topic/batch/{jobId}`
- [x] Topic name = unique job ID
- [x] Multiple clients reçoivent messages pour le même job
- [x] Broadcast correctement implémenté (SimpMessagingTemplate.convertAndSend)

### Error Handling & Retry Notifications
- [x] Notifications envoyées pour chaque erreur (JobErrorMessage via BatchJobNotificationService)
- [x] Inclut: error message, affected dataset, retry count
- [x] Client peut afficher retry status (tentative X sur 3)
- [x] Après 3 retries: erreur finale notifiée

### Heartbeat & Keep-Alive
- [x] Heartbeat configuré (ping chaque 30s — `setHeartbeatValue([30000, 30000])`)
- [x] Client peut detecter disconnection (onDisconnect callback)
- [x] Reconnection automatique (avec exponential backoff: 1s, 2s, 5s, 10s, 30s)

### Testing (Backend)
- [x] Tests WebSocket avec SockJS client mock (`BatchJobNotificationServiceTest` — Mockito)
- [x] Tests STOMP protocol compliance (topic prefix, message type fields)
- [x] Tests message delivery (13 unit tests in `BatchJobNotificationServiceTest`)
- [x] Tests persistence & recovery (`BatchJobFullStatusIntegrationTest` — 6 integration tests)
- [x] Coverage >80%

---

## ✅ Acceptance Criteria - Frontend

### WebSocket Client
- [x] WebSocket client établissant connexion à `/ws/batch-notifications`
- [x] Utiliser STOMP client library: `@stomp/stompjs` + `sockjs-client`
- [x] Connection lifecycle management:
  - Connect on demand (first subscribe)
  - Auto-reconnect on disconnect
  - Exponential backoff: 1s, 2s, 5s, 10s, 30s
- [x] Error handling pour connection failures (onStompError callback)

### NotificationPanel Component
- [x] Nouveau composant `NotificationPanel` affichant jobs en progress
- [x] Affiche:
  - Job ID
  - Real-time progress bar (0-100%)
  - Current dataset being processed
  - Estimated time remaining
  - Elapsed time (for completed jobs)
- [x] Layout: positioned en bottom-right corner (fixed, bottom:20, right:20, w:350)
- [x] Collapsible: peut réduire/agrandir

### Real-Time Progress Bar (ENHANCED)
- [x] Linear progress bar: 0% → 100%
- [x] Update smoothly (no jumps — driven by WebSocket events)
- [x] Color: primary (running) → success (completed) / error (failed)
- [x] Percentage text: "40% (2/5 datasets)"
- [x] Time remaining: "~30s restant"
- [x] Average time per dataset displayed (in `JobProgressMessage`)

### Toast Notifications
- [x] Job Started: `Batch démarré avec X dataset(s)` (info)
- [x] Job Progress: (displayed in NotificationPanel only, not toast)
- [x] Job Completed: `✅ Batch terminé ! X lignes générées en Xs` (success)
- [x] Job Error: `❌ Erreur dataset X: message (tentative Y/3)` (error)
- [x] Partial: `⚠️ Batch partiellement terminé (N erreur(s))` (warning)

### Graceful Reconnection (ENHANCED)
- [x] Detect connection loss (onDisconnect callback)
- [x] Show "Reconnexion..." / "Déconnecté" badge in NotificationPanel
- [x] Attempt reconnection with exponential backoff (WebSocketService)
- [x] On reconnection success:
  - Pending subscriptions re-applied automatically
  - Active jobs restored from localStorage
  - Re-subscribed to necessary topics
- [x] After max retries: console.warn logged, `DISCONNECTED` state shown

### State Management (ENHANCED)
- [x] Active jobs stored in React Context (`BatchJobsContext`)
- [x] Jobs state includes: jobId, status, progress, errors, timestamps
- [x] Persist to localStorage: RUNNING jobs only (key: `movkfact_active_jobs`)
- [x] On page reload:
  - Restore RUNNING jobs from localStorage
  - Re-subscribe to necessary topics via `useEffect`

### Component Integration
- [x] `NotificationPanel` intégré dans `Layout.jsx` (persistent display on all pages)
- [x] `BatchJobsProvider` dans `App.jsx` (accessible globally)
- [x] Multiple jobs tracking simultaneously (no hardcoded limit)

### Testing (Frontend)
- [x] Tests for WebSocket client connection/reconnection (`BatchJobsContext.test.js`)
- [x] Tests for NotificationPanel component rendering (`NotificationPanel.test.jsx`)
- [x] Tests for progress bar updates (percentage, counts)
- [x] Tests for toast notifications (via BatchJobsContext mock WS)
- [x] Tests for error handling & recovery (error count display)
- [x] Tests for state management (reducer via Provider, localStorage)
- [x] Coverage >80% — 31 tests passing (100%)

---

## 🏗️ Technical Specifications

### Backend Implementation

**1. WebSocket Configuration**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic/")
            .setHeartbeatValue(new long[]{30000, 30000}); // 30s heartbeat
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/batch-notifications")
            .setAllowedOrigins("*")
            .withSockJS(); // SockJS fallback
    }
}
```

**2. WebSocket Message Handler**
```java
@Component
public class BatchJobNotificationService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private JobStatusRepository jobStatusRepository;
    
    // Send job started notification
    public void notifyJobStarted(String jobId, int dataSetCount) {
        JobStartedMessage msg = new JobStartedMessage(
            jobId, 
            dataSetCount, 
            LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/batch/" + jobId,
            msg
        );
        
        // Persist to database
        jobStatusRepository.saveJobStarted(jobId, dataSetCount);
    }
    
    // Send progress update
    public void notifyProgress(String jobId, int completed, int total) {
        JobProgressMessage msg = new JobProgressMessage(
            jobId,
            completed,
            total,
            (int)(completed * 100.0 / total),
            LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/batch/" + jobId,
            msg
        );
        
        jobStatusRepository.updateJobProgress(jobId, completed, total);
    }
    
    // Send error notification
    public void notifyError(String jobId, String error, int retryCount) {
        JobErrorMessage msg = new JobErrorMessage(
            jobId,
            error,
            retryCount,
            LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/batch/" + jobId,
            msg
        );
        
        jobStatusRepository.recordJobError(jobId, error);
    }
    
    // Send job completed
    public void notifyCompleted(String jobId, List<Long> dataSetIds) {
        JobCompletedMessage msg = new JobCompletedMessage(
            jobId,
            "SUCCESS",
            dataSetIds,
            LocalDateTime.now()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/batch/" + jobId,
            msg
        );
        
        jobStatusRepository.saveJobCompleted(jobId, dataSetIds);
    }
}
```

**3. JobStatus Entity (Persistence)**
```java
@Entity
@Table(name = "job_status")
public class JobStatus {
    @Id
    private String jobId;
    
    @Enumerated(EnumType.STRING)
    private JobStatusType status; // QUEUED, RUNNING, COMPLETED, FAILED
    
    private Integer progress; // 0-100%
    private Integer total;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer errorCount;
    
    @Transient
    private String errorDetails;
    
    // getters/setters
}

public enum JobStatusType {
    QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED
}
```

**4. JobStatus Repository**
```java
public interface JobStatusRepository extends JpaRepository<JobStatus, String> {
    JobStatus findByJobId(String jobId);
    List<JobStatus> findByStatus(JobStatusType status);
}

// Service methods
public void saveJobStarted(String jobId, int total) {
    JobStatus status = new JobStatus();
    status.setJobId(jobId);
    status.setStatus(JobStatusType.RUNNING);
    status.setProgress(0);
    status.setTotal(total);
    status.setCreatedAt(LocalDateTime.now());
    jobStatusRepository.save(status);
}

public JobStatus getFullStatus(String jobId) {
    return jobStatusRepository.findByJobId(jobId);
}
```

### Frontend Implementation

**1. WebSocket Service**
```javascript
// services/WebSocketService.js
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscribers = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
    }
    
    connect() {
        const socket = new SockJS('http://localhost:8080/ws/batch-notifications');
        this.client = Stomp.over(socket);
        
        this.client.connect({}, frame => {
            console.log('Connected:', frame);
            this.reconnectAttempts = 0;
        }, error => {
            console.error('Connection error:', error);
            this.reconnect();
        });
    }
    
    subscribeToBatch(jobId, callback) {
        const topic = `/topic/batch/${jobId}`;
        
        if (!this.subscribers.has(topic)) {
            const subscription = this.client.subscribe(topic, message => {
                const data = JSON.parse(message.body);
                callback(data);
            });
            this.subscribers.set(topic, subscription);
        }
    }
    
    unsubscribeFromBatch(jobId) {
        const topic = `/topic/batch/${jobId}`;
        const subscription = this.subscribers.get(topic);
        if (subscription) {
            subscription.unsubscribe();
            this.subscribers.delete(topic);
        }
    }
    
    reconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
            console.log(`Reconnecting in ${delay}ms...`);
            setTimeout(() => this.connect(), delay);
        }
    }
    
    disconnect() {
        if (this.client) {
            this.client.disconnect(() => {
                console.log('Disconnected');
            });
        }
    }
}

export default new WebSocketService();
```

**2. NotificationPanel Component**
```jsx
// components/NotificationPanel.jsx
import React, { useEffect, useState } from 'react';
import WebSocketService from '../services/WebSocketService';
import { Box, LinearProgress, Typography, Paper, Collapse } from '@mui/material';

export const NotificationPanel = () => {
    const [activeJobs, setActiveJobs] = useState({});
    const [expanded, setExpanded] = useState(false);
    
    useEffect(() => {
        // Subscribe to all active jobs
        Object.keys(activeJobs).forEach(jobId => {
            WebSocketService.subscribeToBatch(jobId, handleJobUpdate);
        });
        
        return () => {
            Object.keys(activeJobs).forEach(jobId => {
                WebSocketService.unsubscribeFromBatch(jobId);
            });
        };
    }, []);
    
    const handleJobUpdate = (message) => {
        const { jobId, type } = message;
        
        setActiveJobs(prev => ({
            ...prev,
            [jobId]: {
                ...prev[jobId],
                ...message,
                lastUpdate: new Date()
            }
        }));
    };
    
    return (
        <Paper
            sx={{
                position: 'fixed',
                bottom: 20,
                right: 20,
                width: 350,
                p: 2,
                zIndex: 1000,
                backgroundColor: '#f5f5f5'
            }}
        >
            <Typography
                variant="h6"
                onClick={() => setExpanded(!expanded)}
                sx={{ cursor: 'pointer', mb: 2 }}
            >
                Jobs ({Object.keys(activeJobs).length})
            </Typography>
            
            <Collapse in={expanded}>
                {Object.values(activeJobs).map(job => (
                    <Box key={job.jobId} sx={{ mb: 3 }}>
                        <Typography variant="body2">{job.jobId}</Typography>
                        <LinearProgress
                            variant="determinate"
                            value={job.percentage || 0}
                            sx={{ mb: 1 }}
                        />
                        <Typography variant="caption">
                            {job.completed}/{job.total} datasets
                            ({job.estimatedRemainingSeconds}s remaining)
                        </Typography>
                    </Box>
                ))}
            </Collapse>
        </Paper>
    );
};
```

---

## 📊 Tâches

| # | Tâche | Assigné | Durée | Dépend de |
|---|-------|---------|-------|-----------|
| 1 | WebSocket config (Spring) | Amelia | 1.5h | S3.2 ready |
| 2 | Message DTOs + service | Amelia | 2h | T1 |
| 3 | JobStatus entity + repository | Amelia | 1.5h | T2 |
| 4 | Persistent job status implementation | Amelia | 1.5h | T3 |
| 5 | Error/retry notifications | Amelia | 1h | T4 |
| 6 | WebSocket client (React) | Sally | 2h | T1 |
| 7 | NotificationPanel component | Sally | 3h | T6 |
| 8 | Graceful reconnection logic | Sally | 2h | T7 |
| 9 | State management (Context/Zustand) | Sally | 2h | T8 |
| 10 | Toast notifications integration | Sally | 1.5h | T9 |
| 11 | Tests: Backend WebSocket integration | Amelia | 3h | T5 |
| 12 | Tests: Frontend components + state | Sally | 3h | T10 |
| 13 | Code review + integration testing | Both | 2h | T12 |

**Durée Totale Estimée:** 28 heures (~3.5 jours, parallèle Amelia/Sally)

---

## 🔗 Dependencies

**From Sprint 3:**
- S3.1 Activity Tracking (pour logging)
- S3.2 Spring Batch (core batch job functionality)

**To Sprint 3:**
- S3.4 E2E Testing (tests must cover WebSocket scenarios)

---

## 📈 Definition of Done

- [x] WebSocket endpoint fonctionnel (/ws/batch-notifications)
- [x] STOMP protocol working correctly
- [x] Job status persisted in database
- [x] All message types sending correctly (job_started, job_progress_update, job_completed, job_error)
- [x] NotificationPanel component rendering (fixed bottom-right, collapsible)
- [x] Progress bars updating in real-time
- [x] Toast notifications working (notistack — info/success/warning/error)
- [x] Graceful reconnection implemented (exponential backoff, localStorage recovery)
- [x] Tests >80% coverage (Backend: 19 new tests | Frontend: 31 tests, 100% passing)
- [ ] Code reviewed et mergé (→ status: review)
- [x] No console errors/warnings (DOM nesting fix applied)

---

## 🚀 Implementation Strategy

**Parallel tracks (Amelia & Sally working together):**

1. **Phase 1 - Backend Setup (Amelia, Day 1, 3h):** WebSocket config, message DTOs
2. **Phase 1 - Frontend Setup (Sally, Day 1, 2h):** WebSocket client, basic connection
3. **Phase 2 - Backend Persistence (Amelia, Day 2, 3h):** JobStatus entity, error notifications
4. **Phase 2 - Frontend UI (Sally, Day 2, 4h):** NotificationPanel component, progress bar
5. **Phase 3 - Integration (Both, Day 3-4, 5h):** Reconnection logic, state mgmt, tests

---

## 📚 Reference

- **Spring WebSocket Docs:** https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket
- **STOMP Protocol:** https://stomp.github.io/
- **SockJS + Stompjs:** https://github.com/sockjs/sockjs-client
- **Sprint Planning:** [SPRINT-3-PLANNING-SUMMARY.md](SPRINT-3-PLANNING-SUMMARY.md)

---

---

## 🤖 Dev Agent Record

### File List

**Backend — New Files**
- `src/main/java/com/movkfact/config/WebSocketConfig.java`
- `src/main/java/com/movkfact/websocket/BatchJobNotificationService.java`
- `src/main/java/com/movkfact/websocket/dto/JobStartedMessage.java`
- `src/main/java/com/movkfact/websocket/dto/JobProgressMessage.java`
- `src/main/java/com/movkfact/websocket/dto/JobCompletedMessage.java`
- `src/main/java/com/movkfact/websocket/dto/JobErrorMessage.java`
- `src/main/java/com/movkfact/entity/JobStatus.java`
- `src/main/java/com/movkfact/entity/JobStatusType.java`
- `src/main/java/com/movkfact/repository/JobStatusRepository.java`
- `src/test/java/com/movkfact/websocket/BatchJobNotificationServiceTest.java`
- `src/test/java/com/movkfact/websocket/BatchJobFullStatusIntegrationTest.java`

**Backend — Modified Files**
- `src/main/java/com/movkfact/batch/BatchJobExecutionListener.java`
- `src/main/java/com/movkfact/batch/BatchJobController.java`
- `src/main/java/com/movkfact/repository/DataSetRepository.java`
- `src/main/java/com/movkfact/config/SecurityConfig.java`

**Frontend — New Files**
- `movkfact-frontend/src/services/WebSocketService.js`
- `movkfact-frontend/src/context/BatchJobsContext.jsx`
- `movkfact-frontend/src/context/BatchJobsContext.test.js`
- `movkfact-frontend/src/components/NotificationPanel/NotificationPanel.jsx`
- `movkfact-frontend/src/components/NotificationPanel/NotificationPanel.test.jsx`

**Frontend — Modified Files**
- `movkfact-frontend/src/App.jsx`
- `movkfact-frontend/src/layout/Layout.jsx`
- `movkfact-frontend/package.json`

### Change Log

| Date | Change |
|------|--------|
| 02/03/2026 | S3.3 implementation: WebSocket config, DTOs, BatchJobNotificationService, JobStatus entity, BatchJobExecutionListener hooks, WebSocketService.js, BatchJobsContext, NotificationPanel |
| 03/03/2026 | Code review fixes: race condition C1, SkipListener C2, rowsGenerated M1, CORS M3, jobId type M4, eslint M5 |

### Senior Developer Review (AI)

**Reviewer:** Winston | **Date:** 03 mars 2026 | **Verdict: ✅ APPROVED**

Issues trouvés et corrigés :
- **[CRITIQUE-FIXED]** Race condition `storeTotalCount` vs `beforeJob` — `beforeJob` dérive maintenant `total` depuis `configKey`
- **[CRITIQUE-FIXED]** SkipListener vides — `onSkipInProcess`/`onSkipInWrite` notifient via WebSocket + persistent error count via `ThreadLocal<Long>`
- **[MEDIUM-FIXED]** `rowsGenerated = written * 100` — calcul réel depuis somme des `count` des configs
- **[MEDIUM-FIXED]** `setAllowedOriginPatterns("*")` — aligné sur les origines de SecurityConfig
- **[MEDIUM-FIXED]** Incohérence type jobId — normalisé en `String` partout dans le frontend
- **[MEDIUM-FIXED]** `eslint-disable` sans explication — commentaire ajouté, `handleMessage` dans les deps

---

**Status:** done
**Priority:** NORMAL
**Scope:** ENHANCED (8 pts, +3 from baseline)
**Created:** 02 mars 2026
**Reviewed & Closed:** 03 mars 2026
