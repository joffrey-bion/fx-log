package org.hildan.gradle.travis.bintray

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateBintrayDescriptorTask extends DefaultTask {

    @TaskAction
    def generate() {
        BintrayTravisExtension extension = (BintrayTravisExtension) project[BintrayTravisExtension.NAME]
        def bintrayDescriptor = new TreeMap<String,Object>()
        bintrayDescriptor['package'] = createPackage extension
        bintrayDescriptor['version'] = createVersion extension
        bintrayDescriptor['files'] = extension.files
        bintrayDescriptor['publish'] = extension.bintray.publish
        String json = JsonOutput.prettyPrint(JsonOutput.toJson(bintrayDescriptor))
        project.file(extension.outputFile).write json
    }

    static TreeMap<String, Object> createPackage(BintrayTravisExtension extension) {
        String githubUser = extension.github.user ?: extension.bintray.user
        String githubSlug = "$githubUser/$extension.github.repo"
        String githubRepoUrl = "https://github.com/$githubSlug"

        def packageDescriptor = new TreeMap<String,Object>()
        packageDescriptor['name'] = extension.bintray.packageName
        packageDescriptor['repo'] = extension.bintray.repo
        packageDescriptor['subject'] = extension.bintray.user ?: extension.github.user
        packageDescriptor['desc'] = extension.description
        packageDescriptor['website_url'] = extension.websiteUrl ?: githubRepoUrl
        packageDescriptor['issue_tracker_url'] = extension.issueTrackerUrl ?: githubRepoUrl + '/issues'
        packageDescriptor['vcs_url'] = extension.vcsUrl ?: githubRepoUrl + '.git'
        packageDescriptor['github_use_tag_release_notes'] = true
        packageDescriptor['github_release_notes_file'] = extension.github.releaseNotesFile
        packageDescriptor['licenses'] = extension.licenses
        packageDescriptor['labels'] = extension.labels
        packageDescriptor['public_download_numbers'] = extension.bintray.publicDownloadNumbers
        packageDescriptor['public_stats'] = extension.bintray.publicStats
        packageDescriptor['attributes'] = extension.attributes
        return packageDescriptor
    }

    static TreeMap<String, Object> createVersion(BintrayTravisExtension extension) {
        def versionDescriptor = new TreeMap<String,Object>()
        versionDescriptor['name'] = extension.release.name
        versionDescriptor['desc'] = extension.release.description ?: extension.description
        versionDescriptor['released'] = extension.release.releaseDate
        versionDescriptor['vcsTag'] = extension.release.vcsTag ?: 'v' + extension.release.name
        versionDescriptor['attributes'] = extension.release.attributes ?: extension.attributes
        versionDescriptor['gpgSign'] = extension.release.gpgSign
        return versionDescriptor
    }
}