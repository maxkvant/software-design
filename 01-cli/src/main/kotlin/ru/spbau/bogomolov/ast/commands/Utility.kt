package ru.spbau.bogomolov.ast.commands

import ru.spbau.bogomolov.ast.utilitynodes.TextNode
import ru.spbau.bogomolov.environment.Environment
import java.io.File

/**
 * Transforms list of strings into list of text nodes representing those strings.
 */
fun List<String>.toTextNodes(isArg: Boolean) = this.map { TextNode(it, isArg) }

fun String.toWords() = this.split("\n", "\t", " ").filterNot { s -> s.isEmpty() }

fun Environment.inCurrentWorkDir(path: String): String {
    val currentDir = this.getDirectory()
    return File(currentDir).resolve(path).absolutePath

}