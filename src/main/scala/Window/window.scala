package Window

import DataStream_API.Source.mySource
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, SlidingProcessingTimeWindows, TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import pojo.Event

object window {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        //提取事件时间戳，定义事件时间，设置水位线
        val stream: DataStream[Event] = env.addSource(new mySource)
          .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps()
          .withTimestampAssigner(new SerializableTimestampAssigner[Event] {
              override def extractTimestamp(element: Event, recordTimestamp: Long): Long = {
                  element.time
              }
          }))
        //开窗统计每个用户5s内的点击次数
        stream.map(data => {
              (data.name, 1)
          })
          .keyBy(_._1)
          .window(TumblingEventTimeWindows.of(Time.seconds(4))).reduce((t1, t2) => (t2._1, t1._2 + t2._2)).print()
        env.execute()
    }
}

/**
         .window(TumblingProcessingTimeWindows.of(Time.days(1),Time.hours(-8)))//基于处理时间的滚动窗口，长度一天，偏移量-8h
         .window(SlidingEventTimeWindows.of(Time.hours(1),Time.minutes(10)))//基于事件时间的滑动窗口
         .window(SlidingProcessingTimeWindows.of(Time.hours(1),Time.minutes(10)))//基于处理时间的滑动窗口
 */