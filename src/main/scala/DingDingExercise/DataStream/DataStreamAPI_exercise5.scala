package DingDingExercise.DataStream

import org.apache.flink.streaming.api.scala._

//列出及格（分数大于60）的学生、课程及成绩

object DataStreamAPI_exercise5 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val text: DataStream[String] = env.readTextFile("datas/score.txt")
        val upper60s: DataStream[String] = text.filter(line => line.split(" ")(2).toInt > 60)
        val res: DataStream[(String, String, String)] = upper60s.map(lines => {
            val words = lines.split(" ")
            (words(0), words(1), words(2))
        })
        res.print()
        env.execute()
    }
}
