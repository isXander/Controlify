package dev.isxander.controlify

import dev.isxander.modstitch.base.extensions.ModstitchExtension
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.InclusiveRepositoryContentDescriptor
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*

internal val Project.modstitch: ModstitchExtension
    get() = extensions.getByType<ModstitchExtension>()

internal val Project.stonecutter: StonecutterBuildExtension
    get() = extensions.getByType<StonecutterBuildExtension>()

val Project.branchProj: Project
    get() = stonecutter.node.sibling("")!!.project

val Project.modVersion: String
    get() = prop("modVersion")!!
val Project.mcVersion: String
    get() = prop("mcVersion")!!
val Project.isPublishingEnabled: Boolean
    get() = prop("pub.enabled") { "true" }!!.toBooleanStrict()

fun <T> Project.propMap(property: String, required: Boolean = false, ifNull: () -> String? = { null }, block: (String) -> T?): T? {
    return ((System.getenv(property) ?: branchProj.findProperty(property)?.toString())
        ?.takeUnless { it.isBlank() }
        ?: ifNull())
        .let { if (required && it == null) error("Property $property is required") else it }
        ?.let(block)
}

fun Project.prop(property: String, required: Boolean = false, ifNull: () -> String? = { null }): String? {
    return propMap(property, required, ifNull) { it }
}

fun Project.isPropDefined(property: String): Boolean {
    return propMap(property) { true } == true
}

// Creates a global task that will only run on the active project
fun Project.createActiveTask(
    taskProvider: TaskProvider<*>? = null,
    taskName: String? = null,
    internal: Boolean = false
): String {
    val taskExists = taskProvider != null || taskName!! in tasks.names
    val task = taskProvider ?: taskName?.takeIf { taskExists }?.let { tasks.named(it) }
    val taskName = when {
        taskProvider != null -> taskProvider.name
        taskName != null -> taskName
        else -> error("Either taskProvider or taskName must be provided")
    }
    val activeTaskName = "${taskName}Active"

    if (stonecutter.current.isActive) {
        rootProject.tasks.register(activeTaskName) {
            group = "controlify${if (internal) "/versioned" else ""}"

            task?.let { dependsOn(it) }
        }
    }

    return activeTaskName
}

fun RepositoryHandler.strictMaven(url: String, action: Action<in InclusiveRepositoryContentDescriptor>) =
    exclusiveContent {
        forRepository { maven(url) }
        filter(action)
    }


