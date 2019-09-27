package org.hexworks.cavesofzircon.world

import org.hexworks.cavesofzircon.blocks.GameBlock
import org.hexworks.cavesofzircon.builders.GameBlockFactory
import org.hexworks.cavesofzircon.extensions.sameLevelNeighborsShuffled
import org.hexworks.cavesofzircon.wfc.Main
import org.hexworks.cavesofzircon.wfc.OverlappingModel
import org.hexworks.zircon.api.Positions
import org.hexworks.zircon.api.data.impl.Position3D
import org.hexworks.zircon.api.data.impl.Size3D
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random

class WorldBuilder(private val worldSize: Size3D) {

    private val width = worldSize.xLength
    private val depth = worldSize.yLength
    private val height = worldSize.zLength
    private var blocks: MutableMap<Position3D, GameBlock> = mutableMapOf()

    fun makeCaves(): WorldBuilder {
        return randomizeTiles()
                .smooth(8)
                .connectLevels()
    }


    fun build(visibleSize: Size3D): World = World(blocks, visibleSize, worldSize)

    fun wfc(): WorldBuilder {
        var model = OverlappingModel(
                bitmap = ImageIO.read(File("/home/bentinata/seed.png")),
                N = 3,
                width = width,
                height = worldSize.yLength,
                periodicInput = true,
                periodicOutput = false,
                symmetry = 8,
                ground = 0
        )

        do {
            val seed = Main.random.nextInt()
            val finished = model.run(seed, 0)
        } while (!finished)

        val coordinates = model.coordinates()
        forAllPositions { pos ->
            val (x, y, z) = pos
            blocks[pos] = if (coordinates[x][y] == "ff000000") {
                GameBlockFactory.wall()
            } else GameBlockFactory.floor()
        }
        return this
    }

    private fun randomizeTiles(): WorldBuilder {
        forAllPositions { pos ->
            blocks[pos] = if (Math.random() < 0.5) {
                GameBlockFactory.floor()
            } else GameBlockFactory.wall()
        }
        return this
    }

    private fun connectLevels() = also {
        (height - 1).downTo(1).forEach(::connectRegionDown)
    }

    private fun smooth(iterations: Int): WorldBuilder {
        val newBlocks = mutableMapOf<Position3D, GameBlock>()
        repeat(iterations) {
            forAllPositions { pos ->
                val (x, y, z) = pos
                var floors = 0
                var rocks = 0
                pos.sameLevelNeighborsShuffled().plus(pos).forEach { neighbor ->
                    blocks.whenPresent(neighbor) { block ->
                        if (block.isEmptyFloor) {
                            floors++
                        } else rocks++
                    }
                }
                newBlocks[Positions.create3DPosition(x, y, z)] = if (floors >= rocks) GameBlockFactory.floor() else GameBlockFactory.wall()
            }
            blocks = newBlocks
        }
        return this
    }

    private fun connectRegionDown(currentLevel: Int) {
        val posToConnect = generateRandomFloorPositionsOn(currentLevel)
                .first { pos ->
                    blocks[pos].isEmptyFloor() && blocks[pos.below()].isEmptyFloor()
                }
        blocks[posToConnect] = GameBlockFactory.stairsDown()
        blocks[posToConnect.below()] = GameBlockFactory.stairsUp()

    }


    private fun Position3D.below() = copy(z = z - 1)

    private fun generateRandomFloorPositionsOn(level: Int) = sequence {
        while (true) {
            var pos = Position3D.unknown()
            while (pos.isUnknown()) {
                val candidate = Positions.create3DPosition(
                        x = Random.nextInt(width - 1),
                        y = Random.nextInt(depth - 1),
                        z = level)
                if (blocks[candidate].isEmptyFloor()) {
                    pos = candidate
                }
            }
            yield(pos)
        }
    }

    private fun GameBlock?.isEmptyFloor(): Boolean {
        return this?.isEmptyFloor ?: false
    }


    private fun forAllPositions(fn: (Position3D) -> Unit) {
        worldSize.fetchPositions().forEach(fn)
    }

    private fun MutableMap<Position3D, GameBlock>.whenPresent(pos: Position3D, fn: (GameBlock) -> Unit) {
        this[pos]?.let(fn)
    }
}
