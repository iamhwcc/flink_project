package DingDingExercise.DataStream

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

import java.time.Duration


/**
 * 5)窗口与水位线基本操作
 * 输入数据为题目(1)中的数据，结合Flink中窗口与水位线相关知识，编写Flink应用程序，
 * 每15s统计一次每个窗口内每个学生的最高成绩及其对应的课程名称和成绩录入时间。
 * 观察输出结果，理解窗口与水位线的处理流程(使用Kafka作为输入输出源，设置延迟时间为3s,且能够处理迟到30s的数据)。
 * */

object DataStreamWindow_exercise2 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val stream: DataStream[score] = env.readTextFile("datas/scorewithlate.txt")
            .filter(line => !line.startsWith("-"))
            .map(line => {
                val strings: Array[String] = line.split(" ")
                score(strings(0).trim, strings(1).trim, strings(2).toInt, strings(3).toLong)
            })
        stream.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[score](Duration.ofSeconds(3))
                .withTimestampAssigner(new SerializableTimestampAssigner[score] {
                    override def extractTimestamp(element: score, recordTimestamp: Long): Long = {
                        element.timestamp * 1000
                    }
                }))
            .keyBy(_.name)
            .window(TumblingEventTimeWindows.of(Time.seconds(10)))
            .allowedLateness(Time.seconds(30))
            .aggregate(new AGG2)
            .print("Result")
        env.execute("Window + WaterMark")
    }

    class AGG2 extends AggregateFunction[score, score, score] {
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
