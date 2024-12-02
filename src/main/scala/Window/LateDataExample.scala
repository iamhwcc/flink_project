package Window

import DataStream_API.Source.mySource
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, TumblingEventTimeWindows}
import pojo.Event
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.time.Duration

case class Result(url:String,count:Long,windowStart:Long,windowEnd:Long)

object LateDataExample {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val stream: DataStream[Event] = env.socketTextStream("192.168.142.150", 7777)
          .map(data => {
              val arr: Array[String] = data.split(",")
              Event(arr(0).trim, arr(1).trim, arr(2).trim.toLong)
          })

        //定义一个侧输出流的输出标签
        val outputTag = OutputTag[Event]("late_data")

        val res: DataStream[Result] = stream.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[Event](Duration.ofSeconds(5))
            .withTimestampAssigner(new SerializableTimestampAssigner[Event] {
                override def extractTimestamp(element: Event, recordTimestamp: Long): Long = {
                    element.time
                }
            })).keyBy(_.url)
          .window(TumblingEventTimeWindows.of(Time.seconds(10),Time.seconds(1602727715)))
          //允许窗口等待的时间
          .allowedLateness(Time.minutes(1))
          //将迟到数据输出到侧输出流
          .sideOutputLateData(outputTag)
          .aggregate(new UrlCountAgg, new UrlCountResult)

        res.print("result")

        //打印原始流
        stream.print("input")
        //打印侧输出流中的数据
        res.getSideOutput(outputTag).print("late-data")

        env.execute()
    }

    //实现增量聚合函数
    class UrlCountAgg extends AggregateFunction[Event, Long, Long] {
        override def createAccumulator(): Long = {
            0L
        }

        override def add(value: Event, accumulator: Long): Long = {
            accumulator + 1
        }

        override def getResult(accumulator: Long): Long = {
            accumulator
        }

        override def merge(a: Long, b: Long): Long = ???
    }

    //实现全窗口函数
    class UrlCountResult extends ProcessWindowFunction[Long, Result, String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[Long], out: Collector[Result]): Unit = {
            val windowStart = context.window.getStart
            val windowEnd = context.window.getEnd
            //提取url
            val url = key
            val count = elements.iterator.next()
            out.collect(Result(url, count, windowStart, windowEnd))//如果是[0,10)，那么windowStart=0，windowEnd=10
        }
    }
}
