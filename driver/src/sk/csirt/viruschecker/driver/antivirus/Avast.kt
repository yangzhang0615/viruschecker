package sk.csirt.viruschecker.driver.antivirus

import sk.csirt.viruschecker.driver.config.AntivirusType

class Avast(
    scanCommand: RunProgramCommand
) : CommandLineAntivirus(scanCommand) {

    override val antivirusName: String = AntivirusType.AVAST.antivirusName

    override suspend fun parseReport(
        report: List<String>,
        params: FileScanParameters
    ): Report =
        report
            .takeIf { it.isNotEmpty() }
            ?.let{ report.first() to report.first { "# Virus database" in it } }
            ?.let { (scanLine, databaseLine) ->
                scanLine.split("\t")[1].let {
                    Report(
                        status = when {
                            "OK" in it -> ScanStatusResult.OK
                            else -> ScanStatusResult.INFECTED
                        },
                        malwareDescription = it,
                        virusDatabaseVersion =
                        databaseLine.split(":")[1]
                    )
                }
            } ?: Report(ScanStatusResult.NOT_AVAILABLE, "", "")
}
