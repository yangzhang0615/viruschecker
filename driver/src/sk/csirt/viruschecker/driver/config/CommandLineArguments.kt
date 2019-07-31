package sk.csirt.viruschecker.driver.config

import com.xenomachina.argparser.ArgParser

class CommandLineArguments(parser: ArgParser) {

    val antivirusTypes by parser.positionalList(
        help = "Antivirus plugins to load. Possible values are: " +
                AntivirusType.values().joinToString(separator = ", ") {
                    it.name
                },
        sizeRange = 1..AntivirusType.values().size
    ){
        AntivirusType.valueOf(this)
    }
}