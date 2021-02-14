package io.nodle.dtn.bpv7.extensions

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.utils.readInt
import io.nodle.dtn.utils.readStruct
import java.io.OutputStream

/**
 * @author Lucien Loiseau on 14/02/21.
 */
data class HopCountBlockData(var limit: Int, var count : Int) : ExtensionBlockData

fun hopCountBlockData(limit: Int, count : Int) : CanonicalBlock = CanonicalBlock(
    blockType = BlockType.HopCountBlock.code,
    data = HopCountBlockData(limit, count)
)

@Throws(CborEncodingException::class)
fun HopCountBlockData.cborMarshalData(out: OutputStream) {
    CBORFactory().createGenerator(out).use {
        it.writeStartArray(2)
        it.writeNumber(limit)
        it.writeNumber(count)
        it.writeEndArray()
    }
}

@Throws(CborParsingException::class)
fun CBORParser.readHopCountBlockData() : HopCountBlockData {
    return readStruct {
        HopCountBlockData(readInt(), readInt())
    }
}