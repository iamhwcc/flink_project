package Window

import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.common.eventtime._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import pojo.Event

import java.time.Duration

object WaterMark_Window {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        env.getConfig.setAutoWatermarkInterval(500L) //将设置水位线的周期设置为500ms（默认200ms）

        val stream: DataStream[Event] = env.socketTextStream("192.168.142.150",7777)
          .map(data => {
              val arr: Array[String] = data.split(",")
              Event(arr(0).trim,arr(1).trim,arr(2).trim.toLong)
          })

        //乱序流生成水位线策略
        stream.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[Event](Duration.ofSeconds(5)) //参数为最大延迟时间(最大乱序程度)
          //时间戳提取策略
          .withTimestampAssigner(new SerializableTimestampAssigner[Event] {
              override def extractTimestamp(element: Event, recordTimestamp: Long): Long = element.time
          }))
          //统计每个用户点击总次数
          .keyBy(_.name).window(TumblingEventTimeWindows.of(Time.seconds(10)))
          .process(new WaterMarkWindowResult)
          .print()
        env.execute()
    }

    class WaterMarkWindowResult extends ProcessWindowFunction[Event,String,String,TimeWindow] {
        override def process(user: String, context: Context, elements: Iterable[Event], out: Collector[String]): Unit = {
            val start = context.window.getStart
            val end = context.window.getEnd
            val cnt = elements.size
            //水位线信息
            val currentWaterMark = context.currentWatermark
            out.collect(s"窗口 $start ~ $end , 用户名: $user, 活跃度: $cnt, 当前水位线位于: $currentWaterMark")
        }
    }

}
