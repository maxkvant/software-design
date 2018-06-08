package ru.spbau.bogomolov.environment

import java.io.File
import java.nio.file.Paths

/**
 * Implementation of Environment wrapping a Map in local storage and a variable for working directory
 */
class LocalMapEnvironment : Environment {
    private val map = mutableMapOf<String, String>()

    private var workingDirectory: String = System.getProperty("user.dir")

    override fun getValue(name: String): String = map.getOrDefault(name, "")

    override fun setValue(name: String, value: String) {
        map[name] = value
    }

    override fun setDirectory(path: String) {
        require(File(path).exists() && File(path).isDirectory)
        workingDirectory =  Paths.get(path).normalize().toAbsolutePath().toString()

    }

    override fun getDirectory(): String {
        return workingDirectory
    }
}