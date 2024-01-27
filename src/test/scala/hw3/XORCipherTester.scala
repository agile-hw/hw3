package hw3

import chisel3._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import org.scalatest.flatspec.AnyFlatSpec

class XORCipherTester extends AnyFlatSpec with ChiselScalatestTester {
  val key = 0x44
  val garb = 0x22
  val width = 8
  val numWords = 2


  behavior of "XORCipher"
  it should "get started" in {
    test(new XORCipher(width, numWords)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // clearing -> empty
      dut.io.state.expect(CipherState.clearing)
      dut.io.in.poke(garb.U)
      dut.io.key.bits.poke(garb.U)
      dut.io.key.valid.poke(false.B)
      dut.io.cmds.poke((new XORCipherCmds).Lit(_.clear -> false.B, _.load -> false.B, _.read -> false.B))
      dut.io.out.bits.expect(0.U)
      dut.io.out.valid.expect(false.B)
      for (c <- 0 until numWords) {
        dut.io.in.poke(c.U)
        dut.io.key.bits.poke(key.U)
        dut.io.key.valid.poke(false.B)
        dut.io.out.bits.expect(0.U)
        dut.io.out.valid.expect(false.B)
        dut.clock.step()
        if (c < numWords - 1)
          dut.io.state.expect(CipherState.clearing)
        else
          dut.io.state.expect(CipherState.empty)
      }

      // empty -> loading -> encrypted
      dut.io.state.expect(CipherState.empty)
      dut.io.cmds.poke((new XORCipherCmds).Lit(_.clear -> false.B, _.load -> true.B, _.read -> false.B))
      for (c <- 0 until numWords) {
        dut.io.in.poke(c.U)
        dut.io.key.bits.poke(key.U)
        dut.io.key.valid.poke(true.B)
        dut.io.out.bits.expect(0.U)
        dut.io.out.valid.expect(false.B)
        dut.clock.step()
        if (c < numWords - 1)
          dut.io.state.expect(CipherState.loading)
        else
          dut.io.state.expect(CipherState.encrypted)
      }
      
      ???
    }
  }
}
