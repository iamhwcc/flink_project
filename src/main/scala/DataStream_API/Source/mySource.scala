package DataStream_API.Source

import org.apache.flink.streaming.api.functions.source._
import pojo.Event

import java.util.Calendar
import scala.util.Random

class mySource extends ParallelSourceFunction[Event] {
    //判断是否要中止数据源的标志位
    private var isRunning = true

    override def run(ctx: SourceFunction.SourceContext[Event]): Unit = {
        //随机数生成
        val random = new Random()

        val users: List[String] = List("hwc", "zzy", "wzt", "wzs")
        val urls: List[String] = List("./myhome", "./home",
            "./cart",
            "./fav",
            "./prod?id=1",
            "./prod?id=2",
        )

        while (isRunning) {
            val event: Event = Event(users(random.nextInt(users.length)), urls(random.nextInt(urls.length)), Calendar.getInstance.getTimeInMillis)
            //collect方法发出数据
            ctx.collect(event)

            //每1s发出一次数据
            Thread.sleep(1000)
        }

    }

    override def cancel(): Unit = {
        this.isRunning = false
    }
}
