package Table_API

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.annotation.{DataTypeHint, FunctionHint}
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.table.functions.TableFunction
import org.apache.flink.types.Row

object UDF_TableFunction {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        //1. 在创建表DDL的语句中设置时间属性字段
        tableEnv.executeSql("CREATE TABLE eventTable (" +
            "uid STRING," +
            "url STRING," +
            "ts BIGINT," +
            "eventTime AS TO_TIMESTAMP( FROM_UNIXTIME( ts / 1000 ))," +
            "WATERMARK FOR eventTime AS eventTime - INTERVAL '5' SECOND" +
            ") WITH (" +
            " 'connector' = 'filesystem'," +
            " 'path' = 'datas/clicks.txt'," +
            " 'format' = 'csv' " +
            ")")

        //注册表函数
        tableEnv.createTemporarySystemFunction("mySplit", classOf[mySplit])

        //使用SQL调用函数查询转换
        val resultTable: Table = tableEnv.sqlQuery("SELECT uid, url,word, len from eventTable, " +
            "LATERAL TABLE(mySplit(url)) as T(word,len)")
        //as T(word,len)设置别名
        tableEnv.toAppendStream[Row](resultTable).print()
        env.execute()
    }

    // 注册自定义函数，按照 '?' 分割url字段
    @FunctionHint(output = new DataTypeHint("Row<word String, len Int>"))
    class mySplit extends TableFunction[Row] {
        def eval(s: String) = {
            s.split("\\?").foreach(string => {
                //使用collect发送出去
                collect(Row.of(string, Int.box(string.length)))
            })
        }
    }

}
