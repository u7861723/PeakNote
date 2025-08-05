# PeakNote - Microsoft Teams Meeting Intelligence Platform

## Overview

PeakNote is a comprehensive meeting intelligence platform that integrates with Microsoft Teams to automatically capture, transcribe, and analyze meeting content. The platform provides intelligent meeting summaries, attendee tracking, and seamless integration with Microsoft Graph API for real-time meeting event processing.

## Features

### Core Capabilities
- **Real-time Meeting Event Processing**: Automatic capture of Microsoft Teams meeting events via webhooks
- **Intelligent Transcript Processing**: AI-powered meeting transcript analysis and summarization
- **Attendee Management**: Comprehensive tracking of meeting participants
- **Meeting URL Sharing**: Secure access control for meeting sharing
- **Subscription Management**: Automated Microsoft Graph API subscription lifecycle management

### AI-Powered Features
- **Meeting Summaries**: Generate structured meeting minutes using OpenAI
- **Daily Stand-up Analysis**: Specialized processing for sprint daily stand-ups
- **Content Categorization**: Automatic identification of action items and discussion points

### Integration Features
- **Microsoft Graph API**: Full integration with Teams calendar and meeting data
- **Webhook Processing**: Real-time event handling for meeting lifecycle
- **Message Queue System**: Asynchronous processing using RabbitMQ
- **Caching Layer**: Redis-based caching for improved performance

## Architecture

### System Components
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Microsoft     │    │   PeakNote      │    │   External      │
│   Teams/Graph   │◄──►│   Backend       │◄──►│   Services      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Database      │
                       │   (MySQL)       │
                       └─────────────────┘
```

### Technology Stack
- **Framework**: Spring Boot 3.4.4
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis with Redisson
- **Message Queue**: RabbitMQ
- **AI Integration**: OpenAI via Spring AI
- **Authentication**: Azure AD with Microsoft Graph
- **Build Tool**: Maven

## Quick Start

### Prerequisites
- Java 17 or higher
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Microsoft Azure App Registration
- OpenAI API Key

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd PeakNote/demo
   ```

2. **Configure environment variables**
   Create `application.yml` with the following configuration:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/peaknote
       username: your_username
       password: your_password
     redis:
       host: localhost
       port: 6379
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest

   azure:
     client-id: your_azure_client_id
     client-secret: your_azure_client_secret
     tenant-id: your_azure_tenant_id

   webhook:
     client-id: your_webhook_client_id
     client-secret: your_webhook_client_secret
     tenant-id: your_webhook_tenant_id

   notification-url: https://your-domain.com/webhook/notification
   teams-transcript-url: https://your-domain.com/webhook/teams-transcript

   spring:
     ai:
       openai:
         api-key: your_openai_api_key
         base-url: https://api.openai.com
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

### Core Endpoints

#### Meeting Attendees
- `GET /attendees?eventId={eventId}` - Retrieve meeting attendees

#### Transcript Management
- `GET /transcript/by-url?url={meetingUrl}` - Get transcript by meeting URL
- `POST /transcript/update` - Update transcript content

#### Webhook Endpoints
- `GET /webhook/notification` - Microsoft Graph webhook validation
- `POST /webhook/notification` - Handle meeting event notifications
- `POST /webhook/teams-transcript` - Handle transcript notifications

### Response Formats

#### Attendees Response
```json
{
  "eventId": "meeting-123",
  "attendees": [
    {
      "email": "john.doe@company.com",
      "displayName": "John Doe"
    }
  ]
}
```

#### Transcript Response
```json
{
  "eventId": "meeting-456",
  "transcript": "Meeting started at 10:00 AM. John: Hello everyone..."
}
```

## Database Schema

### Core Tables
- **users**: Microsoft Teams user information
- **meeting_event**: Meeting event details from Microsoft Graph
- **meeting_attendee**: Meeting participant information
- **meeting_transcript**: Meeting transcript content and metadata
- **meeting_url_access**: Meeting URL sharing permissions
- **graph_subscription**: Microsoft Graph API subscriptions

### Key Relationships
- Meeting events can have multiple attendees and transcripts
- Users can share meeting URLs with other users
- Graph subscriptions track webhook lifecycle

## Configuration

### Azure AD Configuration
1. Register an application in Azure AD
2. Grant Microsoft Graph API permissions:
   - `Calendars.Read`
   - `OnlineMeetings.Read`
   - `User.Read.All`
3. Configure webhook endpoints for real-time notifications

### RabbitMQ Configuration
The application uses two main queues:
- `peaknote.event.queue`: For meeting event processing
- `peaknote.transcript.queue`: For transcript processing

### Redis Configuration
- Caching for transcript content and URL mappings
- Deduplication for webhook messages
- Session management and temporary data storage

## Development

### Project Structure
```
src/main/java/com/peaknote/demo/
├── config/          # Configuration classes
├── controller/      # REST API controllers
├── entity/          # JPA entities
├── model/           # Data transfer objects
├── repository/      # Data access layer
└── service/         # Business logic services
```

### Key Services
- **GraphService**: Microsoft Graph API integration
- **TranscriptService**: Transcript processing and management
- **MeetingSummaryService**: AI-powered meeting summarization
- **SubscriptionService**: Graph API subscription management
- **MessageConsumer**: Asynchronous message processing

### Adding New Features
1. Create entity classes in `entity/` package
2. Add repository interfaces in `repository/` package
3. Implement business logic in `service/` package
4. Create REST endpoints in `controller/` package
5. Update configuration as needed

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Considerations
- Use external database (Azure SQL, AWS RDS)
- Configure Redis cluster for high availability
- Set up RabbitMQ cluster for message reliability
- Implement proper logging and monitoring
- Configure SSL/TLS for webhook endpoints
- Set up health checks and metrics

## Monitoring and Logging

### Health Checks
- Database connectivity
- Redis connection status
- RabbitMQ connection status
- Microsoft Graph API connectivity

### Key Metrics
- Webhook processing latency
- Transcript processing success rate
- AI summarization performance
- Database query performance

### Logging
The application uses SLF4J with structured logging:
- Webhook processing events
- Transcript processing status
- Error tracking and debugging
- Performance metrics

## Security

### Data Protection
- All sensitive data encrypted at rest
- Secure communication with Microsoft Graph API
- Access control for meeting URL sharing
- Audit trail for all operations

### Authentication
- Azure AD integration for user authentication
- Client secret-based service authentication
- Webhook validation for Microsoft Graph

## Troubleshooting

### Common Issues

#### Webhook Not Receiving Events
1. Verify Azure AD app registration permissions
2. Check webhook URL accessibility
3. Validate subscription status in Graph API
4. Review application logs for errors

#### Transcript Processing Failures
1. Check OpenAI API key configuration
2. Verify meeting event status in database
3. Review RabbitMQ queue status
4. Check Redis connectivity

#### Database Connection Issues
1. Verify MySQL server status
2. Check connection pool configuration
3. Review database credentials
4. Monitor connection limits

### Debug Mode
Enable debug logging by adding to `application.yml`:
```yaml
logging:
  level:
    com.peaknote.demo: DEBUG
    org.springframework.web: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add comprehensive comments for complex logic
- Include unit tests for all new features

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the GitHub repository
- Contact the development team
- Review the documentation and troubleshooting guide

## Roadmap

### Upcoming Features
- [ ] Advanced meeting analytics
- [ ] Integration with additional calendar systems
- [ ] Enhanced AI summarization capabilities
- [ ] Mobile application support
- [ ] Advanced reporting and dashboards

### Version History
- **v1.0.0**: Initial release with core meeting intelligence features
- **v1.1.0**: Enhanced AI summarization and performance improvements
- [ ] Advanced webhook processing and error handling

---

**PeakNote** - Transforming meetings into actionable intelligence

## Configuration

### Using the Template

The project includes an `application.yml.template` file that you can use as a starting point:

1. **Copy the template:**
   ```bash
   cp src/main/resources/application.yml.template src/main/resources/application.yml
   ```

2. **Set environment variables** for your deployment environment:
   ```bash
   export DB_USERNAME=your_db_username
   export DB_PASSWORD=your_db_password
   export REDIS_HOST=your_redis_host
   export AZURE_TENANT_ID=your_azure_tenant_id
   export AZURE_CLIENT_ID=your_azure_client_id
   export AZURE_CLIENT_SECRET=your_azure_client_secret
   export OPENAI_API_KEY=your_openai_api_key
   # ... and other required variables
   ```

3. **Or set them in your deployment environment** (Docker, Kubernetes, etc.)

### Environment-Specific Configuration

- **Development**: Use `application.yml` with local environment variables
- **Testing**: Uses `application-test.yml` automatically when `SPRING_PROFILES_ACTIVE=test`
- **Production**: Use `application.yml` with production environment variables

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_USERNAME` | MySQL database username | `root` |
| `DB_PASSWORD` | MySQL database password | `your_password` |
| `REDIS_HOST` | Redis server hostname | `localhost` |
| `REDIS_PORT` | Redis server port | `6379` |
| `RABBITMQ_HOST` | RabbitMQ server hostname | `localhost` |
| `AZURE_TENANT_ID` | Azure AD tenant ID | `your-tenant-id` |
| `AZURE_CLIENT_ID` | Azure AD client ID | `your-client-id` |
| `AZURE_CLIENT_SECRET` | Azure AD client secret | `your-client-secret` |
| `OPENAI_API_KEY` | OpenAI API key | `sk-...` |

## CI/CD with GitHub Actions

The project includes a GitHub Actions workflow that:

1. **Runs tests** with MySQL, Redis, and RabbitMQ services
2. **Builds the application** and creates a Docker image
3. **Deploys to staging** when pushing to `develop` branch
4. **Deploys to production** when pushing to `main` branch

### Testing in CI/CD

The workflow automatically:
- Uses `application-test.yml` for testing
- Sets up test databases and services
- Runs with optimized settings for faster execution
- Generates test reports

### Deployment

To deploy to your environments:

1. **Set up GitHub Secrets** for your deployment environments
2. **Configure your deployment targets** in the workflow
3. **Push to the appropriate branch** to trigger deployment

Example deployment commands (uncomment in workflow):
```yaml
# For Kubernetes
kubectl apply -f k8s/staging/

# For Docker Registry
docker push your-registry/peaknote-backend:staging
```
