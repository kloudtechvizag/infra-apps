import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

// Create or get the security realm
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def users = [
  [ "admin",         "Administrator@123", "admin", "admin", "admin@admin.com" ],
  [ "spampana",      "Clouddevops#2025", "SREENIVASARAO", "PAMPANA", "sreenivas9836@gmail.com" ],
  [ "vsiraparapu",   "Clouddevops#2025", "Vanitha", "Siraparapu", "vanitha0211rani@gmail.com" ],
  [ "jkandregula",   "Clouddevops#2025", "Jayanth sai srinivas", "kandregula", "jayanthkandregula01@gmail.com" ],
  [ "ssiraparapu",   "Clouddevops#2025", "Sivasai", "siraparapu", "sivasaisiraparapu408@gmail.com" ],
  [ "nboddu",        "Clouddevops#2025", "Naidu", "Boddu", "b.y.naidu1998@gmail.com" ],
  [ "ugorle",        "Clouddevops#2025", "umadevi", "gorle", "gorleumadevi0109@gmail.com" ],
  [ "kgandi",        "Clouddevops#2025", "kavya", "gandi", "gandikavya29@gmail.com" ],
  [ "skamireddy",    "Clouddevops#2025", "sudha kiran", "kamireddy", "sudhakirankamireddy@gmail.com" ],
  [ "dsingampalli",  "Clouddevops#2025", "Devi", "singampalli", "deviramesh37@gmail.com" ],
  [ "hjagarapu",     "Clouddevops#2025", "Hemanth", "Jagarapu", "jagarapusaihemanth@gmail.com" ],
  [ "sgorle",        "Clouddevops#2025", "SHIVA KUMAR", "GORLE", "gsivakumarvirat183@gmail.com" ],
  [ "vpithani",      "Clouddevops#2025", "VAMSI KRISHNA", "PITHANI", "vamsikrishna2567@gmail.com" ],
  [ "kbayyavarapu",  "Clouddevops#2025", "KULA SEKHAR", "BAYYAVARAPU", "kulasekhar2027@gmail.com" ],
]

users.each { u ->
  if (hudsonRealm.getUser(u[0]) == null) {
    def user = hudsonRealm.createAccount(u[0], u[1])
    user.setFullName("${u[2]} ${u[3]}")
    user.addProperty(new hudson.tasks.Mailer.UserProperty(u[4]))
    println "✅ Created user: ${u[0]}"
  } else {
    println "ℹ️ User already exists: ${u[0]}"
  }
}

instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy if not already configured
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()
