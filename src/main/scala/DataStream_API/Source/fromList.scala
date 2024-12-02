package DataStream_API.Source

import org.apache.flink.streaming.api.scala._
import pojo.Event

object fromList {
    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        //创建集合
        //        val info = List(Event("stalwart", "http://pornhub.com", 1000L),
        //            Event("raider", "./home", 44L),
        //            Event("lucy", "https://www.qq.com", 324L))
        //        val stream = env.fromCollection(info)

        //不创建集合
        val stream: DataStream[Int] = env.fromElements(1, 1, 1, 15, 5, 6, 94, 94, 9)
        stream.print()
        env.execute()
    }
}
