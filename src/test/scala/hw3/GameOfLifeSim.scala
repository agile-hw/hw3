package hw3

import scala.collection.mutable.ArrayBuffer

class Grid(val nRows: Int, val nCols: Int) {
    val cells = ArrayBuffer.fill(nRows, nCols)(false)

    // YOUR CODE HERE

    override def toString(): String = {
        val sb = new StringBuilder()
        for (r <- 0 until nRows) {
            for (c <- 0 until nCols) {
                sb += (if (cells(r)(c)) '*' else '-')
            }
            sb += '\n'
        }
        sb.result()
    }
}


object Grid {
    def apply(gridAsString: String) = {
        val split = gridAsString.split('\n')
        val nRows = split.size
        val nCols = split.head.length
        val g = new Grid(nRows, nCols)
        for (r <- 0 until nRows) {
            for (c <- 0 until nCols) {
                g.cells(r)(c) = split(r)(c) == '*'
            }
        }
        g
    }
}


class GameOfLifeSim(initialGrid: Grid, rules: GameRules) {
    var g = initialGrid

    def nextGrid(): Grid = {

        // YOUR CODE HERE
        ???

    }

    def evolve() = { g = nextGrid() }

    override def toString(): String = g.toString
    val nRows = g.nRows
    val nCols = g.nCols
}
