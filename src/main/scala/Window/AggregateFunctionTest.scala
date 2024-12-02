package Window

import DataStream_API.Source.mySource
import org.apache.flink.api.common.eventtime._
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import pojo.Event

object AggregateFunctionTest {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        //简化版有序流设置水位线和时间戳分配
        val stream: DataStream[Event] = env.addSource(new mySource).assignAscendingTimestamps(_.time)
        //计算 -> 所有用户的所有点击次数 / 用户个数 (PV / UV)
        stream.keyBy(data => true)
          .window( SlidingEventTimeWindows.of(Time.seconds(10),Time.seconds(2)) )
          .aggregate( new PVUV ).print()
        env.execute()
    }

    //实现自定义聚合函数
    //累加器类型 (Long,Set[String]) -> (点击次数，去重后的用户名)
    class PVUV extends AggregateFunction[Event,(Long,Set[String]),Double] {
        //初始化累加器
        override def createAccumulator(): (Long, Set[String]) = {
            (0L,Set[String]())
        }
        //累加
        override def add(in: Event, accumulator: (Long, Set[String])): (Long, Set[String]) = {
            //每来一个数据，都会进行add叠加聚合
            // 来的数据第一个值(次数)+1，第二个值(用户名)放入set
            (accumulator._1 + 1,accumulator._2 + in.name)
        }
        //返回结果
        override def getResult(accumulator: (Long, Set[String])): Double = {
            //返回最终计算结果
            // PV / UV
            accumulator._1.toDouble / accumulator._2.size   //第一个数转Double就行
        }
        //如果不是会话窗口，可以不实现
        override def merge(a: (Long, Set[String]), b: (Long, Set[String])): (Long, Set[String]) = ???
    }
}
