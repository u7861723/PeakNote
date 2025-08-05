# PeakNote API Documentation

## Overview
This document provides comprehensive API documentation for the PeakNote application, covering attendee management and transcript services.

## Base URL
```
http://localhost:8080
```

## Authentication
Currently, the API does not require authentication for the documented endpoints.

---

## Attendee Management API

### Get Meeting Attendees

Retrieves all attendees for a specific meeting event.

**Endpoint:** `GET /attendees`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `eventId` | String | Yes | The unique identifier of the meeting event |

**Response Format:**
```json
{
  "eventId": "string",
  "attendees": [
    {
      "email": "user@example.com",
      "displayName": "John Doe"
    }
  ]
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `eventId` | String | The meeting event identifier |
| `attendees` | Array | List of meeting attendees |
| `attendees[].email` | String | Attendee's email address |
| `attendees[].displayName` | String | Attendee's display name |

**Example Request:**
```bash
curl -X GET "http://localhost:8080/attendees?eventId=meeting-123"
```

**Example Response:**
```json
{
  "eventId": "meeting-123",
  "attendees": [
    {
      "email": "john.doe@company.com",
      "displayName": "John Doe"
    },
    {
      "email": "jane.smith@company.com",
      "displayName": "Jane Smith"
    }
  ]
}
```

**HTTP Status Codes:**
- `200 OK` - Successfully retrieved attendees
- `400 Bad Request` - Missing or invalid eventId parameter
- `500 Internal Server Error` - Server error

---

## Transcript Management API

### Get Transcript by URL

Retrieves meeting transcript information based on the meeting URL.

**Endpoint:** `GET /transcript/by-url`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `url` | String | Yes | The meeting URL to search for transcripts |

**Response Format:**
```json
{
  "eventId": "string",
  "transcript": "string"
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `eventId` | String | The meeting event identifier (if found) |
| `transcript` | String | The meeting transcript content |

**Example Request:**
```bash
curl -X GET "http://localhost:8080/transcript/by-url?url=https://teams.microsoft.com/l/meetup-join/..."
```

**Example Response:**
```json
{
  "eventId": "meeting-456",
  "transcript": "Meeting started at 10:00 AM. John: Hello everyone... Jane: Good morning..."
}
```

**Empty Response (No transcript found):**
```json
{
  "transcript": ""
}
```

**HTTP Status Codes:**
- `200 OK` - Successfully retrieved transcript or no transcript found
- `400 Bad Request` - Missing or invalid URL parameter
- `500 Internal Server Error` - Server error

### Update Transcript

Updates the transcript content for a specific meeting event.

**Endpoint:** `POST /transcript/update`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `eventId` | String | Yes | The unique identifier of the meeting event |
| `content` | String | Yes | The new transcript content |

**Response Format:**
```
success
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/transcript/update" \
  -d "eventId=meeting-456" \
  -d "content=Updated meeting transcript content..."
```

**Example Response:**
```
success
```

**HTTP Status Codes:**
- `200 OK` - Successfully updated transcript
- `400 Bad Request` - Missing or invalid parameters
- `500 Internal Server Error` - Server error

---

## CORS Support

The Transcript API supports Cross-Origin Resource Sharing (CORS) with the following configuration:
- **Allowed Origins:** `*` (All origins)
- **Methods:** GET, POST
- **Headers:** All headers

---

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "error": "Bad Request",
  "message": "Required parameter 'eventId' is missing"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Data Models

### Meeting Attendee
```json
{
  "email": "string",
  "displayName": "string"
}
```

### Transcript Response
```json
{
  "eventId": "string",
  "transcript": "string"
}
```

---

## Notes

1. **Caching:** The transcript service implements Redis caching for improved performance:
   - URL to event ID mapping is cached
   - Transcript content is cached by event ID
   - Cache is automatically invalidated when transcripts are updated

2. **URL Encoding:** Meeting URLs should be properly URL-encoded when passed as query parameters.

3. **Rate Limiting:** Currently, no rate limiting is implemented on these endpoints.

4. **Logging:** All operations are logged for debugging and monitoring purposes.

---

## Version Information

- **API Version:** 1.0
- **Last Updated:** Current
- **Framework:** Spring Boot 