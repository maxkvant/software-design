package ru.spbau.bogomolov.ast.commands

import ru.spbau.bogomolov.ast.AstNode
import ru.spbau.bogomolov.environment.Environment
import java.io.File

/**
 * If first token is 'cd' then parsing is successful, other tokens are treated as arguments.
 */
fun parseCdFromTokens(env: Environment, tokens: List<String>): Cd? {
    if (tokens.isEmpty() || tokens[0] != "cd") {
        return null
    }
    return Cd(env, tokens.subList(1, tokens.size).toTextNodes(true))
}

/**
 * cd command. Should have 1 or 0 arguments. Argument is treated as path to directory.
 * If there is 1 argument then changes working directory to argument.
 * If there are no arguments then changes working directory to home path.
 */
class Cd(private val env: Environment, private val args: List<AstNode>) : Command(args, "cd", false, false) {

    override fun consumeArgument(arg: AstNode) { }

    override fun invoke() {
        reset()
        try {
            if (args.isEmpty()) {
                val homePath: String = System.getProperty("user.home")
                env.setDirectory(homePath)
            } else {
                require(args.size == 1) { "too many arguments" }

                val dirTo = args[0].getOutput()
                val dir = File(env.inCurrentWorkDir(dirTo))

                require(dir.exists() && dir.isDirectory) { "no such directory: ${dir.absolutePath}" }

                env.setDirectory(dir.absolutePath)
            }
            appendToOutput(env.getDirectory())
        } catch (e: Exception) {
            reset()
            e.message?. let { appendToErrors(it) }
        }
    }

    override fun shouldExit() = false

}