# FixLocal API Automation

This folder hosts the Postman/Newman collection used to exercise every FixLocal endpoint.

## Structure

```
tests/
  postman/
    fixlocal-api.postman_collection.json
    fixlocal-local.postman_environment.json
  README.md
```

## Prerequisites

- Node.js (for `npx newman`)
- Java 17 + MongoDB running (backend uses `http://localhost:8080`)
- Postman (optional) for visual runs

## Running Locally

1. Start the backend: `mvn spring-boot:run`
2. In another terminal:

```bash
npx newman run tests/postman/fixlocal-api.postman_collection.json \
  -e tests/postman/fixlocal-local.postman_environment.json \
  --reporters cli
```

> **Running on another machine**: copy the entire `tests/` directory, install Node.js, run `npm install -g newman` (or use `npx newman ...`), update the environment file with that machine’s credentials/base URL, then execute the command above. No IDE is required.

3. Update environment variables:
   - `userEmail`, `userPassword` – credentials of test user
   - `tradespersonId` – existing tradesperson ID (or seed one)
   - `userToken`, `adminToken` – can be pre-set to skip login requests

## Tips

- After running the “Auth → Login” request, Newman automatically stores `userToken` in collection variables via the test script.
- Set `adminToken` manually via environment (grab from `/api/v1/auth/login` for admin account).
- Adjust the collection to add more assertions (status codes, response body checks) per your needs.

For CI integration, invoke the Newman command in your pipeline and export HTML/JUnit reports using `--reporters cli,html,junit`.
