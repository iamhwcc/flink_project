package DingDingExercise.DataStream

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

/**
 * -> 状态编程
 * 输入数据为题目(1)中的数据，结合Flink中的状态编程，
 * 编写Flink独立应用程序，求出每个学生的最高成绩及其对应的课程名称和成绩录入时间，
 * 将最高成绩作为状态保存、更新
 */

case class score(name: String, course: String, score: Int, timestamp: Long)

object DataStreamWindow_State {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val source: DataStream[String] = env.readTextFile("datas/score.txt")
        val stream: DataStream[score] = source.map(line => {
            val strings: Array[String] = line.split(" ")
            score(strings(0).trim, strings(1).trim, strings(2).toInt, strings(3).toLong)
        })
        stream.keyBy(_.name)
            .flatMap(new MyFlatMap)
            .print("Result")
        env.execute("state For score")
    }

    //自定义一个RichFlatMapFunction
    class MyFlatMap extends RichFlatMapFunction[score, (String, Int, String, Long)] {
        lazy val lastScoreState: ValueState[(String, Int, String, Long)] = getRuntimeContext
            .getState(new ValueStateDescriptor[(String, Int, String, Long)]("last_high_score", classOf[(String, Int, String, Long)]))

        override def flatMap(value: score, out: Collector[(String, Int, String, Long)]): Unit = {
            val lastState = Option(lastScoreState.value()).getOrElse((value.name, 0, "", 0L))
            if (value.score > lastState._2) {
                lastScoreState.update((value.name, value.score, value.course, value.timestamp))
                out.collect((value.name, value.score, value.course, value.timestamp))
            }
        }
    }
}
