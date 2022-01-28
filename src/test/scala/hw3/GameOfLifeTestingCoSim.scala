package hw3

import chisel3._
import chiseltest._

class GameOfLifeTestingCoSim(sim: GameOfLifeSim, dut: GameOfLife) {
    def loadDUT():Unit = {
        dut.io.load.poke(true.B)
        dut.io.step.poke(false.B)
        for (r <- 0 until dut.nRows) {
            for (c <- 0 until dut.nCols) {
                dut.io.gridIn(r)(c).poke(sim.g.cells(r)(c).B)
            }
        }
        dut.clock.step()
        dut.io.load.poke(false.B)
        checkGridOutput()
    }

    def printGridOutput():Unit = {
        val gridToPrint = new Grid(dut.nRows, dut.nCols)
        for (r <- 0 until dut.nRows) {
            for (c <- 0 until dut.nCols) {
                gridToPrint.cells(r)(c) = dut.io.gridOut(r)(c).peek().litToBoolean
            }
        }
        println(gridToPrint)
    }

    def checkGridOutput():Unit = {
        for (r <- 0 until dut.nRows) {
            for (c <- 0 until dut.nCols) {
                dut.io.gridOut(r)(c).expect(sim.g.cells(r)(c).B)
            }
        }
    }

    def stepAndCheck(numSteps: Int=1):Unit = {
        for (step <- 0 until numSteps) {
            dut.io.load.poke(false.B)
            dut.io.step.poke(true.B)
            dut.clock.step()
            sim.evolve()
            checkGridOutput()
        }
    }
}

object GameOfLifeTestingCoSim {
    def apply(dut: GameOfLife, gridAsString: String) = {
        val rules = dut.rules
        val sim = new GameOfLifeSim(Grid(gridAsString), rules)
        assert(sim.nRows == dut.nRows)
        assert(sim.nCols == dut.nCols)
        new GameOfLifeTestingCoSim(sim, dut)
    }
}
