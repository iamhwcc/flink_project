package FinalExercise

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import pojo.{Event, StockPrice}
import stockSource.StockPriceSource

import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}
import java.text.SimpleDateFormat
import java.util.{Properties, TimeZone}

//StockPrice(stockId: String, timeStamp: Long, price: Double)


object HandleDataToMySQL {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val stream: DataStream[StockPrice] = env.addSource(new StockPriceSource)
            .assignAscendingTimestamps(_.timeStamp)
        //按每只股票分区，开窗
        val OHLC_result: DataStream[OUT] = stream.keyBy(_.stockId)
            .window(TumblingEventTimeWindows.of(Time.minutes(1)))
            .process(new MyOHLC)
        OHLC_result.print("插入MySQL的数据")
        OHLC_result.addSink(new mySink())
        env.execute()
    }

    //定义输出类型
    case class OUT(time: String, stockID: String, O: Double, H: Double, L: Double, C: Double)

    //5分钟为周期，计算该周期内的开盘价、最高价、最低价、收盘价
    //O开盘价 H最高价 L最低价 C收盘价
    class MyOHLC extends ProcessWindowFunction[StockPrice, OUT, String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[StockPrice], out: Collector[OUT]): Unit = {
            val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
            val wine: String = dateFormat.format(context.window.getEnd)

            val list1: List[StockPrice] = elements.toList.sortBy(_.timeStamp)
            val openPrice: Double = list1.head.price.formatted("%.2f").toDouble
            val closePrice: Double = list1.last.price.formatted("%.2f").toDouble
            val list2: List[StockPrice] = elements.toList.sortBy(_.price)
            val HighestPrice: Double = list2.last.price.formatted("%.2f").toDouble
            val LowestPrice: Double = list2.head.price.formatted("%.2f").toDouble
            out.collect(OUT(wine, key, openPrice, HighestPrice, LowestPrice, closePrice))
        }
    }

    class mySink extends RichSinkFunction[OUT] {
        var conn: Connection = _
        var insertstream: PreparedStatement = _

        override def open(parameters: Configuration): Unit = {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Flink_KLine?useSSL=false",
                "root", "123456")
            insertstream = conn.prepareStatement("INSERT INTO myStock (stockTime, stockId, openPrice, highestPrice, lowestPrice, closePrice) " +
                "VALUES (?,?,?,?,?,?)")
        }
        //处理逻辑
        override def invoke(u: OUT): Unit = {
            insertstream.setTimestamp(1, Timestamp.valueOf(u.time))
            insertstream.setString(2, u.stockID)
            insertstream.setDouble(3, u.O)
            insertstream.setDouble(4, u.H)
            insertstream.setDouble(5, u.L)
            insertstream.setDouble(6, u.C)
            insertstream.execute()
        }
        //关闭连接
        override def close(): Unit = {
            insertstream.close()
            conn.close()
        }
    }
}
