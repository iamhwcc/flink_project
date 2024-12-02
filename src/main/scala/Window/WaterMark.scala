package Window

import org.apache.flink.api.common.eventtime._
import org.apache.flink.streaming.api.scala._
import pojo.Event

import java.time.Duration

object WaterMark {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        env.getConfig.setAutoWatermarkInterval(500L)   //将设置水位线的周期设置为500ms（默认200ms）

        val stream: DataStream[Event] = env.fromElements(Event("Mary", "./home", 1000L),
            Event("Bob", "./about", 1020L), Event("Jack", "./test", 1200L),
            Event("Mary", "./prod?id=1", 3000L),
            Event("Mary", "./adds?password=mary01234", 1256L)
        )

        //乱序流生成水位线策略
        stream.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[Event](Duration.ofSeconds(2))//参数为最大延迟时间(最大乱序程度)
          //时间戳提取策略
          .withTimestampAssigner(new SerializableTimestampAssigner[Event]{
              override def extractTimestamp(element: Event, recordTimestamp: Long): Long = element.time
          }))


        //自定义周期性水位线
        stream.assignTimestampsAndWatermarks(new WatermarkStrategy[Event] {
            override def createTimestampAssigner(context: TimestampAssignerSupplier.Context): TimestampAssigner[Event] = {
                new SerializableTimestampAssigner[Event] {
                    override def extractTimestamp(element: Event, recordTimestamp: Long): Long = element.time
                }
            }

            override def createWatermarkGenerator(context: WatermarkGeneratorSupplier.Context): WatermarkGenerator[Event] = {
                new WatermarkGenerator[Event] {
                    //定义一个延迟时间
                    val delay = 5000L
                    //定义属性保存最大数间戳
                    var maxTimeStamp = Long.MinValue+delay+1L   //设置成这样是因为下面设置周期水位线时间要-delay-1,如果只设置为Long.MinValue就会溢出

                    //定义每个事件的时间戳，如果不是周期性水位线，那么下面的onPeriodicEmit就不需要，直接用onEvent的output发射出当前时间的时间戳即可
                    override def onEvent(event: Event, eventTimestamp: Long, output: WatermarkOutput): Unit = {
                        maxTimeStamp=math.max(event.time,maxTimeStamp)
                    }

                    //定期设置水位线
                    override def onPeriodicEmit(output: WatermarkOutput): Unit = {
                        //
                        val watermark = new Watermark(maxTimeStamp-delay-1L)
                        output.emitWatermark(watermark)
                    }
                }
            }
        })


        env.execute()
    }
}
