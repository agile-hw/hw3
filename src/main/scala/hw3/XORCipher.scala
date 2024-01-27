package hw3

import chisel3._
import chisel3.util._


class XORCipherCmds extends Bundle {
    val clear = Input(Bool())
    val load  = Input(Bool())
    val read  = Input(Bool())
}


object CipherState extends ChiselEnum {
    val clearing, empty, loading, encrypted, reading = Value
}


class XORCipherIO(width: Int) extends Bundle {
    val in    = Input(UInt(width.W))
    val key   = Flipped(Valid(UInt(width.W)))
    val cmds  = Input(new XORCipherCmds)
    val out   = Valid(UInt(width.W))
    val state = Output(CipherState())
}


class XORCipher(width: Int, numWords: Int) extends Module {
    val io = IO(new XORCipherIO(width))

    // YOUR CODE HERE

}
