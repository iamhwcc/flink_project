package DingDingExercise.DataStream

import org.apache.flink.streaming.api.scala._

/*  该系DataBase课程共有多少人选修 */

object DataStreamAPI_exercise4 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val text: DataStream[String] = env.readTextFile("datas/score.txt")
        val DataBaseLines: DataStream[String] = text.filter(line => line.contains("Database"))
        val DataBaseTuple: DataStream[(String, Int)] = DataBaseLines.map(line => {
            val words: Array[String] = line.split(" ")
            (words(1),1)
        })
        DataBaseTuple.keyBy(_._1).sum(1).print()
        env.execute()
    }
}
