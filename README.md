# message-bridge

A stateless REST API that sends a message to a recipient. SMS works today through
Twilio. WhatsApp and email are planned; the API already accepts them and answers
`501 Not Implemented` until a sender exists.

Nothing is stored. Every request is handled on its own.

## Requirements

- Java 21
- A Twilio account with an API key

## Run locally

```bash
cp .env.example .env    # then fill in your Twilio values (API_KEY is optional)
./mvnw spring-boot:run
```

The app reads `.env` from the project folder at startup (environment variables with
the same names also work and take priority). It refuses to start if a value is missing.
The server listens on http://localhost:8080 (or on `PORT` if that is set).

## API

### `POST /v1/message`

If `API_KEY` is set, every request needs the header `X-API-Key: <your API_KEY>`;
without it, or with a wrong key, the API answers `401` and sends nothing. If `API_KEY`
is empty, anyone can call the API and the app logs a warning at startup. On a public
URL that means anyone can send SMS on your Twilio account, so set a key before
sharing the URL widely.

```json
{
  "type": "SMS",
  "to": "+14155550100",
  "message": "Hello from message-bridge"
}
```

| Field     | Rules                                                         |
|-----------|---------------------------------------------------------------|
| `type`    | `SMS`, `WHATSAPP` or `EMAIL` (uppercase)                       |
| `to`      | For SMS: E.164 phone number, `+` then country code and number |
| `message` | Required, up to 1600 characters                               |

Success (`200 OK`):

```json
{ "messageId": "SM...", "type": "SMS" }
```

Errors use the standard problem format ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)):

```json
{ "title": "Bad Request", "status": 400, "detail": "'to' must not be blank", "instance": "/v1/message" }
```

| Status | When                                                      |
|--------|-----------------------------------------------------------|
| 400    | Missing or invalid field, unknown `type`, bad phone number |
| 401    | `API_KEY` is set and `X-API-Key` is missing or wrong       |
| 501    | `type` is valid but not supported yet (WhatsApp, email)    |
| 502    | Twilio refused the message or could not be reached         |

Example:

```bash
curl -X POST http://localhost:8080/v1/message \
  -H "X-API-Key: your-api-key" \
  -H "Content-Type: application/json" \
  -d '{"type":"SMS","to":"+14155550100","message":"Hello from message-bridge"}'
```

### Twilio trial accounts

On a trial account Twilio only sends to verified numbers, and `message` must be one of
Twilio's template names (for example `sms_2fa`) instead of free text. See
[Try out Twilio SMS](https://www.twilio.com/docs/usage/trials/try-out-sms).

## Design

```
controller/MessageController      validates the request and returns JSON
model/                            MessageType, MessageRequest, MessageResponse
service/MessageService            sends the message through the channel for its type (SMS via Twilio)
exception/GlobalExceptionHandler  turns errors into problem JSON
```

### Adding a channel

In `MessageService`, add a private send method for the channel and point its `case`
in `send()` to it. Nothing else changes. For example, WhatsApp through Twilio uses
the same Messages API with `whatsapp:` prefixed numbers, so it can reuse the existing
Twilio client.

## Tests

```bash
./mvnw test
```

Tests use dummy settings from `src/test/resources` and never call Twilio, so they
need no `.env`.

## Run with Docker

```bash
docker build -t message-bridge .
docker run --rm -p 8080:8080 --env-file .env message-bridge
```

The image holds only the compiled app. `.dockerignore` keeps `.env` out of it, so
secrets are passed in at run time with `--env-file` or `-e`.

## Deploy to Render

1. Push this repository to GitHub.
2. In the [Render dashboard](https://dashboard.render.com), choose **New > Web Service**
   and connect the repository. Render detects the `Dockerfile`.
3. Pick the **Free** instance type.
4. Under **Environment Variables**, add `TWILIO_ACCOUNT_SID`, `TWILIO_API_KEY`,
   `TWILIO_API_SECRET` and `TWILIO_FROM_NUMBER`. Optionally add `API_KEY` to require
   the `X-API-Key` header; use a new, long one for the deployed app (`openssl rand -hex 32`).
5. Create the service. Render builds the image, sets `PORT`, and gives you a URL like
   `https://message-bridge.onrender.com`. Every push to the main branch redeploys.

Browsers may call the API from any website by default. To allow only your UI, set
`CORS_ALLOWED_ORIGINS` to its URL, for example `https://message-bridge-ui.vercel.app`.

On the free plan the service sleeps after 15 minutes without traffic, and the next
request waits about a minute while it starts again. Open the URL once before a demo.
