import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.measureTimeMillis

data class BenchmarkRecord(
    val listSize: Int,
    val numLists: Int,
    val mergeDuration: Long,
    val concurrentMergeDuration: Long,
)

suspend fun benchmarkMergeKSortedLists() {
    val BENCHMARK_FILENAME = "/Users/ankush/Documents/benchmark.json"
    val listSizeValues = listOf(10, 50, 100, 500, 1000, 5000, 10_000, 50_000, 100_000, 500_000, 1000_000)
    val numListsValues = listOf(10, 50, 100, 500, 1000, 5000, 10_000, 50_000, 100_000, 500_000, 1000_000)
    val records = mutableListOf<BenchmarkRecord>()

    fun sortedListGenerator(listSize: Int) = (1..listSize).map {
        (-listSize..listSize).random()
    }.sorted()

    listSizeValues.forEach outer@{ listSize ->
        numListsValues.forEach inner@{ numLists ->
            if ((kotlin.math.log(numLists.toDouble(), 10.0) +
                kotlin.math.log(listSize.toDouble(), 10.0)) > 8) {
                return@inner
            }
            val lists = (1..numLists).map { sortedListGenerator(listSize) }
            val mergeDuration = (1..5).minOf { measureTimeMillis { mergeKSortedLists(lists) } }
            val concurrentMergeDuration =
                (1..5).minOf { measureTimeMillis { concurrentlyMergeKSortedLists(lists) } }
            records.add(
                BenchmarkRecord(numLists, listSize, mergeDuration, concurrentMergeDuration),
            )
        }
    }
    File(BENCHMARK_FILENAME).writeText(
        GsonBuilder().setPrettyPrinting().create().toJson(records)
    )
}

fun main() {
    runBlocking {
        withContext(Dispatchers.Default) {
            benchmarkMergeKSortedLists()
        }
    }
}

