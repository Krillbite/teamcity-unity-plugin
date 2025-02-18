/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.kotlinFile
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.10"

project {
    vcsRoot(TagReleaseVcs)
    vcsRoot(PullRequestVcs)
    vcsRoot(MasterVcs)

    buildType(ReleaseBuildConfiguration)
    buildType(PullRequestBuildConfiguration)
    buildType(MasterBuildConfiguration)
}

object TagReleaseVcs : GitVcsRoot({
    id("TeamCityUnityPlugin_TagReleaseVcs")
    name = "TagReleaseVcs"
    url = "https://github.com/JetBrains/teamcity-unity-plugin.git"
    branch = "master"
    useTagsAsBranches = true
    branchSpec = """
        +:refs/(tags/*)
        -:<default>
    """.trimIndent()
})

object ReleaseBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_ReleaseBuild")
    name = "ReleaseBuild"

    params {
        password("env.ORG_GRADLE_PROJECT_jetbrains.marketplace.token", "credentialsJSON:9836dc1a-9546-4018-b7c5-b5cbbaa03213", readOnly = true)
    }

    vcs {
        root(TagReleaseVcs)
    }

    steps {
        kotlinFile {
            name = "calculate version"
            path = "./.teamcity/steps/calculateVersion.kts"
            arguments = "%teamcity.build.branch%"
        }
        gradle {
            name = "build"
            tasks = "clean build serverPlugin"
        }
        gradle {
            name = "publish to marketplace"
            tasks = "publishPlugin"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:tags/v*"
        }
    }
})

object PullRequestVcs : GitVcsRoot({
    id("TeamCityUnityPlugin_PullRequestVcs")
    name = "PullRequestVcs"
    url = "https://github.com/JetBrains/teamcity-unity-plugin.git"
    branchSpec = """
        -:<default>
    """.trimIndent()
})

object PullRequestBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_PullRequestBuild")
    name = "PullRequestBuild"

    val githubTokenParameter = "GITHUB_TOKEN"
    params {
        password(githubTokenParameter, "credentialsJSON:da577601-ec6c-4387-8996-e14771fe5ca2", readOnly = true)
    }

    vcs {
        root(PullRequestVcs)
    }

    features {
        pullRequests {
            vcsRootExtId = PullRequestVcs.id?.value
            provider = github {
                filterTargetBranch = "refs/heads/master"
                filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
                authType = token {
                    token = "%$githubTokenParameter%"
                }
            }
        }
        commitStatusPublisher {
            vcsRootExtId = PullRequestVcs.id?.value
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%$githubTokenParameter%"
                }
            }
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        gradle {
            name = "build"
            tasks = "clean build serverPlugin"
        }
    }
})

object MasterVcs : GitVcsRoot({
    id("TeamCityUnityPlugin_MasterVcs")
    name = "MasterVcs"
    url = "https://github.com/JetBrains/teamcity-unity-plugin.git"
    branch = "master"
})

object MasterBuildConfiguration : BuildType({
    id("TeamCityUnityPlugin_MasterBuild")
    name = "MasterBuild"

    vcs {
        root(MasterVcs)
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        gradle {
            name = "build"
            tasks = "clean build serverPlugin"
        }
    }
})