package sk.csirt.viruschecker.gateway.persistence.service

import sk.csirt.viruschecker.gateway.persistence.entity.AntivirusReportEntity
import sk.csirt.viruschecker.gateway.persistence.entity.ScanReportEntity
import sk.csirt.viruschecker.gateway.persistence.repository.ScanReportRepository
import sk.csirt.viruschecker.gateway.persistence.service.converter.toFileHashScanResponse
import sk.csirt.viruschecker.routing.payload.FileHashScanResponse

class PersistentScanReportService(
    private val reportRepository: ScanReportRepository
) : ScanReportService {
    override suspend fun save(response: FileHashScanResponse) {
        ScanReportEntity(
            date = response.report.date,
            filename = response.report.filename,
            sha256 = response.sha256,
            md5 = response.md5,
            sha1 = response.sha1,
            reports = response.report.results.map {
                AntivirusReportEntity(
                    antivirus = it.antivirus,
                    status = it.status.name,
                    malwareDescription = it.malwareDescription,
                    virusDatabaseVersion = it.virusDatabaseVersion
                )
            }
        ).also { reportRepository.save(it) }
    }

    override suspend fun findBySha256(hash: String): FileHashScanResponse? =
        reportRepository.findBySha256(hash)?.toFileHashScanResponse()

    override suspend fun findAll(): List<FileHashScanResponse> {
        return reportRepository.findAll().map {
            it.toFileHashScanResponse()
        }
    }

    override suspend fun findBy(searchWords: Iterable<String>): List<FileHashScanResponse> {
        val lowerSearchWords = searchWords.map { it.toLowerCase() }
        return reportRepository
            .findAll()
            .asSequence()
            .filter { scanReport ->
                lowerSearchWords.any { it == scanReport.md5 }

                        || lowerSearchWords.any { it == scanReport.sha1 }

                        || lowerSearchWords.any { it == scanReport.sha256 }

                        || lowerSearchWords.any { it in scanReport.date.toString() }

                        || scanReport.filename.toLowerCase().let { filename -> lowerSearchWords.any { it in filename } }

                        || scanReport.reports.joinToString(", ").toLowerCase().let { reports -> lowerSearchWords.any { it in reports } }
            }.map {
                it.toFileHashScanResponse()
            }.toList()
    }


}