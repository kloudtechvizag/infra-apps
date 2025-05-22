# DevOps Infrastructure Stack

This repository contains a Docker Compose configuration for setting up a complete DevOps infrastructure stack. It includes essential tools for continuous integration, code quality analysis, and artifact management.

## Components

The stack includes the following services:

- **Jenkins** - Continuous Integration/Continuous Deployment server
- **SonarQube** - Code quality and security analysis
- **Nexus Repository** - Artifact repository manager
- **Nginx** - Reverse proxy for service routing
- **PostgreSQL** - Database for SonarQube

## Prerequisites

Before you begin, ensure you have the following installed:

- Docker Engine (version 20.10.0 or later)
- Docker Compose (version 2.0.0 or later)
- Minimum 8GB RAM available
- At least 20GB free disk space

## Installation

1. Clone this repository:
   ```bash
   git clone <repository-url>
   cd <repository-name>
   ```

2. Create the necessary nginx configuration:
   ```bash
   mkdir -p nginx
   # Create nginx/default.conf with your reverse proxy configuration
   ```

3. Start the services:
   ```bash
   docker-compose up -d
   ```

## Service Access

After successful deployment, the services will be available at:

- Jenkins: http://localhost/jenkins
  - Default port: 8080
  - Initial admin password location: `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`

- SonarQube: http://localhost/sonarqube
  - Default port: 9000
  - Default credentials: admin/admin

- Nexus Repository: http://localhost:8081
  - Default credentials: admin/admin123
  - Initial admin password location: `docker exec nexus cat /nexus-data/admin.password`

## Volume Information

The setup uses Docker volumes for persistent storage:

- `jenkins_home`: Jenkins data and configuration
- `sonarqube_data`: SonarQube data
- `sonarqube_extensions`: SonarQube plugins and extensions
- `postgresql`: SonarQube database
- `nexus-data`: Nexus Repository data

## Configuration

### Environment Variables

#### SonarQube
- SONAR_JDBC_URL: jdbc:postgresql://db:5432/sonar
- SONAR_JDBC_USERNAME: sonar
- SONAR_JDBC_PASSWORD: sonar
- SONAR_WEB_CONTEXT: /sonarqube

#### Jenkins
- JENKINS_OPTS: --prefix=/jenkins

#### PostgreSQL
- POSTGRES_USER: sonar
- POSTGRES_PASSWORD: sonar
- POSTGRES_DB: sonar

## Network Configuration

All services are connected through a bridge network named 'backend' for secure internal communication.

## Ports

- Nginx: 80
- Jenkins: 8080 (UI), 50000 (agents)
- SonarQube: 9000
- Nexus: 8081

## Maintenance

### Backup

To backup the data, you should regularly backup the Docker volumes:

1. Stop the services:
   ```bash
   docker-compose down
   ```

2. Backup the volumes:
   ```bash
   docker run --rm -v jenkins_home:/source:ro -v $(pwd):/backup alpine tar czf /backup/jenkins_backup.tar.gz -C /source ./
   docker run --rm -v nexus-data:/source:ro -v $(pwd):/backup alpine tar czf /backup/nexus_backup.tar.gz -C /source ./
   docker run --rm -v sonarqube_data:/source:ro -v $(pwd):/backup alpine tar czf /backup/sonarqube_backup.tar.gz -C /source ./
   ```

### Updates

To update the services:

1. Pull the latest images:
   ```bash
   docker-compose pull
   ```

2. Restart the services:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

## Troubleshooting

### Common Issues

1. **Services fail to start**
   - Check system resources (RAM, CPU)
   - Verify all ports are available
   - Check logs: `docker-compose logs [service_name]`

2. **Permission Issues**
   - Ensure proper permissions on mounted volumes
   - Check SELinux settings if applicable

3. **Memory Issues**
   - Adjust Docker memory limits
   - Configure JVM memory settings for Java-based services

### Logs

To view logs for specific services:
```bash
docker-compose logs -f [service_name]
```

## Security Considerations

1. Change default passwords immediately after first login
2. Configure SSL/TLS for production use
3. Implement proper backup strategies
4. Regularly update all services
5. Configure proper authentication mechanisms

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.