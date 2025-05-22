import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

// Create or get the security realm
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def users = [
  [ "admin",         "Administrator@123", "admin@admin.com" ],
  [ "admindev",      "Administrator@123", "admindev@admin.com" ],
  [ "spampana",      "Clouddevops#2025", "sreenivas9836@gmail.com" ],
  [ "vsiraparapu",   "Clouddevops#2025", "vanitha0211rani@gmail.com" ],
  [ "jkandregula",   "Clouddevops#2025", "jayanthkandregula01@gmail.com" ],
  [ "ssiraparapu",   "Clouddevops#2025", "sivasaisiraparapu408@gmail.com" ],
  [ "nboddu",        "Clouddevops#2025", "b.y.naidu1998@gmail.com" ],
  [ "ugorle",        "Clouddevops#2025", "gorleumadevi0109@gmail.com" ],
  [ "kgandi",        "Clouddevops#2025", "gandikavya29@gmail.com" ],
  [ "skamireddy",    "Clouddevops#2025", "sudhakirankamireddy@gmail.com" ],
  [ "dsingampalli",  "Clouddevops#2025", "deviramesh37@gmail.com" ],
  [ "hjagarapu",     "Clouddevops#2025", "jagarapusaihemanth@gmail.com" ],
  [ "sgorle",        "Clouddevops#2025", "gsivakumarvirat183@gmail.com" ],
  [ "vpithani",      "Clouddevops#2025", "vamsikrishna2567@gmail.com" ],
  [ "kbayyavarapu",  "Clouddevops#2025", "kulasekhar2027@gmail.com" ],
]

users.each { u ->
  if (hudsonRealm.getUser(u[0]) == null) {
    def user = hudsonRealm.createAccount(u[0], u[1])
    user.addProperty(new hudson.tasks.Mailer.UserProperty(u[2]))
    println "✅ Created user: ${u[0]}"
  } else {
    println "ℹ️ User already exists: ${u[0]}"
  }
}

instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()