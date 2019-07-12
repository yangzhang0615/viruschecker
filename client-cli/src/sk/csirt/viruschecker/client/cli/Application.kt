package sk.csirt.viruschecker.client.cli

import com.xenomachina.argparser.ArgParser
import io.ktor.application.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import sk.csirt.viruschecker.client.cli.config.CommandLineArguments
import sk.csirt.viruschecker.client.config.httpClient
import sk.csirt.viruschecker.client.payload.FileMultiScanResponse
import sk.csirt.viruschecker.client.reporting.CommandLineReporter
import sk.csirt.viruschecker.client.reporting.CsvReporter
import sk.csirt.viruschecker.client.reporting.DefaultReporter
import sk.csirt.viruschecker.client.reporting.Reporter
import sk.csirt.viruschecker.client.service.DefaultScanService
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

lateinit var parsedArgs: CommandLineArguments

fun main(args: Array<String>) {
    parsedArgs = ArgParser(args).parseInto(::CommandLineArguments)
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val client = httpClient(parsedArgs.timeout)

    val urls = parsedArgs.driverUrls
    val fileToScan = parsedArgs.fileToScan

    val scanService = DefaultScanService(urls, client)
    val scanReport = runBlocking { scanService.scanFile(fileToScan) }

    printReports(scanReport)

    client.close()
    exitProcess(0)
}

private fun printReports(scanReport: FileMultiScanResponse) {
    val reportFile = parsedArgs.outputFile

    val reporters: List<Reporter> = listOf<Reporter>(
        CommandLineReporter()
    ) + if (reportFile != null) {
        if (reportFile.name.endsWith(".csv"))
            listOf<Reporter>(CsvReporter(reportFile))
        else
            listOf<Reporter>(DefaultReporter(reportFile))
    } else emptyList()


    reporters.forEach { it.saveReport(scanReport) }
}

