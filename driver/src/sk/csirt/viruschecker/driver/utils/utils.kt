package sk.csirt.viruschecker.driver.utils

import java.nio.file.Files
import java.nio.file.Paths

fun Iterable<String>.cleanCommentsAndEmptyLines() =
    asSequence()
        .filterNot { it.startsWith("#") }
        .filterNot { it.isBlank() }
        .map { line ->
            line.indexOfFirst { '#' == it }
                .takeIf { it > 0 }
                ?.let { line.substring(0, it) } ?: line
        }.toList()

fun parseParameter(
    commandList: MutableList<String>,
    parameter: String,
    value: String
) {
    when {
        parameter.endsWith(":") -> commandList.add(parameter + value)
        parameter.endsWith("=") -> commandList.add(parameter + value)
        parameter.isBlank() -> commandList.add(value)
        parameter == "(none)" -> commandList.add(value)
        else -> commandList.add(parameter).also { commandList.add(value) }
    }
}

fun createDirectoryIfNotExists(directoryName: String){
    val directoryPath = Paths.get(directoryName)
    if(Files.exists(directoryPath).not()){
        Files.createDirectory(directoryPath)
    }
}