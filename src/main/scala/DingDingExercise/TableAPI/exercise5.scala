package DingDingExercise.TableAPI

import DingDingExercise.TableAPI.exercise5.changeScore
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.annotation.{DataTypeHint, FunctionHint}
import org.apache.flink.table.api.Expressions.{$, call}
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.{AggregateFunction, ScalarFunction, TableAggregateFunction, TableFunction}
import org.apache.flink.types.Row
import org.apache.flink.util.Collector

object exercise5 {
    def main(args: Array[String]): Unit = {
        val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env)
        tableEnv.executeSql("CREATE TABLE stu (" +
            "`id` STRING," +
            "`name` STRING," +
            "score DOUBLE NOT NULL," +
            "`timestamp` BIGINT" +
            ") WITH (" +
            " 'connector' = 'filesystem', " +
            " 'path' = 'datas/StuList.txt', " +
            " 'format' = 'csv' " +
            ")")
        tableEnv.createTemporarySystemFunction("changeScore", classOf[changeScore])
        val resultTable1: Table = tableEnv.sqlQuery("SELECT id, name, score, changeScore(150,score) FROM stu")

        //90优秀    80一般    60及格
        val assessScore = new AssessScore(90, 80, 60)
        tableEnv.createTemporarySystemFunction("AssessScore", assessScore)
        val resultTable2: Table = tableEnv.sqlQuery("SELECT id, name, score, level, hashCode FROM stu, " +
            "LATERAL TABLE(AssessScore(score)) as T(level, hashCode)")

        tableEnv.createTemporarySystemFunction("scoreAvg", classOf[scoreAvg])
        val resultTable3: Table = tableEnv.sqlQuery("SELECT id,scoreAvg(score) as avg_score FROM stu " +
            "GROUP BY id")

        tableEnv.createTemporarySystemFunction("scoreRank", classOf[scoreRank])
        val stuTable: Table = tableEnv.from("stu")
        val resultTable4: Table = stuTable.groupBy($("id")).flatAggregate(call("scoreRank", $("score"))
                as("score", "rank"))
            .select($("id"), $("score"), $("rank"))

        tableEnv.toAppendStream[Row](resultTable1).print("标量函数")
        tableEnv.toAppendStream[Row](resultTable2).print("表函数")
        tableEnv.toRetractStream[Row](resultTable3).print("聚合函数")
        tableEnv.toRetractStream[Row](resultTable4).print("表聚合函数")
        env.execute()
    }

    //标量函数
    class changeScore extends ScalarFunction {
        def eval(ratio: Int, score: Double): Double = {
            val number: Double = (score / 100) * ratio
            BigDecimal(number).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
        }
    }

    //表函数
    @FunctionHint(output = new DataTypeHint("Row<level String, hashCode Int>"))
    class AssessScore(good: Double, mid: Double, pass: Double) extends TableFunction[Row] {
        def eval(score: Double) = {
            var level = ""
            if (score.>=(good)) {
                level = "Good"
            } else if (score.>=(mid)) {
                level = "Fair"
            } else if (score.>=(pass)) {
                level = "Pass"
            } else {
                level = "Fail"
            }
            collect(Row.of(level, Int.box(score.hashCode())))
        }
    }

    case class scoreAcc(var sum: Double = 0, var cnt: Int = 0) //聚合累加器

    //聚合函数
    class scoreAvg extends AggregateFunction[java.lang.Double, scoreAcc] {
        override def getValue(acc: scoreAcc): java.lang.Double = {
            if (acc.cnt == 0) {
                null
            } else {
                acc.sum / acc.cnt
            }
        }

        override def createAccumulator(): scoreAcc = scoreAcc()

        def accumulate(acc: scoreAcc, inScore: java.lang.Double): Unit = {
            acc.sum += inScore
            acc.cnt += 1
        }
    }

    case class scoreRankResult(score: Double, rank: Int) //表聚合结果

    case class scoreRankAcc(var first: Double, var second: Double) //表聚合累加器

    //表聚合函数
    class scoreRank extends TableAggregateFunction[scoreRankResult, scoreRankAcc] {
        override def createAccumulator(): scoreRankAcc = {
            scoreRankAcc(0.0, 0.0)
        }

        def accumulate(acc: scoreRankAcc, inScore: Double): Unit = {
            if (inScore > acc.first) {
                acc.second = acc.first
                acc.first = inScore
            } else if (inScore > acc.second) {
                acc.second = inScore
            }
        }

        def emitValue(acc: scoreRankAcc, out: Collector[scoreRankResult]): Unit = {
            out.collect(scoreRankResult(acc.first, 1))
            out.collect(scoreRankResult(acc.second, 2))
        }
    }
}
