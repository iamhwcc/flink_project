package Window

import DataStream_API.Source.mySource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import pojo.Event

object FullWindowFunctionTest {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        //简化版有序流设置水位线和时间戳分配
        val stream: DataStream[Event] = env.addSource(new mySource).assignAscendingTimestamps(_.time)

        //测试全窗口函数,统计uv
        stream.keyBy(data => "key").window(TumblingEventTimeWindows.of(Time.minutes(1)))
          .process(new UVCountByWindow).print()
        env.execute()
    }

    //自定义实现ProcessWindowFunction
    class UVCountByWindow extends ProcessWindowFunction[Event, String, String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[Event], out: Collector[String]): Unit = {
            //使用一个set进行去重
            var userSet = Set[String]()

            //从elements提取所有数据，依次放入set去重
            elements.foreach(userSet += _.name)
            val uv = userSet.size
            //提取窗口信息，包装成String进行输出
            val windowEnd = context.window.getEnd //窗口结束时间
            val windowStart = context.window.getStart //窗口开始时间
            out.collect(s"窗口 $windowStart ~ $windowEnd, 窗口内uv为:$uv")
        }
    }
}
