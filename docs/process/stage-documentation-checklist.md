# Reusable Stage Documentation Checklist

Use this checklist at the end of every project stage to ensure documentation integrity and progress tracking.

---

## 15-Point Stage Documentation Checklist

- [ ] **1. Stage Status:** Set the completed stage status to `DONE` (or `IN_PROGRESS` / `REVIEW` if pending verification) in `docs/PROPERTY_RENTAL_MANAGER_IMPLEMENTATION_PLAN.md`.
- [ ] **2. Task Checklist:** Check off all completed task items (`[x]`) in the applicable stage section of the main implementation plan.
- [ ] **3. Stage Execution Record:** Complete the Stage Execution Record template with start/completion dates, branch, objectives, implemented items, and test results.
- [ ] **4. Progress Log:** Append a new entry to the Progress Log table in `docs/PROPERTY_RENTAL_MANAGER_IMPLEMENTATION_PLAN.md`.
- [ ] **5. Current Next Action:** Update section 17 (`Current next action`) to explicitly state the next stage to be implemented.
- [ ] **6. Technical Docs:** Update or create domain-specific guides in `docs/development/` and `docs/architecture/`.
- [ ] **7. README Updates:** Update `core/README.md` if startup scripts, environment variables, or port mappings changed.
- [ ] **8. API Documentation:** Verify Swagger UI / OpenAPI documentation and update REST API guides if endpoints or payloads changed.
- [ ] **9. ERD & Schema Docs:** Update Mermaid ERD and database documentation if Flyway migrations altered the database model.
- [ ] **10. Decision Log:** Append any new architectural or technical decisions to the Decision Log table in `docs/PROPERTY_RENTAL_MANAGER_IMPLEMENTATION_PLAN.md`.
- [ ] **11. CHANGELOG:** Document added features, fixes, or breaking changes in `core/CHANGELOG.md` under `[Unreleased]`.
- [ ] **12. Test Verification:** Document exact test commands executed (e.g. `./mvnw test`) and record actual test outputs.
- [ ] **13. Issues & Technical Debt:** Record any deferred items, minor limitations, or known issues.
- [ ] **14. AC Verification Rule:** Never mark a stage as `DONE` without verifying every acceptance criterion.
- [ ] **15. Execution Integrity:** Never claim commands or tests were executed unless they were actually run.
