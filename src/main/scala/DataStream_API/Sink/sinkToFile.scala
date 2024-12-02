package DataStream_API.Sink

import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala._
import pojo.Event

object sinkToFile {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        val stream: DataStream[Event] = env.fromElements(Event("Mary", "./home", 1000L),
            Event("Bob", "./about", 1020L), Event("Jack", "./test", 1200L),
            Event("Mary", "./prod?id=1", 3000L),
            Event("Mary", "./adds?password=mary01234", 1256L)
        )
        //以文本的形式分布式地写入文件
        val fileSink = StreamingFileSink.forRowFormat(new Path("./outPut"),new SimpleStringEncoder[String]("UTF-8"))
          .build()
        stream.map(_.toString).addSink(fileSink)
        env.execute()
    }
}
