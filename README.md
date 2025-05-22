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

## Nginx Reverse Proxy Configuration

The Nginx reverse proxy is a crucial component that routes traffic to different services based on URL paths. Here's a detailed explanation of the configuration:

### Architecture Diagram

```plaintext
                                     ┌─────────────────┐
                                     │                 │
                      80/443        │  Nginx Reverse  │
Client Requests ─────────────────►  │     Proxy       │
                                     │                 │
                                     └─────────┬───────┘
                                              │
                    ┌──────────────────┬──────┴───────┬───────────────┐
                    │                  │              │               │
                    ▼                  ▼              ▼               ▼
             ┌──────────────┐  ┌──────────────┐ ┌──────────┐  ┌───────────┐
             │   Jenkins    │  │  SonarQube   │ │  Nexus   │  │ Other     │
             │ (port 8080) │  │ (port 9000)  │ │(port 8081)│  │ Services  │
             └──────────────┘  └──────────────┘ └──────────┘  └───────────┘
```

### Route Configuration

The Nginx reverse proxy routes are configured as follows:

1. **Jenkins** (`/jenkins`):
   ```nginx
   location /jenkins {
       proxy_pass http://jenkins:8080/jenkins;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
   }
   ```

2. **SonarQube** (`/sonarqube`):
   ```nginx
   location /sonarqube {
       proxy_pass http://sonarqube:9000/sonarqube;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   }
   ```

3. **Nexus Repository** (`/`):
   ```nginx
   location / {
       proxy_pass http://nexus:8081/;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   }
   ```

### Complete Nginx Configuration

Create a file at `nginx/default.conf` with the following content:

```nginx
server {
    listen 80;
    server_name localhost;

    # Increase max body size for artifact uploads
    client_max_body_size 1G;
    
    # Proxy headers for all locations
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;

    # Jenkins proxy configuration
    location /jenkins {
        proxy_pass http://jenkins:8080/jenkins;
        proxy_redirect off;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header X-Jenkins-Context /jenkins;
        
        # WebSocket support
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection upgrade;
    }

    # SonarQube proxy configuration
    location /sonarqube {
        proxy_pass http://sonarqube:9000/sonarqube;
        proxy_redirect off;
        
        # Additional SonarQube specific settings
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }

    # Nexus proxy configuration
    location / {
        proxy_pass http://nexus:8081/;
        proxy_redirect off;

        # Nexus specific settings
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }
}
```

### Route Flow Diagram

```plaintext
Client Request
      │
      ▼
┌─────────────┐
│  Nginx 80   │
└─────────────┘
      │
      ├─────────────────┬─────────────────┐
      │                 │                 │
      ▼                 ▼                 ▼
/jenkins path     /sonarqube path    / (root path)
      │                 │                 │
      ▼                 ▼                 ▼
Jenkins:8080      SonarQube:9000    Nexus:8081
```

### Important Considerations

1. **SSL/TLS**: For production environments, add SSL configuration:
   ```nginx
   listen 443 ssl;
   ssl_certificate /etc/nginx/ssl/server.crt;
   ssl_certificate_key /etc/nginx/ssl/server.key;
   ```

2. **Security Headers**: Consider adding security headers:
   ```nginx
   add_header X-Frame-Options "SAMEORIGIN";
   add_header X-XSS-Protection "1; mode=block";
   add_header X-Content-Type-Options "nosniff";
   ```

3. **Rate Limiting**: For production, implement rate limiting:
   ```nginx
   limit_req_zone $binary_remote_addr zone=mylimit:10m rate=10r/s;
   location / {
       limit_req zone=mylimit burst=20 nodelay;
   }
   ```

### Troubleshooting

1. **502 Bad Gateway**:
   - Check if the backend service is running
   - Verify network connectivity
   - Check service logs

2. **504 Gateway Timeout**:
   - Increase proxy timeouts:
     ```nginx
     proxy_connect_timeout 60s;
     proxy_send_timeout 60s;
     proxy_read_timeout 60s;
     ```

3. **413 Request Entity Too Large**:
   - Adjust `client_max_body_size` in the Nginx configuration

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