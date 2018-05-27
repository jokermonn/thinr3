package com.joker.thinr3

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ThinR3Plugin implements Plugin<Project> {
  @Override
  void apply(Project project) {
    project.afterEvaluate {
      project.plugins.withId('com.android.application') {
        project.android.applicationVariants.all { ApplicationVariant variant ->
          variant.outputs.each { ApkVariantOutput variantOutput ->
            if (variantOutput.name.contains("release")) {
              def task = project.tasks.
                  findByName(
                      "transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}")
              if (task == null) {
                throw new GradleException(
                    "u should use gradle plugin 3.0+ and set minifyEnabled true for release")
              }
              task
                  .doLast {
                it.outputs
                    .files
                    .files
                    .grep { File file -> file.isDirectory() }
                    .each { File dir ->
                  dir.listFiles({ File file -> file.name.endsWith(".jar") } as FileFilter)
                      .each { File jar -> ASMHelper.collectRInfo(jar) }
                }
                it.outputs
                    .files
                    .files
                    .grep { File file -> file.isDirectory() }
                    .each { File dir ->
                  dir.listFiles({ File file -> file.name.endsWith(".jar") } as FileFilter)
                      .each { File jar -> ASMHelper.replaceAndDelRInfo(jar) }
                }
              }
            }
          }
        }
      }
    }
  }
}