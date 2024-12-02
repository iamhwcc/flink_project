package DingDingExercise.DataStream

import org.apache.flink.streaming.api.scala._

/*  各门课程的平均分是多少 */

object DataStreamAPI_exercise3 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val text: DataStream[String] = env.readTextFile("datas/score.txt")
        val CourseAndScore: DataStream[(String, Int, Int)] = text.map(lines => {
            val words: Array[String] = lines.split(" ")
            (words(1), words(2).toInt, 1)
        })
        val group: KeyedStream[(String, Int, Int), String] = CourseAndScore.keyBy(_._1)
        val CourseAndScoreAndCnt: DataStream[(String, Int, Int)] = group.reduce((t1, t2) => (t1._1, t1._2 + t2._2, t1._3 + t2._3))
        CourseAndScoreAndCnt.map(t => {
            (t._1, t._2.toDouble / t._3)
        }).print()
        env.execute()
    }
}
