package DingDingExercise.DataStream

import org.apache.flink.streaming.api.scala._

/*
    Mary选了多少门课；
* */

object DataStreamAPI_exercise2 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val text: DataStream[String] = env.readTextFile("datas/score.txt")
        val MaryLines: DataStream[String] = text.filter(line => line.contains("Mary"))
        val MaryCourses: DataStream[(String, Int)] = MaryLines.map(line => {
            val words = line.split(" ")
            (words(1),1)
        })
        val res: DataStream[(String, Int)] = MaryCourses.keyBy(_ => "Mary")
            .reduce((v1, v2) => ("Mary", v1._2 + v2._2))
        res.map(_._2).print()
        env.execute()
    }
}
