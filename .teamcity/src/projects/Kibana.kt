package projects

import DefaultRoot
import builds.*
import builds.default.*
import builds.oss.*
import builds.test.*
import dependsOn
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.slackConnection
import kibanaAgent
import templates.DefaultTemplate

class KibanaConfiguration() {
  var agentNetwork: String = "teamcity"
  var agentSubnet: String = "teamcity"

  constructor(init: KibanaConfiguration.() -> Unit) : this() {
    init()
  }
}

var kibanaConfiguration = KibanaConfiguration()

fun Kibana(config: KibanaConfiguration = KibanaConfiguration()) : Project {
  kibanaConfiguration = config

  return Project {
    params {
      param("teamcity.ui.settings.readOnly", "true")
    }

    vcsRoot(DefaultRoot)
    template(DefaultTemplate)

    defaultTemplate = DefaultTemplate

    features {
      val sizes = listOf("2", "4", "8", "16")
      for (size in sizes) {
        kibanaAgent(size)
      }

      feature {
        id = "kibana"
        type = "CloudProfile"
        param("agentPushPreset", "")
        param("profileId", "kibana")
        param("profileServerUrl", "")
        param("name", "kibana")
        param("total-work-time", "")
        param("credentialsType", "key")
        param("description", "")
        param("next-hour", "")
        param("cloud-code", "google")
        param("terminate-after-build", "true")
        param("terminate-idle-time", "30")
        param("enabled", "true")
        param("secure:accessKey", "credentialsJSON:447fdd4d-7129-46b7-9822-2e57658c7422")
      }

      slackConnection {
        id = "KIBANA_SLACK"
        displayName = "Kibana Slack"
        botToken = "credentialsJSON:39eafcfc-97a6-4877-82c1-115f1f10d14b"
        clientId = "12985172978.1291178427153"
        clientSecret = "credentialsJSON:8b5901fb-fd2c-4e45-8aff-fdd86dc68f67"
      }
    }

    buildType(Lint)

    subProject {
      id("Test")
      name = "Test"

      subProject {
        id("Jest")
        name = "Jest"

        buildType(Jest)
        buildType(XPackJest)
        buildType(JestIntegration)
      }

      buildType(ApiIntegration)
      buildType(QuickTests)
      buildType(AllTests)
    }

    subProject {
      id("OSS")
      name = "OSS Distro"

      buildType(OssBuild)

      subProject {
        id("OSS_Functional")
        name = "Functional"

        buildType(OssCiGroups)
        buildType(OssVisualRegression)
        buildType(OssFirefox)
        buildType(OssAccessibility)
        buildType(OssPluginFunctional)

        subProject {
          id("CIGroups")
          name = "CI Groups"

          ossCiGroups.forEach { buildType(it) }
        }
      }
    }

    subProject {
      id("Default")
      name = "Default Distro"

      buildType(DefaultBuild)

      subProject {
        id("Default_Functional")
        name = "Functional"

        buildType(DefaultCiGroups)
        buildType(DefaultVisualRegression)
        buildType(DefaultFirefox)
        buildType(DefaultAccessibility)
        buildType(DefaultSecuritySolution)

        subProject {
          id("Default_CIGroups")
          name = "CI Groups"

          defaultCiGroups.forEach { buildType(it) }
        }
      }
    }

    buildType(EssentialCi)
    buildType(BaselineCi)
  }
}
