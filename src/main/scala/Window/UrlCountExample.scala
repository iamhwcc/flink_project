package Window

import DataStream_API.Source.mySource
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, TumblingEventTimeWindows}
import pojo.Event
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

case class ResultForUrl(url:String,count:Long,windowStart:Long,windowEnd:Long)

object UrlCountExample {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        //简化版有序流设置水位线和时间戳分配
        val stream: DataStream[Event] = env.addSource(new mySource).assignAscendingTimestamps(_.time)

        //增量聚合函数+全窗口函数包装统计信息
        stream.keyBy(_.url)
          .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
          .aggregate(new UrlCountAgg, new UrlCountResult)
          .print()
        env.execute()
    }
    //实现增量聚合函数
    class UrlCountAgg extends AggregateFunction[Event,Long,Long]{
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
    class UrlCountResult extends ProcessWindowFunction[Long,ResultForUrl,String,TimeWindow]{
        override def process(key: String, context: Context, elements: Iterable[Long], out: Collector[ResultForUrl]): Unit = {
            val windowStart = context.window.getStart
            val windowEnd = context.window.getEnd
            //提取url
            val url = key
            val count = elements.iterator.next()
            out.collect(ResultForUrl(url,count,windowStart,windowEnd))
        }
    }
}
