package ru.spbau.bogomolov.ast.commands

import ru.spbau.bogomolov.ast.AstNode
import ru.spbau.bogomolov.environment.Environment
import java.io.File

/**
 * If first token is 'ls' then parsing is successful, other tokens are treated as arguments.
 */
fun parseLsFromTokens(env: Environment, tokens: List<String>): Ls? {
    if (tokens.isEmpty() || tokens[0] != "ls") {
        return null
    }
    return Ls(env, tokens.subList(1, tokens.size).toTextNodes(true))
}

/**
 * ls command. Takes 0 or 1 argument. Argument is treated as path to directory.
 * If there is 1 argument then changes working directory to argument.
 * If there are no arguments then changes working directory to home path.
 */
class Ls(private val env: Environment, private val args: List<AstNode>) : Command(emptyList(), "ls", false, false) {
    override fun consumeArgument(arg: AstNode) {}

    override fun shouldExit() = false

    override fun invoke() {
        reset()
        try {
            setOutput(env.getDirectory() + "\n")

            require(args.size <= 1) { "too many arguments" }

            val path = if (args.isEmpty()) env.getDirectory() else env.inCurrentWorkDir(args[0].getOutput())
            require(File(path).exists() && File(path).isDirectory) { "no such directory" }
            File(path).listFiles().forEach {
                appendToOutput(it.name + "\n")
            }
        } catch (e: Exception) {
            reset()
            e.message?. let { appendToErrors(it)  }
        }
    }
}