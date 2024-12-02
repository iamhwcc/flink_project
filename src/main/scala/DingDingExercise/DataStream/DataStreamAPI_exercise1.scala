package DingDingExercise.DataStream

import org.apache.flink.streaming.api.scala._

/*
*    Tom同学的总成绩平均分是多少；
* */

object DataStreamAPI_exercise1 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val text: DataStream[String] = env.readTextFile("datas/score.txt")
        val TomsLine: DataStream[String] = text.filter(line => line.contains("Tom"))
        val TomArray: DataStream[Array[String]] = TomsLine.map(line => line.split(" "))
        val tuple: DataStream[(String, Int)] = TomArray.map(word => (word(0), word(2).toInt))
        val res: KeyedStream[(Int, Int), String] = tuple.map(tuple => (tuple._2, 1)).keyBy(_ => "Tom")
        val ans: DataStream[(Int, Int)] = res.reduce((v1, v2) => (v1._1 + v2._1, v1._2 + v2._2))
        ans.map(t => t._1.toDouble/t._2).print()
        env.execute()
    }
}
