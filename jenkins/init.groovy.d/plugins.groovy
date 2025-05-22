import jenkins.model.*
import hudson.PluginWrapper
import hudson.model.UpdateCenter
import hudson.util.VersionNumber

def instance = Jenkins.getInstance()

def pluginParameterList = [
  "workflow-aggregator",       // For pipelines
  "git",                       // Git support
  "github",                    // GitHub integration
  "github-branch-source",      // GitHub multibranch pipeline support
  "pipeline-stage-view",       // Pipeline visualization
  "credentials-binding",       // For securely binding credentials
  "blueocean",                 // Blue Ocean UI
  "sonar",                     // SonarQube scanner
  "ssh-slaves",                // SSH agents
  "matrix-auth",               // Matrix-based security
  "mailer",                    // Email notifications
  "email-ext",                 // Extended email support
  "antisamy-markup-formatter"  // Safe HTML formatting
]

def pm = instance.getPluginManager()
def uc = instance.getUpdateCenter()

pluginParameterList.each { pluginId ->
    if (!pm.getPlugin(pluginId)) {
        println "Installing plugin: $pluginId"
        def plugin = uc.getPlugin(pluginId)
        if (plugin) {
            plugin.deploy(true)
        } else {
            println "Plugin not found in Update Center: $pluginId"
        }
    } else {
        println "Plugin already installed: $pluginId"
    }
}

// Save Jenkins state
instance.save()
