package org.hildan.gradle.travis.bintray

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

class BintrayTravisExtension {

    public static String NAME = "travis"

    String outputFile = 'bintray.json'
    String description
    String websiteUrl
    String vcsUrl
    String issueTrackerUrl
    Map[] files = []
    String[] licenses = []
    String[] labels = []
    Map[] attributes = []

    ReleaseExtension release
    BintrayExtension bintray
    GithubExtension github

    BintrayTravisExtension(Project project) {
        description = project.description
        release = new ReleaseExtension(project)
        bintray = new BintrayExtension(project)
        github = new GithubExtension(project)
    }

    def release(Closure closure) {
        ConfigureUtil.configure(closure, release)
    }

    def bintray(Closure closure) {
        ConfigureUtil.configure(closure, bintray)
    }

    def github(Closure closure) {
        ConfigureUtil.configure(closure, github)
    }

    class ReleaseExtension {
        String name
        String description
        String vcsTag
        String releaseDate = new Date().format("yyyy-MM-dd", TimeZone.getTimeZone("UTC"))
        Map[] attributes
        boolean gpgSign = false

        ReleaseExtension(Project project) {
            name = project.version
        }
    }

    class BintrayExtension {
        String user
        String repo
        String packageName
        boolean publish = true
        boolean publicDownloadNumbers = false
        boolean publicStats = false

        BintrayExtension(Project project) {
            packageName = project.name
        }
    }

    class GithubExtension {
        String user
        String repo
        String releaseNotesFile = 'RELEASE.md'

        GithubExtension(Project project) {
            repo = project.name
        }
    }

}
