# ProjectOS

## PROJECT CONTEXT

ProjectOS is a universal project management API. A "project" is anything
with a goal, a body of work, and an expected outcome: software, a college
project, research, a business launch, an event, a YouTube channel, learning
a skill, a home renovation. Never assume software-specific workflows.

A project holds goals, tasks, milestones, notes, deadlines, progress and an
activity history. A user should be able to open one project and answer:
what am I trying to achieve, what is done, what remains, what is blocked or
overdue, what is next, am I on track.

Scope for v1 — build only this:
projects, goals, tasks, milestones, notes, activity log, dashboard,
filtering, search, authentication.

Explicitly OUT of scope. Do not build these and do not leave stubs:
file/document uploads, teams and member invitations, notifications,
project templates, custom user-defined fields, and anything AI-related
(assistants, agents, summaries, recommendations, RAG).

### Tech stack

Java 17, Maven, Spring Boot 3.x
spring-boot-starter-web, spring-boot-starter-data-jpa,
spring-boot-starter-validation, spring-boot-starter-security (Part 11 only)
PostgreSQL at runtime, H2 for tests
Flyway for migrations
Lombok, MapStruct
springdoc-openapi-starter-webmvc-ui
JUnit 5, AssertJ, Mockito, spring-boot-starter-test

Do not add any dependency not listed here without telling the user why first.
No Elasticsearch, no Redis, no Kafka, no message queues, no async executors.

### Domain model (built up across the parts)

**Project**: name, description, projectType, customType, objective,
startDate, targetDate, status, priority, tags, owner.
Has many: Goal, Task, Milestone, Note, Activity.

**Task**: title, description, status, priority, deadline, completedAt, tags.
Belongs to: Project (required), Milestone (optional, nullable).

**Milestone**: name, description, deadline, status, orderIndex.
Belongs to: Project. Has many Tasks. completionPercent is computed.

**Goal**: description, target (free text), deadline, status.
Belongs to: Project.

**Note**: title, content, tags. Belongs to: Project.

**Activity**: type, message, entityType, entityId, timestamp.
Belongs to: Project. Written only by an event listener.

**Enums**
- Priority: LOW, MEDIUM, HIGH, CRITICAL
- ProjectStatus: PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
- TaskStatus: TODO, IN_PROGRESS, BLOCKED, COMPLETED
- MilestoneStatus / GoalStatus: NOT_STARTED, IN_PROGRESS, COMPLETED
- ProjectType: SOFTWARE, EDUCATION, RESEARCH, BUSINESS, STARTUP, PERSONAL,
  CREATIVE, CONTENT, EVENT, MARKETING, OTHER

Deleting a project cascades to its goals, tasks, milestones, notes and
activity. Deleting a milestone must NOT delete its tasks; set their
milestone to null instead.

## BUILD PROCESS

The build is split into 12 parts (see the original build prompt for the
full per-part spec). Implement ONE part at a time. After finishing a part:
print a short summary of files created/changed, print how to verify it
works, then STOP and wait for the user to say "continue to Part N". Do not
start the next part on your own.

## ENGINEERING RULES — apply to every part, every time

### Structure
- Package by FEATURE, never by layer.
  `com.projectos.<feature>/` contains the entity, repository, service
  interface, service impl, controller, mapper and a `dto/` subpackage.
  `com.projectos.common/` contains BaseEntity, enums, exceptions, config.
- Never create root-level packages named controllers/, services/,
  models/ or dtos/.

### Layering
- Controllers: routing, binding, validation, delegation. Nothing else.
  No business logic. No try/catch. No repository access.
- Services: all business rules. No HTTP or servlet types — no
  ResponseEntity, no HttpStatus, no web request objects.
- Repositories: Spring Data interfaces only.

### Dependency management
- Constructor injection only. Never `@Autowired` on a field or setter.
- Every service has an interface plus an Impl; controllers depend on the
  interface.
- When one feature needs data from another, define a narrow interface in
  the CONSUMING package and implement it in the providing package. Do
  not inject a whole fat service just to call one method.

### API boundary
- Controllers never accept or return JPA entities. DTOs only.
- All DTOs are Java records in the feature's `dto/` subpackage.
- Separate CreateXRequest, UpdateXRequest and XResponse types. Update
  requests use partial-update semantics: null means "leave unchanged".
- Validate every request DTO with Bean Validation annotations.
- Every list endpoint is paginated and returns `Page<XResponse>`.
- Mapping uses MapStruct, `componentModel = "spring"`. Update mappers use
  `NullValuePropertyMappingStrategy.IGNORE`.

### Persistence
- Every entity extends BaseEntity.
- All `@ManyToOne` associations are `FetchType.LAZY`.
- `jpa.hibernate.ddl-auto` is always "validate". Never update or create.
- Every schema change is a NEW Flyway migration. Never edit an applied one.
- Index the columns you actually filter on.
- Avoid N+1: use `@EntityGraph` or a fetch join when you need children.
  Never rely on lazy loading during serialization.

### Domain rules
- Every child entity (task, goal, milestone, note, activity) belongs to
  exactly one project and is always accessed through its project id in
  the URL path. Before acting on a child, verify it belongs to that
  project; if it does not, throw ResourceNotFoundException. Never reveal
  that the resource exists under a different project.
- Progress percentages are ALWAYS computed from data, never stored in a
  column. Zero tasks means zero percent, never a divide-by-zero.
- Percentage and risk calculations live in small stateless classes with
  no repository dependencies, so they are unit testable as pure logic.

### Errors
- One `@RestControllerAdvice` named GlobalExceptionHandler handles
  everything and returns a consistent ApiError body.
- ResourceNotFoundException -> 404, BusinessRuleException -> 409,
  validation failure -> 400 with a fieldErrors map, anything else -> 500
  logged server-side with nothing leaked to the client.

### Testing
- Each part containing business rules ships unit tests for those rules.
- Test the rules, not the framework. Do not test getters or mappers.

### Style
- Small classes with one reason to change.
- No speculative abstractions. No interface without a real inversion need.
- No TODO comments, no placeholder methods, no dead code.
- Comment only non-obvious decisions, never restate the code.

## Review checklist before each commit

Skim the diff for these five failures most likely to slip through:
- `@Autowired` on a field instead of a constructor parameter
- a JPA entity appearing in a controller method signature
- business logic or `try/catch` inside a controller
- repository methods like `findByProjectIdAndStatusAndPriority...`
- a stored `progress` or `completionPercent` column
