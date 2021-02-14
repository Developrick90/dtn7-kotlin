package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.security.addEd25519Signature
import io.nodle.dtn.crypto.toEd25519PrivateKey
import io.nodle.dtn.utils.hexToBa
import picocli.CommandLine
import java.net.URI
import java.util.concurrent.Callable

/**
 * @author Lucien Loiseau on 13/02/21.
 */
@CommandLine.Command(
    name = "create",
    mixinStandardHelpOptions = true,
    description = ["", "create a bundle"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""]
)
class BpCreate : Callable<Void> {
    @CommandLine.Option(names = ["-d", "--destination"], description = ["destination eid "])
    private var destination = "dtn://destination/"

    @CommandLine.Option(names = ["-s", "--source"], description = ["destination eid "])
    private var source = "dtn://source/"

    @CommandLine.Option(names = ["-r", "--report"], description = ["report-to eid"])
    private var report = "dtn://report/"

    @CommandLine.Option(names = ["-l", "--lifetime"], description = ["lifetime of the bundle"])
    private var lifetime: Long = 0

    @CommandLine.Option(names = ["--sign"], description = ["sign blocks (require --key to be set)"])
    private var targets: List<Int> = ArrayList()

    @CommandLine.Option(names = ["--key"], description = ["ed25519 key (in hex prefixed with 0x)"])
    private var hexKey: String = ""

    @CommandLine.Option(names = ["--crc-16"], description = ["use crc-16"])
    private var crc16 = false

    @CommandLine.Option(names = ["--crc-32"], description = ["use crc-32"])
    private var crc32 = false

    override fun call(): Void? {
        val crc = if (crc16) {
            CRCType.CRC16
        } else {
            CRCType.CRC32
        }

        val bundle = PrimaryBlock()
            .destination(URI.create(destination))
            .source(URI.create(source))
            .reportTo(URI.create(report))
            .crcType(crc)
            .lifetime(lifetime)
            .makeBundle()
            .addBlock(payloadBlock(System.`in`.readBytes()).crcType(crc))

        if (targets.isNotEmpty()) {
            if(hexKey == "") {
                println("the --key paraneter must be supplied with --sign")
            }

            try {
                val key = hexKey.hexToBa().toEd25519PrivateKey()
                bundle.addEd25519Signature(key, targets)
            } catch (e: Exception) {
                println("supplied key is not an ed25519 private key")
            }
        }

        bundle.cborMarshal(System.`out`)

        return null
    }
}