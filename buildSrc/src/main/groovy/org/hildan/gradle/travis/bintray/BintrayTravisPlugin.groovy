package org.hildan.gradle.travis.bintray

import org.gradle.api.Plugin
import org.gradle.api.Project

class BintrayTravisPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
//        project.pluginManager.apply('com.jfrog.bintray')
        project.extensions.create(BintrayTravisExtension.NAME, BintrayTravisExtension, project)
        project.task('generateBintrayDescriptor', type: GenerateBintrayDescriptorTask)
    }
}