package hw3

import chisel3._
import chisel3.util._

case class GameRules(neighsToSpawn: Int, minToSurvive: Int, maxToSurvive: Int)

class GameOfLifeIO(nRows: Int, nCols: Int) extends Bundle {
    val load = Input(Bool())
    val step = Input(Bool())
    val gridIn = Input(Vec(nRows, Vec(nCols, Bool())))
    val gridOut = Output(Vec(nRows, Vec(nCols, Bool())))
}

class GameOfLife(val nRows: Int, val nCols: Int, val rules: GameRules) extends Module {
    val io = IO(new GameOfLifeIO(nRows, nCols))

    // instantiate grid of registers & connect to output
    val cells = Seq.fill(nRows,nCols)(RegInit(false.B))
    for (r <- 0 until nRows) {
        for (c <- 0 until nCols) {
            io.gridOut(r)(c) := cells(r)(c)
        }
    }

    // YOUR CODE HERE

}
