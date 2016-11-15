package org.hildan.gradle.travis.bintray

import com.jfrog.bintray.gradle.BintrayPlugin
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class BintrayTravisPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(BintrayPlugin)
        BintrayExtension ext = project.extensions.getByType(BintrayExtension)

        project.extensions.create(BintrayTravisExtension.NAME, BintrayTravisExtension, project)
        project.task(GenerateBintrayDescriptorTask.NAME, type: GenerateBintrayDescriptorTask)
    }
}