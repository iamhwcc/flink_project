package stockSource

import java.util.{Calendar, TimeZone}
import org.apache.flink.streaming.api.functions.source.{ParallelSourceFunction, RichSourceFunction}
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import pojo.StockPrice

import scala.util.Random

class StockPriceSource extends ParallelSourceFunction[StockPrice] {
    val rand = new Random()
    var isRunning: Boolean = true
    //初始化股票价格
    var priceList: List[Double] = List(10.0d, 20.0d, 30.0d, 40.0d)
    var stockId = 0
    var curPrice = 0.0d

    override def run(srcCtx: SourceContext[StockPrice]): Unit = {
        while (isRunning) {
            //每次从列表中随机选择一只股票
            stockId = rand.nextInt(priceList.size)
            val curPrice = priceList(stockId) + rand.nextGaussian() * 0.1
            priceList = priceList.updated(stockId, curPrice)
            val curTime = Calendar.getInstance.getTimeInMillis

            //将数据源收集写入SourceContext
            srcCtx.collect(StockPrice("stock_" + stockId.toString, curTime, curPrice))
            Thread.sleep(rand.nextInt(1000))
        }
    }

    override def cancel(): Unit = {
        isRunning = false
    }
}
