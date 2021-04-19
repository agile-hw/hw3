package hw3

import chisel3._
import chisel3.tester._
import chisel3.tester.RawTester.test

import org.scalatest.FreeSpec


object LifeTestData {
    val standardRules = GameRules(3, 2, 3)

    val basic_0 =
        """--*
          |-**
          |-*-""".stripMargin

    val basic_5 =
        """---
          |---
          |---""".stripMargin

    val triangle =
        """*--
          |**-
          |***""".stripMargin

    val blinker =
        """-*-
          |-*-
          |-*-""".stripMargin

    val glider =
        """-*---
          |--*--
          |***--
          |-----
          |-----""".stripMargin
    
}


class GameOfLifeTester extends FreeSpec with ChiselScalatestTester {
    "GameOfLifeSim should be able to execute basic" in {
        val lg = new GameOfLifeSim(Grid(LifeTestData.basic_0),
                              LifeTestData.standardRules)
        println(lg)
        for (t <- 0  until 5) {
            lg.evolve()
            println(lg)
        }
        assert(lg.g.cells == Grid(LifeTestData.basic_5).cells)
    }

    "GameOfLife be able to load basic" in {
        test(new GameOfLife(3, 3, LifeTestData.standardRules)) { c =>
            val coSim = GameOfLifeTestingCoSim(c, LifeTestData.basic_0)
            coSim.loadDUT()
        }
    }

    "GameOfLife should take a few steps on triangle" in {
        test(new GameOfLife(3, 3, LifeTestData.standardRules)) { c =>
            val coSim = GameOfLifeTestingCoSim(c, LifeTestData.triangle)
            coSim.loadDUT()
            coSim.stepAndCheck(5)
        }
    }

    "GameOfLife should to execute glider" in {
        test(new GameOfLife(5, 5, LifeTestData.standardRules)) { c =>
            val coSim = GameOfLifeTestingCoSim(c, LifeTestData.glider)
            coSim.loadDUT()
            coSim.stepAndCheck(8)
        }
    }
}
