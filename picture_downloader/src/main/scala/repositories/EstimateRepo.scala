package repositories

import akka.stream.Materializer
import com.crobox.clickhouse.ClickhouseClient
import model.Estimate
import repository.AbstractRepository

import scala.concurrent.ExecutionContext

class EstimateRepo(client: ClickhouseClient, tableName: String)(implicit ec: ExecutionContext, mat: Materializer)
  extends AbstractRepository[Estimate](client, tableName){
}
