# DevOps Infrastructure Stack

This repository contains a Docker Compose configuration for setting up a complete DevOps infrastructure stack. It includes essential tools for continuous integration, code quality analysis, artifact management, and reverse proxy management.

## Components

The stack includes the following services:

- **Jenkins** - Continuous Integration/Continuous Deployment server
- **SonarQube** - Code quality and security analysis
- **Nexus Repository** - Artifact repository manager
- **Nginx Proxy Manager** - Web UI for Nginx reverse proxy configuration
- **PostgreSQL** - Database for SonarQube

## Architecture Overview

```
                                  ┌─────────────────────┐
                                  │                     │
                   80/443/81     │  Nginx Proxy        │
Client Requests ─────────────────►│  Manager            │
                                  │                     │
                                  └─────────┬───────────┘
                                           │
                 ┌──────────────────┬──────┴───────┬───────────────┐
                 │                  │              │               │
                 ▼                  ▼              ▼               ▼
          ┌──────────────┐  ┌──────────────┐ ┌──────────┐  ┌───────────┐
          │   Jenkins    │  │  SonarQube   │ │  Nexus   │  │ PostgreSQL │
          │ (port 8080) │  │ (port 9000)  │ │(port 8081)│  │ Database  │
          └──────────────┘  └──────────────┘ └──────────┘  └───────────┘
```

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

2. Start the services:
   ```bash
   docker-compose up -d
   ```

3. Configure Nginx Proxy Manager:
   - Access the Nginx Proxy Manager UI at http://localhost:81
   - Default login credentials:
     - Email: admin@example.com
     - Password: changeme
   - You'll be prompted to change these credentials on first login

## Service Access

After successful deployment, the services will be available at:

- **Jenkins**: http://localhost:8080
  - Initial admin password location: `docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword`
  - Pre-configured users are available (see Jenkins Configuration section)

- **SonarQube**: http://localhost:9000
  - Default credentials: admin/admin

- **Nexus Repository**: http://localhost:8081
  - Default credentials: admin/admin123
  - Initial admin password location: `docker exec nexus cat /nexus-data/admin.password`

- **Nginx Proxy Manager**: http://localhost:81
  - Default credentials: admin@example.com/changeme

## Jenkins Configuration

### Pre-configured Users

Jenkins is pre-configured with several users through the init scripts. The admin users are:

- Username: admin, Password: Administrator@123
- Username: admindev, Password: Administrator@123

Additional users are also configured with the password "Clouddevops#2025".

### Pre-installed Plugins

Jenkins comes with the following plugins pre-installed:

- workflow-aggregator (Pipeline support)
- git and github (Git/GitHub integration)
- github-branch-source (GitHub multibranch pipeline)
- pipeline-stage-view (Pipeline visualization)
- credentials-binding (Secure credentials)
- blueocean (Modern UI)
- sonar (SonarQube integration)
- ssh-slaves (SSH agents)
- matrix-auth (Security)
- mailer and email-ext (Email notifications)
- antisamy-markup-formatter (Safe HTML)

## Volume Information

The setup uses Docker volumes for persistent storage:

- `jenkins_home`: Jenkins data and configuration
- `sonarqube_data`: SonarQube data
- `sonarqube_extensions`: SonarQube plugins and extensions
- `postgresql`: SonarQube database
- `nexus-data`: Nexus Repository data
- `npm_data`: Nginx Proxy Manager data
- `npm_letsencrypt`: Nginx Proxy Manager SSL certificates

## Configuration Details

### SonarQube

- Database: PostgreSQL
- JDBC URL: jdbc:postgresql://db:5432/sonar
- Database credentials: sonar/sonar
- Web port: 9000

### Jenkins

- Web port: 8080
- Agent port: 50000
- Automatic setup wizard is disabled
- Custom initialization scripts in `./jenkins/init.groovy.d/`

### Nexus Repository

- Web port: 8081
- Data directory: /nexus-data

### Nginx Proxy Manager

- Web UI port: 81
- HTTP port: 80
- HTTPS port: 443

## Network Configuration

All services are connected through a bridge network named 'backend' for secure internal communication.

## Proxy Configuration with Nginx Proxy Manager

Nginx Proxy Manager provides a user-friendly web interface to configure proxy hosts, redirection rules, and SSL certificates.

### Setting Up Proxy Hosts

1. Access the Nginx Proxy Manager UI at http://localhost:81
2. Navigate to "Proxy Hosts" and click "Add Proxy Host"
3. Configure the following for each service:

#### For Jenkins:
- Domain Names: your-domain.com (or localhost for testing)
- Scheme: http
- Forward Hostname/IP: jenkins
- Forward Port: 8080
- Forward Path: /jenkins
- Enable "Block Common Exploits"
- Add SSL certificate if needed

#### For SonarQube:
- Domain Names: sonar.your-domain.com (or localhost for testing)
- Scheme: http
- Forward Hostname/IP: sonarqube
- Forward Port: 9000
- Enable "Block Common Exploits"
- Add SSL certificate if needed

#### For Nexus:
- Domain Names: nexus.your-domain.com (or localhost for testing)
- Scheme: http
- Forward Hostname/IP: nexus
- Forward Port: 8081
- Enable "Block Common Exploits"
- Add SSL certificate if needed

### SSL Certificate Management

Nginx Proxy Manager can automatically obtain and renew Let's Encrypt SSL certificates:

1. Navigate to "SSL Certificates" and click "Add SSL Certificate"
2. Choose "Let's Encrypt"
3. Enter your domain(s) and email address
4. Enable "Use a DNS Challenge"
5. Select your DNS provider and enter API credentials
6. Click "Save"

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
   docker run --rm -v npm_data:/source:ro -v $(pwd):/backup alpine tar czf /backup/npm_data_backup.tar.gz -C /source ./
   docker run --rm -v npm_letsencrypt:/source:ro -v $(pwd):/backup alpine tar czf /backup/npm_letsencrypt_backup.tar.gz -C /source ./
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

4. **Nginx Proxy Manager Issues**
   - Check if the database is properly initialized
   - Verify network connectivity between containers
   - Check logs: `docker-compose logs nginxproxymanager`

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
6. Use strong passwords for all services
7. Restrict access to management ports (8080, 9000, 8081, 81)

## Resource Requirements

| Service | CPU | Memory | Disk |
|---------|-----|--------|------|
| Jenkins | 1-2 cores | 1-2GB | 10GB+ |
| SonarQube | 2 cores | 2-4GB | 5GB+ |
| PostgreSQL | 1 core | 1GB | 5GB+ |
| Nexus | 2 cores | 2-4GB | 10GB+ |
| Nginx Proxy Manager | 1 core | 512MB | 1GB |

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.