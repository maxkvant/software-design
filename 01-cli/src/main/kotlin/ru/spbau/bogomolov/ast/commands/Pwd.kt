package ru.spbau.bogomolov.ast.commands

import ru.spbau.bogomolov.ast.AstNode
import ru.spbau.bogomolov.environment.Environment

/**
 * If first token is 'ls' then parsing is successful, other tokens are treated as arguments.
 */
fun parsePwdFromTokens(env: Environment, tokens: List<String>): Pwd? {
    if (tokens.isEmpty() || tokens[0] != "pwd") {
        return null
    }
    return Pwd(env)
}

/**
 * pwd command. Doesn't take arguments. Prints path to current directory.
 */
class Pwd(private val env: Environment) : Command(emptyList(), "pwd", false, false) {

    override fun consumeArgument(arg: AstNode) {}

    override fun shouldExit() = false

    override fun invoke() {
        setOutput(env.getDirectory() + "\n")
    }
}