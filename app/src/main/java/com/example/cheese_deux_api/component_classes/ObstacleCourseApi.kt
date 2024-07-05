package com.example.cheese_deux_api.component_classes

import com.example.cheese_deux_api.cheese_deux.CheeseDeuxApi
import com.example.cheese_deux_api.cheese_deux.ObstacleCourseBody

class ObstacleCourseApi(val cheeseDeuxApi: CheeseDeuxApi) {
    private var obstacleLaneList = MutableList(8) { Pair(false, 1) }
    private var nextIterationObstacleLaneList = MutableList(8) { Pair(false, 1) }
    var hindranceTypeList = MutableList<HindranceClass>(8) {HindranceClass()}


    private suspend fun getInitialList(): List<String> {
        val obstacleCourse = try {
            cheeseDeuxApi.getObstacleList(obstacleCourseBody = ObstacleCourseBody(extent = 16)) //filling two lists
        } catch (t: Throwable) {
            null
        }

        return if (obstacleCourse != null && obstacleCourse.isSuccessful) {
            obstacleCourse.body()!!.obstacleCourse
        } else MutableList(4) { "" }
    }

    suspend fun getHindranceType(): HindranceClass {
        val hindrance = try {
            cheeseDeuxApi.getHitHindrance()
        } catch (t: Throwable) {
            null
        }
        var hindranceClass = HindranceClass()
        if (hindrance != null && hindrance.isSuccessful) {
            hindranceClass = HindranceClass(
                type = when (hindrance.body()!!.type) {
                    1 -> HindranceType.SPEEDUP
                    2 -> HindranceType.AUTOJUMP
                    3 -> HindranceType.CAT_CLOSER
                    else -> {
                        HindranceType.NONE
                    }
                },
                amount = hindrance.body()!!.amount
            )
        }
        return hindranceClass
    }

    suspend fun initialiseObstacleList(): Pair<MutableList<Pair<Boolean, Int>>, MutableList<Pair<Boolean, Int>>> {
        val responseString: List<String> = getInitialList()
        responseString.forEachIndexed { index, laneStr ->
            if (index < 8) {
                obstacleLaneList[index] = stringToLane(laneStr)
            } else {
                nextIterationObstacleLaneList[index-8] = stringToLane(laneStr)
            }
        }
        //gets hindrance type for each hindrance
        obstacleLaneList.forEachIndexed {index, (isHindrance, _) ->
            if (isHindrance) {
                hindranceTypeList[index] = getHindranceType()
            }
        }
        return Pair(obstacleLaneList, nextIterationObstacleLaneList)
    }

    private fun stringToLane(laneStr: String): Pair<Boolean, Int> {
        return when (laneStr) {
            "L" -> Pair(false, 1)

            "M" -> Pair(false, 2)

            "R" -> Pair(false, 3)

            "B" -> Pair(true, (1..3).random())

            else -> Pair(false, (1..3).random())
        }
    }

    suspend fun getLaneIndex(obstacleIndex: Int): Pair<Boolean, Int> {
        val lane = obstacleLaneList[obstacleIndex]
        if (lane.first) {
            hindranceTypeList[obstacleIndex] = getHindranceType()
        }
        obstacleLaneList[obstacleIndex] = nextIterationObstacleLaneList[obstacleIndex]
        updateNextIterationObstacleList(obstacleIndex)

        return lane
    }

    private suspend fun updateNextIterationObstacleList(obstacleIndex: Int) {
        val newLaneResponse = try {
            cheeseDeuxApi.getObstacleList(obstacleCourseBody = ObstacleCourseBody(extent = 1))
        } catch (t: Throwable) {
            null
        }
        if (newLaneResponse != null && newLaneResponse.isSuccessful) {
            val newLaneString =
                newLaneResponse.body()!!.obstacleCourse //list of 1 string, as we request only 1 string
            nextIterationObstacleLaneList[obstacleIndex] =
                stringToLane(newLaneString[0])
        }
    }
}