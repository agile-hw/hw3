package hw3

import chisel3._
import chisel3.tester._
import chisel3.tester.RawTester.test
import chisel3.experimental.BundleLiterals._
import org.scalatest.FreeSpec

import scala.collection.mutable.ArrayBuffer

// test vectors here https://datatracker.ietf.org/doc/rfc7539/

object ChaChaModelBehavior {
    def testROTL: Boolean = {
        // check no wrap around
        for (b <- 0 until 32) {
            val out = ChaChaModel.ROTL(1, b)
            val exp = BigInt(1) << b
            // println(s"a: 1, b: $b, out: $out, exp: $exp")
            assert(out == exp)
        } 

        // Check wrap around
        for (b <- 32 until 64) {
            val out = ChaChaModel.ROTL(1, b)
            val exp = BigInt(1) << (b - 32)
            // println(s"a: 1, b: $b, out: $out, exp: $exp")
            assert(out == exp)
        } 
        assert(ChaChaModel.ROTL(1, 32) == 1)
        assert(ChaChaModel.ROTL(1234234, 32) == 1234234)
        assert(ChaChaModel.ROTL(1234234, 33) == 1234234 << 1)
        true
    }

    def testQR: Boolean = {
        val a = BigInt("11111111", 16)
        val b = BigInt("01020304", 16)
        val c = BigInt("9b8d6f43", 16)
        val d = BigInt("01234567", 16)
        val in = RoundData(a, b, c, d)
        // println(s"in: ${in.toHex} == $in")

        val expA = BigInt("ea2a92f4", 16)        
        val expB = BigInt("cb1cf8ce", 16)
        val expC = BigInt("4581472e", 16)
        val expD = BigInt("5881c4bb", 16)
        val out = RoundData(expA, expB, expC, expD)
        val got = ChaChaModel.QR(in)
        // println(s"got: ${got.toHex}, exp: ${out.toHex}")
        assert(got.a == out.a)
        assert(got.b == out.b)
        assert(got.c == out.c)
        assert(got.d == out.d)
        true
    }

    def testQR2: Boolean = {
        val testVec = Seq(
            BigInt("879531e0", 16), BigInt("c5ecf37d", 16), BigInt("516461b1", 16), BigInt("c9a62f8a", 16),
            BigInt("44c20ef3", 16), BigInt("3390af7f", 16), BigInt("d9fc690b", 16), BigInt("2a5f714c", 16),
            BigInt("53372767", 16), BigInt("b00a5631", 16), BigInt("974c541a", 16), BigInt("359e9963", 16),
            BigInt("5c971061", 16), BigInt("3d631689", 16), BigInt("2098d9d6", 16), BigInt("91dbd320", 16))
        val in = ChaChaModel.testVecToIn(testVec)

        val mat: collection.mutable.ArrayBuffer[BigInt] = collection.mutable.ArrayBuffer(in.mat: _*)
        val r = ChaChaModel.QR(RoundData(mat(2), mat(7), mat(8), mat(13)))
        mat(2) = r.a ; mat(7) = r.b ; mat(8) = r.c ; mat(13) = r.d

        val exp = Seq(
            BigInt("879531e0", 16), BigInt("c5ecf37d", 16), BigInt("bdb886dc", 16), BigInt("c9a62f8a", 16),
            BigInt("44c20ef3", 16), BigInt("3390af7f", 16), BigInt("d9fc690b", 16), BigInt("cfacafd2", 16),
            BigInt("e46bea80", 16), BigInt("b00a5631", 16), BigInt("974c541a", 16), BigInt("359e9963", 16),
            BigInt("5c971061", 16), BigInt("ccc07c79", 16), BigInt("2098d9d6", 16), BigInt("91dbd320", 16))

        assert (mat.toSeq == exp)

        true
    }
    

    def testChaChaIn: Boolean = {
        val testVec = Seq(
            BigInt("879531e0", 16), BigInt("c5ecf37d", 16), BigInt("516461b1", 16), BigInt("c9a62f8a", 16),
            BigInt("44c20ef3", 16), BigInt("3390af7f", 16), BigInt("d9fc690b", 16), BigInt("2a5f714c", 16),
            BigInt("53372767", 16), BigInt("b00a5631", 16), BigInt("974c541a", 16), BigInt("359e9963", 16),
            BigInt("5c971061", 16), BigInt("3d631689", 16), BigInt("2098d9d6", 16), BigInt("91dbd320", 16))
        val key   = BigInt("44c20ef33390af7fd9fc690b2a5f714c53372767b00a5631974c541a359e9963", 16)
        val nonce = BigInt("3d6316892098d9d691dbd320", 16)
        val block = BigInt("5c971061", 16)
        val const = BigInt("879531e0c5ecf37d516461b1c9a62f8a", 16)
        val expConst = for (i <- 0 to 3) yield testVec(i)
        val expKey = for (i <- 4 to 11) yield testVec(i)
        val expNonce = for (i <- 13 to 15) yield testVec(i)
        val expBlock = testVec(12)
        val c = ChaChaIn(key, nonce, block, const)
        val ks = c.keyBits
        val ns = c.nonceBits
        val cs = c.constBits
        assert(cs == expConst)
        assert(ks == expKey)
        assert(ns == expNonce)
        assert(c.blockCnt == expBlock)
        assert(c.mat == testVec)
        true
    }

    def testChaCha: Boolean = {
        val testVecIn = Seq(
            BigInt("61707865", 16), BigInt("3320646e", 16), BigInt("79622d32", 16), BigInt("6b206574", 16),
            BigInt("03020100", 16), BigInt("07060504", 16), BigInt("0b0a0908", 16), BigInt("0f0e0d0c", 16),
            BigInt("13121110", 16), BigInt("17161514", 16), BigInt("1b1a1918", 16), BigInt("1f1e1d1c", 16),
            BigInt("00000001", 16), BigInt("09000000", 16), BigInt("4a000000", 16), BigInt("00000000", 16))
        val testVecOut = Seq(
            BigInt("e4e7f110", 16), BigInt("15593bd1", 16), BigInt("1fdd0f50", 16), BigInt("c47120a3", 16),
            BigInt("c7f4d1c7", 16), BigInt("0368c033", 16), BigInt("9aaa2204", 16), BigInt("4e6cd4c3", 16),
            BigInt("466482d2", 16), BigInt("09aa9f07", 16), BigInt("05d7c214", 16), BigInt("a2028bd9", 16),
            BigInt("d19c12b5", 16), BigInt("b94e16de", 16), BigInt("e883d0cb", 16), BigInt("4e3c50a2", 16))

        val in = ChaChaModel.testVecToIn(testVecIn)
        val exp = ChaChaModel.testVecToIn(testVecOut)
        val expInt = ChaChaModel.wordsToBigInt(exp.mat)
        println(s"in: ${in.toHex}")

        // Generate the keystream
        val got = ChaChaModel.keyGen(in)
        // println(s"got: $got, exp: $expInt")

        assert(got == expInt)
        true
    }
}

// Chisel level tests
object ChaChaBehavior {
    /**
      * Top level test, ensure that your ROTL() and QR() Chisel methods are working
      * before attempting this.
      *
      * @return
      */
    def testChaCha: Boolean = {
        val testVecIn = Seq(
            BigInt("61707865", 16), BigInt("3320646e", 16), BigInt("79622d32", 16), BigInt("6b206574", 16),
            BigInt("03020100", 16), BigInt("07060504", 16), BigInt("0b0a0908", 16), BigInt("0f0e0d0c", 16),
            BigInt("13121110", 16), BigInt("17161514", 16), BigInt("1b1a1918", 16), BigInt("1f1e1d1c", 16),
            BigInt("00000001", 16), BigInt("09000000", 16), BigInt("4a000000", 16), BigInt("00000000", 16))

        val testVecOut = Seq(
            BigInt("e4e7f110", 16), BigInt("15593bd1", 16), BigInt("1fdd0f50", 16), BigInt("c47120a3", 16),
            BigInt("c7f4d1c7", 16), BigInt("0368c033", 16), BigInt("9aaa2204", 16), BigInt("4e6cd4c3", 16),
            BigInt("466482d2", 16), BigInt("09aa9f07", 16), BigInt("05d7c214", 16), BigInt("a2028bd9", 16),
            BigInt("d19c12b5", 16), BigInt("b94e16de", 16), BigInt("e883d0cb", 16), BigInt("4e3c50a2", 16))

        // expected output
        val exp: ChaChaIn = ChaChaModel.testVecToIn(testVecOut)
        val expInt = ChaChaModel.wordsToBigInt(exp.mat)

        test(new ChaCha) { c => 
            // Convert input test vec to more convenenient ChaChaIn
            val in: ChaChaIn = ChaChaModel.testVecToIn(testVecIn)

            // Poke in the test vec
            c.io.in.bits.key.poke(in.key.U)
            c.io.in.bits.nonce.poke(in.nonce.U)
            c.io.in.bits.blockCnt.poke(in.blockCnt.U)
            c.io.in.valid.poke(true.B)

            // Write the valid input to regs
            c.clock.step()

            // keystream generation begins after valid signal goes low
            c.io.in.valid.poke(false.B)

            // 10 col rounds + 10 diag rounds
            c.clock.step(20)

            // val out = c.io.keyStream.bits.peek().litValue()
            // println(s"got: $out, exp: $expInt")
            c.io.keyStream.bits.expect(expInt.U)
            c.io.keyStream.valid.expect(true.B)

        }
        true
    }

    /**
      * Tests the Chisel ROTL() implemenation.
      * @return
      */
    def testROTL: Boolean = {
        for (b <- 0 until 32) {
            test(new Module {
                val io = IO(new Bundle {
                    val in = Input(UInt(32.W))
                    val out = Output(UInt(32.W))
                })
                io.out := ChaCha.ROTL(io.in, b)
            }) { c => 
                for (a <- 0 until 31) {
                    val exp = ChaChaModel.ROTL(1 << a, b)
                    c.io.in.poke((1 << a).U)
                    c.io.out.expect(exp.U)
                }
            }
        }

        true
    }

    /**
      * Tests the correctness of Chisel QR() using ChaChaIO.
      *
      * @return
      */
    def testQR: Boolean = {
        val testVecIn = Seq(
            BigInt("61707865", 16), BigInt("3320646e", 16), BigInt("79622d32", 16), BigInt("6b206574", 16),
            BigInt("44c20ef3", 16), BigInt("3390af7f", 16), BigInt("d9fc690b", 16), BigInt("2a5f714c", 16),
            BigInt("53372767", 16), BigInt("b00a5631", 16), BigInt("974c541a", 16), BigInt("359e9963", 16),
            BigInt("5c971061", 16), BigInt("3d631689", 16), BigInt("2098d9d6", 16), BigInt("91dbd320", 16))

        val testVecOut = Seq(
            BigInt("61707865", 16), BigInt("3320646e", 16), BigInt("bf35fde5", 16), BigInt("6b206574", 16),
            BigInt("44c20ef3", 16), BigInt("3390af7f", 16), BigInt("d9fc690b", 16), BigInt("f32913c2", 16),
            BigInt("9e920d40", 16), BigInt("b00a5631", 16), BigInt("974c541a", 16), BigInt("359e9963", 16),
            BigInt("5c971061", 16), BigInt("c2634737", 16), BigInt("2098d9d6", 16), BigInt("91dbd320", 16))

        val in = ChaChaModel.testVecToIn(testVecIn)
        println(s"in: $in")
        val exp = ChaChaModel.testVecToIn(testVecOut)
        val expInt = ChaChaModel.wordsToBigInt(exp.mat)

        val testIdxs = Seq(2, 7, 8, 13)

        test(new Module {
            val io = IO(new ChaChaIO)
            io.keyStream.noenq()
            io.in.ready := true.B
            // ChaCha.pHex(io.in.bits.toSeq)

            val regs = Seq.fill(16)(Reg(UInt(32.W)))
            when (io.in.fire) {
                for (i <- 0 until 16) {
                    regs(i) := io.in.bits.toSeq(i)
                }
            } .otherwise {
                val mat = ArrayBuffer[UInt]()
                // read 4 UInts from regs
                for (i <- testIdxs) {
                    mat += regs(i)
                }
                // Perform QR on them
                val res = ChaCha.QR(mat)

                // Write them back to regs
                for (i <- 0 until 4) {
                    regs(testIdxs(i)) := res(i)
                }
                // output is 512b, so collapse our Seq[UInt] to one UInt
                io.keyStream.bits := ChaCha.collapseMat(regs)
            }
            ChaCha.pHex(regs)

        }) { c => 
            c.io.in.bits.key.poke(in.key.U)
            c.io.in.bits.nonce.poke(in.nonce.U)
            c.io.in.bits.blockCnt.poke(in.blockCnt.U)
            c.io.in.valid.poke(true.B)
            c.clock.step(1) 
            c.io.in.valid.poke(false.B)
            c.clock.step(1) 
            val out = c.io.keyStream.bits.peek().litValue()
            println(s"got: $out, exp: $expInt")
            c.io.keyStream.bits.expect(expInt.U)

            c.clock.step(1) // for printf

        }

        true
    }

    /**
      * Tests correctness of Chisel QR() function without ChaChaIO.
      *
      * @return
      */
    def testQR2: Boolean = {
        test(new Module {
            val io = IO(new Bundle {
                val a = Output(UInt(32.W))
                val b = Output(UInt(32.W))
                val c = Output(UInt(32.W))
                val d = Output(UInt(32.W))
            })
            val in = Seq(BigInt("79622d32", 16), BigInt("2a5f714c", 16), 
                         BigInt("53372767", 16), BigInt("3d631689", 16))
            val regs = for (c <- in) yield RegInit(c.U(32.W))
            val out = ChaCha.QR(regs)
            io.a := out(0)
            io.b := out(1)
            io.c := out(2)
            io.d := out(3)
            printf(p"a: ${Hexadecimal(in(0).U)}, b: ${Hexadecimal(in(1).U)}, c: ${Hexadecimal(in(2).U)}, d: ${Hexadecimal(in(3).U)}, " +
                   p"a2: ${Hexadecimal(io.a)}, b2: ${Hexadecimal(io.b)}, c2: ${Hexadecimal(io.c)}, d2: ${Hexadecimal(io.d)}\n")
        }) { c => 
                c.clock.step(1)
                val exp = ChaChaModel.QR(RoundData(BigInt("79622d32", 16), BigInt("2a5f714c", 16), 
                                    BigInt("53372767", 16), BigInt("3d631689", 16)))
                println(s"exp: ${exp.toHex}")
                c.io.a.expect(exp.a.U)
                c.io.b.expect(exp.b.U)
                c.io.c.expect(exp.c.U)
                c.io.d.expect(exp.d.U)
        }
        true
    }

    /**
      * Another test for Chisel QR() function.
      *
      * @return
      */
    def testQR3: Boolean = {
        test(new Module {
            val io = IO(new Bundle {
                val a = Output(UInt(32.W))
                val b = Output(UInt(32.W))
                val c = Output(UInt(32.W))
                val d = Output(UInt(32.W))
            })
            val in = Seq(BigInt("11111111", 16), BigInt("01020304", 16), 
                         BigInt("9b8d6f43", 16), BigInt("01234567", 16))
            val regs = for (c <- in) yield RegInit(c.U(32.W))
            val out = ChaCha.QR(regs)
            io.a := out(0)
            io.b := out(1)
            io.c := out(2)
            io.d := out(3)
            printf(p"a: ${Hexadecimal(in(0).U)}, b: ${Hexadecimal(in(1).U)}, c: ${Hexadecimal(in(2).U)}, d: ${Hexadecimal(in(3).U)}, " +
                   p"a2: ${Hexadecimal(io.a)}, b2: ${Hexadecimal(io.b)}, c2: ${Hexadecimal(io.c)}, d2: ${Hexadecimal(io.d)}\n")
        }) { c => 
                c.clock.step(1)
                c.io.a.expect(BigInt("ea2a92f4", 16).U)        
                c.io.b.expect(BigInt("cb1cf8ce", 16).U)
                c.io.c.expect(BigInt("4581472e", 16).U)
                c.io.d.expect(BigInt("5881c4bb", 16).U)
        }
        true
    }
}

class ChaChaModelTester extends FreeSpec with ChiselScalatestTester {
    "Software ROTL should work" in {
        assert(ChaChaModelBehavior.testROTL)
    }

    "Software QR should pass test vector" in {
        assert(ChaChaModelBehavior.testQR)
    }

    "Software QR should pass test vector2" in {
        assert(ChaChaModelBehavior.testQR2)
    }

    "Software ChaChaIn should split bitstring into 32b chunks" in {
        assert(ChaChaModelBehavior.testChaChaIn)
    }

    "Software ChaCha keyGen should pass the test vector" in {
        assert(ChaChaModelBehavior.testChaCha)
    }
}

class ChaChaTester extends FreeSpec with ChiselScalatestTester {

    "Hardware ChaCha ROTL should work" in {
        assert(ChaChaBehavior.testROTL)
    }

    "Hardware ChaCha QR should work" in {
        assert(ChaChaBehavior.testQR)
    }

    "Hardware ChaCha QR2 should work" in {
        assert(ChaChaBehavior.testQR2)
    }

    "Hardware ChaCha QR3 should work" in {
        assert(ChaChaBehavior.testQR3)
    }

    "Hardware ChaCha should pass the test vector" in {
        assert(ChaChaBehavior.testChaCha)
    }
}
