package DataStream_API.Source

import org.apache.flink.streaming.api.scala._
import pojo.{Event, StockPrice}
import stockSource.StockPriceSource

object fromMySource {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
//        val datas: DataStream[Event] = env.addSource(new mySource)
        val datas:DataStream[StockPrice]= env.addSource(new StockPriceSource)

        datas.print()

        env.execute()
    }

}
