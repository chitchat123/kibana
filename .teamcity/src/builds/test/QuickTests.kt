package builds.test

import addSlackNotifications
import addTestArtifacts
import failedTestReporter
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import junit
import kibanaAgent

object QuickTests : BuildType({
  name = "Quick Tests"
  paused = true
  description = "Executes Quick Tests"

  kibanaAgent(2)

  val testScripts = mapOf(
    "Test Hardening" to ".ci/teamcity/tests/test_hardening.sh",
    "X-Pack List cyclic dependency" to ".ci/teamcity/tests/xpack_list_cyclic_dependency.sh",
    "X-Pack SIEM cyclic dependency" to ".ci/teamcity/tests/xpack_siem_cyclic_dependency.sh",
    "Test Projects" to ".ci/teamcity/tests/test_projects.sh",
    "Mocha Tests" to ".ci/teamcity/tests/mocha.sh"
  )

  steps {
    for (testScript in testScripts) {
      script {
        name = testScript.key
        scriptContent = """
          #!/bin/bash
          ${testScript.value}
        """.trimIndent()
      }
    }

    failedTestReporter()
  }

  features {
    junit()
  }

  addTestArtifacts()
  addSlackNotifications()
})
