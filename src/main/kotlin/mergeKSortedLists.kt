import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.*

fun mergeTwoSortedLists(list1: List<Int>, list2: List<Int>): List<Int> {
    var index1 = 0
    var index2 = 0
    val result = mutableListOf<Int>()
    while(index1 < list1.size && index2 < list2.size) {
        if(list1[index1] < list2[index2]) {
            result.add(list1[index1])
            index1 += 1
        } else {
            result.add(list2[index2])
            index2 += 1
        }
    }
    result.addAll(list1.slice(index1 until list1.size))
    result.addAll(list2.slice(index2 until list2.size))
    return result.toList()
}

suspend fun concurrentlyMergeKSortedLists(lists: List<List<Int>>): List<Int> = coroutineScope {
    val channel = Channel<List<Int>>(1)
    val jobs = mutableListOf<Job>()
    repeat(lists.size) {
        jobs.add(
            launch {
                channel.send(lists[it])
            }
        )
    }
    repeat(lists.size - 1) {
        jobs.add(
            launch {
                channel.send(mergeTwoSortedLists(channel.receive(), channel.receive()))
            }
        )
    }
    jobs.joinAll()
    channel.receive()
}

fun mergeKSortedLists(lists: List<List<Int>>): List<Int> {
    val queue = ArrayDeque<List<Int>>()
    lists.forEach { queue.add(it) }
    while(queue.size > 1) {
        queue.addLast(mergeTwoSortedLists(queue.removeFirst(), queue.removeFirst()))
    }
    return queue.removeFirst()
}