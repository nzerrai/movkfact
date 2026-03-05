# SPRINT 3 - ACTIVATION DOCUMENT

**Date:** 01 mars 2026 @ 20:45 CET  
**Activated By:** Bob (Scrum Master)  
**Status:** ✅ SPRINT 3 NOW ACTIVE

---

## Transition Summary

**Incoming:** Sprint 2 officially CLOSED ✅  
**Outgoing:** Sprint 3 now ACTIVATED ✅

### Timeline
- **Sprint 2 End Date:** 01 mars 2026 @ 20:45 CET
- **Sprint 3 Start Date:** 01 mars 2026 @ 20:45 CET (immediate)
- **Sprint 3 End Date:** 13 avril 2026 @ 23:59 CET
- **Sprint 3 Duration:** 2 weeks (28 calendar days)

---

## Sprint 2 → Sprint 3 Handoff

### What's Handed Over
1. ✅ Production-ready MVP with data generation pipeline
2. ✅ 458/458 tests passing (100% test coverage on acceptance criteria)
3. ✅ Comprehensive API documentation (Swagger)
4. ✅ Clean codebase ready for S3 development
5. ✅ Established patterns: MVC backend, component-based frontend
6. ✅ Database schema finalized and indexed
7. ✅ CI/CD pipeline prepared

### What's Blocked/Waiting
- **Nothing!** All S2 stories complete, no blockers for S3

### What Needs Attention in S3
1. Activity Tracking implementation (users need dataset state history)
2. Batch generation for multiple datasets in parallel
3. Real-time WebSocket notifications (user experience enhancement)
4. E2E test automation (quality assurance)
5. Docker containerization (deployment readiness)

---

## Sprint 3 Configuration

### Team Assignment

| Role | Person | Focus |
|------|--------|-------|
| **Backend Lead** | Amelia (Dev) | S3.1 Activity Tracking, S3.2 Batch Jobs, S3.5 Docker |
| **Frontend Lead** | Sally (UX) | S3.3 WebSocket UI, S3.4 E2E Testing |
| **QA Lead** | Quinn (QA Engineer) | S3.3 WebSocket validation, S3.4 E2E test suite |
| **Architecture Review** | Winston (Architect) | S3.2 Batch architecture, S3.3 WebSocket design |
| **Product Manager** | John (PM) | Requirements clarification, stakeholder updates |
| **Scrum Master** | Bob (SM) | Sprint coordination, daily standups, impediment removal |

### Stories in Sprint 3

| ID | Title | Points | Assignee | Status |
|----|-------|--------|----------|--------|
| S3.1 | Activity Tracking Service | 5 | Amelia | Ready |
| S3.2 | Spring Batch for Batch Generation | 6 | Amelia | Ready |
| S3.3 | WebSocket Notifications | 5 | Sally + Amelia | Ready |
| S3.4 | E2E Testing & Test Automation | 6 | Quinn | Ready |
| S3.5 | Docker Deployment & Documentation | 6 | Amelia | Ready |
| **Total** | | **28** | | **Ready** |

### Velocity Target
- **Target Velocity:** 14 points/week
- **Sprint Duration:** 2 weeks
- **Expected Completion:** 28 points ✅

---

## Environment Setup for Sprint 3

### Local Development

```bash
# Backend (Java 17, Spring Boot)
cd /home/seplos/mockfact
mvn clean install
mvn spring-boot:run

# Frontend (Node 18, React 18)
cd movkfact-frontend
npm install
npm start

# Database: H2 in-memory (auto-configured)
# Backend API: http://localhost:8080
# Frontend: http://localhost:3000
```

### Git Workflow
- Branch naming: `feature/S3-X-description`
- Commits: Descriptive, referencing story ID
- Pull requests: Code review required before merge
- CI/CD: GitHub Actions validates on each PR

### Communication
- **Daily Standups:** 09:00 CET (via Copilot Chat)
- **Weekly Reviews:** Every Friday @ 17:00 CET
- **Channel:** GitHub Issues for sprint discussions
- **Documentation:** Keep README and API docs updated

---

## Key Technical Notes for S3

### S3.1 Activity Tracking
- Requires audit table in database
- Soft deletes with `deleted_at` timestamp
- Versioning: Original (v0) + Current (vN)
- Consider event-driven architecture (listeners)

### S3.2 Spring Batch
- Configure `JobConfiguration` with parallelization
- ItemReader/Writer pattern recommended
- Target performance: 10 datasets × 1000 rows < 10 seconds
- Error handling with retry logic

### S3.3 WebSocket
- Use Spring Websocket + STOMP for abstraction
- Topic pattern: `/topic/batch/{jobId}`
- Real-time metrics: job progress, completion, errors
- Frontend React hooks for state management

### S3.4 E2E Testing
- Cypress or Playwright recommended
- Test database fixtures for consistent runs
- Performance assertions: <5s for 1000 rows
- CI integration: automated test runs on PR

### S3.5 Docker
- Separate Dockerfiles for backend/frontend
- docker-compose.yml orchestrates all services
- Environment variables for configuration
- Volume mounts for H2 database persistence

---

## Pre-Sprint 3 Checklist

### Completed ✅
- [x] Sprint 2 all stories done
- [x] All tests passing
- [x] Code reviewed and merged
- [x] Documentation updated
- [x] Database schema finalized
- [x] API contracts defined
- [x] Team briefed

### To Do Before First Day
- [ ] Amelia: Review Spring Batch documentation
- [ ] Sally: Prepare WebSocket UI mockups
- [ ] Quinn: Setup Cypress/Playwright environment
- [ ] Bob: Prepare sprint board with S3 stories
- [ ] Winston: Review S3 architectural decisions

---

## Success Criteria for Sprint 3

**Definition of Done (Sprint 3):**
- [ ] All 5 stories marked "Done"
- [ ] Test coverage >80% (backend + frontend)
- [ ] E2E tests automated and passing
- [ ] Performance benchmarks met
- [ ] Docker deployment tested locally
- [ ] All APIs documented in Swagger
- [ ] User acceptance testing completed
- [ ] Zero critical bugs in production code
- [ ] Team signs off on quality

---

## Risk Register for Sprint 3

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| WebSocket connection issues | Medium | High | Sprint test with large client load |
| Batch job performance | Low | High | Profile early, optimize ItemReader/Writer |
| Docker networking | Low | Medium | Local docker-compose testing before prod |
| E2E test flakiness | Medium | Medium | Use page object pattern, explicit waits |
| Time crunch on Docker | Low | Medium | Start Docker work in Week 1 |

---

## Resource Links

### Documentation
- [Sprint 3 Kickoff Summary](sprint-3-started/kickoff-summary.md)
- [Sprint 3 User Stories](sprint-3-started/stories.md)
- [Sprint 2 Closure Report](SPRINT-2-CLOSURE-REPORT.md)

### Code Repositories
- Backend: `/home/seplos/mockfact/src/main/java`
- Frontend: `/home/seplos/mockfact/movkfact-frontend/src`
- Tests: `/home/seplos/mockfact/src/test`

### Tools & Services
- GitHub: <https://github.com/movkfact>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- H2 Console: <http://localhost:8080/h2-console>

---

## Final Notes

Sprint 3 is positioned for success with clear objectives, skilled team, and a solid foundation from Sprint 2. The focus is on advanced features (Activity Tracking, Batch Processing, WebSockets) and production readiness (Docker, E2E Testing). With disciplined execution and effective communication, Sprint 3 should deliver all 28 points and position the product for Q2 scaling.

**Team:** You have everything you need. Let's build something great! 🚀

---

*Activated by:* Bob (Scrum Master)  
*Date:* 01 mars 2026, 20:45 CET  
*Next Update:* Sprint 3 Day 1 standup (31/03/2026)
