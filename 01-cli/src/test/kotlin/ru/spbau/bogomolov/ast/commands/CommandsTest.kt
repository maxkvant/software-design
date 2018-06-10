package ru.spbau.bogomolov.ast.commands

import org.mockito.Mockito.*
import ru.spbau.bogomolov.ast.AstNode
import ru.spbau.bogomolov.environment.Environment
import ru.spbau.bogomolov.environment.LocalMapEnvironment
import kotlin.test.*

class CommandsTest {
    companion object {
        private val root = System.getProperty("user.dir")

        private const val commandName = "name"
        private const val errorText = "Unknown error\n"
        private const val outputText = "My output\n"
        private const val wcOutput = "Mama America\nPapa Russia\n"
        private const val varName = "var"
        private const val varValue = "value"

        private val mockedNodeWithErrors = mock(AstNode::class.java)
        private val mockedNodeWithOutput = mock(AstNode::class.java)
        private val mockedWcInput = mock(AstNode::class.java)
        private val mockedWcFile = mock(AstNode::class.java)
        private val mockedEchoInput = mock(AstNode::class.java)
        private val mockedEchoArgs = mock(AstNode::class.java)
        private val mockedCatFile = mock(AstNode::class.java)
        private val mockedEnvironment = mock(Environment::class.java)
        private val mockedLsEnvironment = mock(Environment::class.java)
        private val mockedParentPath = mock(AstNode::class.java)
        private val mockedTestPath = mock(AstNode::class.java)

        init {
            `when`(mockedEnvironment.getDirectory()).thenReturn(root)

            `when`(mockedNodeWithErrors.getErrors()).thenReturn(errorText)

            `when`(mockedNodeWithOutput.getOutput()).thenReturn(outputText)
            `when`(mockedNodeWithOutput.getErrors()).thenReturn("")

            `when`(mockedWcInput.getOutput()).thenReturn(wcOutput)
            `when`(mockedWcInput.getErrors()).thenReturn("")
            `when`(mockedWcInput.isArgument()).thenReturn(false)

            `when`(mockedWcFile.getOutput()).thenReturn("""$root/src/test/resources/wc.txt""")
            `when`(mockedWcFile.getErrors()).thenReturn("")
            `when`(mockedWcFile.isArgument()).thenReturn(true)

            `when`(mockedEchoInput.getOutput()).thenReturn(outputText)
            `when`(mockedEchoInput.getErrors()).thenReturn("")
            `when`(mockedEchoInput.isArgument()).thenReturn(false)

            `when`(mockedEchoArgs.getOutput()).thenReturn(outputText)
            `when`(mockedEchoArgs.getErrors()).thenReturn("")
            `when`(mockedEchoArgs.isArgument()).thenReturn(true)

            `when`(mockedCatFile.getOutput()).thenReturn("""$root/src/test/resources/cat.txt""")
            `when`(mockedCatFile.getErrors()).thenReturn("")
            `when`(mockedCatFile.isArgument()).thenReturn(true)

            `when`(mockedLsEnvironment.getDirectory()).thenReturn("""$root/src/test/resources""")
            `when`(mockedParentPath.getOutput()).thenReturn("..")
            `when`(mockedTestPath.getOutput()).thenReturn("src/test/resources")
        }
    }

    @Test
    fun generalCommandNoArguments() {
        val command = CommandWithNoArgs()
        command.invoke()
        assertEquals("", command.getOutput())
        assertEquals("No arguments provided to $commandName\n", command.getErrors())
    }

    class CommandWithNoArgs : Command(emptyList(), commandName, true, false) {
        override fun consumeArgument(arg: AstNode) {}
        override fun shouldExit() = false
    }

    @Test
    fun generalCommandCollectErrors() {
        val command = CommandWithErrorArgs()
        command.invoke()
        assertEquals("Error in argument $commandName:\n${errorText}" +
                "Error in argument $commandName:\n${errorText}", command.getErrors())
    }

    class CommandWithErrorArgs : Command(
            listOf(mockedNodeWithErrors, mockedNodeWithErrors),
            commandName,
            true,
            false
    ) {
        override fun consumeArgument(arg: AstNode) {}
        override fun shouldExit() = false
    }

    @Test
    fun generalCommandConsumeArgs() {
        val command = CommandWithConsume()
        command.invoke()
        assertEquals("", command.getErrors())
        assertEquals("${outputText}${outputText}", command.getOutput())
    }

    class CommandWithConsume : Command(
            listOf(mockedNodeWithOutput, mockedNodeWithOutput),
            commandName,
            true,
            false
    ) {
        override fun consumeArgument(arg: AstNode) {
            appendToOutput(arg.getOutput())
        }

        override fun shouldExit() = false
    }

    @Test
    fun wcTestInput() {
        val wc = Wc(mockedEnvironment, listOf(mockedWcInput))
        wc.invoke()
        assertEquals("2 4 25\n", wc.getOutput())
    }

    @Test
    fun wcTestFile() {
        val wc = Wc(mockedEnvironment, listOf(mockedWcFile))
        wc.invoke()
        assertTrue(wc.getOutput().startsWith("5 15 59 "))
    }

    @Test
    fun pwdTest() {
        val pwd = Pwd(mockedEnvironment)
        pwd.invoke()
        assertEquals(root + "\n", pwd.getOutput())
    }

    @Test
    fun exitTest() {
        val exit = Exit()
        exit.invoke()
        assertTrue(exit.shouldExit())
    }

    @Test
    fun echoTestInput() {
        val echo = Echo(listOf(mockedEchoInput))
        echo.invoke()
        assertEquals(outputText + "\n", echo.getOutput())
    }

    @Test
    fun echoTestArgs() {
        val echo = Echo(listOf(mockedEchoArgs, mockedEchoArgs))
        echo.invoke()
        assertEquals(outputText + "\n" + outputText + "\n", echo.getOutput())
    }

    @Test
    fun catTestFile() {
        val cat = Cat(mockedEnvironment, listOf(mockedCatFile))
        cat.invoke()
        assertEquals("some random text\n" +
                "on several lines\n" +
                "that i'm writing now\n", cat.getOutput())
    }

    @Test
    fun assignmentTest() {
        val assignment = Assignment(mockedEnvironment, varName, varValue)
        assignment.invoke()
        verify(mockedEnvironment).setValue(varName, varValue)
    }

    @Test
    fun lsTestNoArguments() {
        val ls = Ls(mockedLsEnvironment, listOf())
        ls.invoke()
        val lsOutput = ls.getOutput()
        assertTrue { lsOutput.contains("cat.txt") }
        assertTrue { lsOutput.contains("wc.txt") }
    }

    @Test
    fun lsTestArgument() {
        val ls = Ls(mockedLsEnvironment, listOf(mockedParentPath))
        ls.invoke()
        val lsOutput = ls.getOutput()
        assertTrue { lsOutput.contains("resources") }
    }

    @Test
    fun cdTestNoArguments() {
        val env = LocalMapEnvironment()
        val cd = Cd(env, listOf())
        cd.invoke()

        assertEquals(System.getProperty("user.home"), env.getDirectory())
        val pwd = Pwd(env)
        pwd.invoke()
        assertEquals(System.getProperty("user.home") + "\n", pwd.getOutput())
    }

    @Test
    fun cdTestArgument() {
        val env = LocalMapEnvironment()
        Cd(env, listOf(mockedTestPath)).invoke()

        Cd(env, listOf(mockedParentPath)).invoke()
        Cd(env, listOf(mockedParentPath)).invoke()
        Cd(env, listOf(mockedParentPath)).invoke()

        assertEquals(root, env.getDirectory())
        val pwd = Pwd(env)
        pwd.invoke()
        assertEquals("$root\n", pwd.getOutput())
    }
}