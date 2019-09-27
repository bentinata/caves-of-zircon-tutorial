package org.hexworks.cavesofzircon.wfc

import com.google.gson.Gson
import org.hexworks.cavesofzircon.wfc.model.Model
import org.hexworks.cavesofzircon.wfc.model.SimpleTiledModel
import org.hexworks.cavesofzircon.wfc.model.json.Overlapping
import org.hexworks.cavesofzircon.wfc.model.json.SampleJson
import org.hexworks.cavesofzircon.wfc.model.json.Simpletiled
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.imageio.ImageIO
import kotlin.random.Random

class Main {
    companion object {
        private val gson = Gson()
        val random = Random

        @JvmStatic
        fun main(args: Array<String>) {
            val startTime = System.currentTimeMillis()
            println("start time = $startTime")

            val fileName = "samples.json"
            val file = File(fileName)
            if(file.exists()) {
                //Read the employee.json file
                val bufferedReader = BufferedReader(FileReader(fileName))

                val data = gson.fromJson(bufferedReader, SampleJson::class.java)

                var model: Model? = null
                var screenshots = 2
                var limit = 0
                var counter = 1
                var name = ""
                data.samples?.all()?.forEach { commonModel ->
                    if(commonModel is Overlapping) {
                        model = OverlappingModel(
                                bitmap = ImageIO.read(File("samples/${commonModel.name}.png")),
                                N = commonModel.n?.toInt() ?: 2,
                                width = commonModel.width?.toInt() ?: 48,
                                height = commonModel.height?.toInt() ?: 48,
                                periodicInput = commonModel.periodicInput?.toBoolean() ?: true,
                                periodicOutput = commonModel.periodic?.toBoolean() ?: false,
                                symmetry = commonModel.symmetry?.toInt() ?: 8,
                                ground = commonModel.ground?.toInt() ?: 0
                        )
                        screenshots = commonModel.screenshots?.toInt() ?: 2
                        limit = commonModel.limit?.toInt() ?: 0
                        name = commonModel.name?: "WRONG NAME 1"
                    } else if(commonModel is Simpletiled) {
                        model = SimpleTiledModel(
                                width = commonModel.width?.toInt() ?: 10,
                                height = commonModel.height?.toInt() ?: 10,
                                name = commonModel.name ?: "Knots",
                                subsetName = commonModel.subset ?: "",
                                periodic = commonModel.periodic?.toBoolean() ?: false,
                                black = commonModel.black?.toBoolean() ?: false
                        )
                        screenshots = commonModel.screenshots?.toInt() ?: 2
                        limit = commonModel.limit?.toInt() ?: 0
                        name = commonModel.name?: "WRONG NAME 2"
                    }

                    outer@for(i in 0 until screenshots) {
                        for(k in 0 until 10) {
                            val seed = random.nextInt()
                            val finished = model?.run(seed, limit)
                            if(finished == true) {
                                println("> DONE - $name")
                                val coordinates = model?.coordinates()
//                                val image = model?.graphics()
//                                val outputFile = File("out/$counter $name $i.png")
//                                ImageIO.write(image, "png", outputFile)
                                break@outer
                            }
                        }
                    }

                    counter++
                }

                println("time = ${System.currentTimeMillis() - startTime} milliseconds")
            }
        }
    }
}