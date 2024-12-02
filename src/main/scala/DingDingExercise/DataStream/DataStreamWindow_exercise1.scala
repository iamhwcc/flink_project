package DingDingExercise.DataStream

import org.apache.flink.api.common.functions.{AggregateFunction, ReduceFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time


/**
 * -> 窗口内数据统计
 * 输入数据为题目(1)中的数据，结合Flink中窗口相关知识，
 * 编写Flink应用程序，每 10s 统计一次每个窗口内 每个学生的最高成绩 及其 对应的课程名称 和 成绩录入时间。
 */

object DataStreamWindow_exercise1 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val source: DataStream[String] = env.readTextFile("datas/scorewithlate.txt")
            .filter(line => !line.startsWith("-"))
        val stream: DataStream[score] = source.map(line => {
            val strings: Array[String] = line.split(" ")
            score(strings(0).trim, strings(1).trim, strings(2).toInt, strings(3).toLong)
        }).assignAscendingTimestamps(_.timestamp * 1000)

        stream.keyBy(_.name)
            .window(TumblingEventTimeWindows.of(Time.seconds(10)))
            .aggregate(new AGG1)

            .print("Result")
        env.execute("Window1")
    }

    class AGG1 extends AggregateFunction[score, score, score] {
        override def createAccumulator(): score = {
            score("", "", 0, 0L)
        }

        override def add(value: score, accumulator: score): score = {
            if (value.score > accumulator.score) {
                score(value.name, value.course, value.score, value.timestamp)
            } else {
                accumulator
            }
        }

        override def getResult(accumulator: score): score = {
            accumulator
        }

        override def merge(a: score, b: score): score = ???
    }
}



