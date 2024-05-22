// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// File to store information to be used to create the final match information map.
var numActionScoreAmp = 0 //SCORE_AMP
var numActionScoreSpeaker = 0 //SCORE_SPEAKER
var numActionFail = 0 //FAIL
var numActionDrop = 0 //DROP
var numActionFerry = 0 //FERRY (AUTO)
var numActionFerryDrive = 0 //FERRY_DRIVE
var numActionFerryShoot = 0 //FERRY_SHOOT
var numActionIntakeAmp = 0 //INTAKE_AMP
var numActionIntakePoach = 0 //INTAKE_POACH
var numActionIntakeCenter = 0 //INTAKE_CENTER
var numActionIntakeFar = 0 //INTAKE_FAR
var numActionScoreAmplify = 0 //SCORE_AMPLIFY
var numActionScoreTrap = 0 //SCORE_TRAP
var numActionIntakeOther = 0 //INTAKE_OTHER

var autoIntakeList by mutableStateOf(List(size = 8) { false })

var matchTimer: CountDownTimer? = null
var matchTime: String = ""
var isTeleopActivated: Boolean = false
var popupOpen = false
var isMatchTimeEnded: Boolean = false
var collectionMode: Constants.ModeSelection = Constants.ModeSelection.NONE
var assignMode: Constants.AssignmentMode = Constants.AssignmentMode.NONE
var stageLeftLevel by mutableStateOf(Constants.StageLevel.N)
var stageRightLevel by mutableStateOf(Constants.StageLevel.N)
var stageCenterLevel by mutableStateOf(Constants.StageLevel.N)
var parked by mutableStateOf(false)
var scoring by mutableStateOf(false)

// Keeps track of what time the last button press was
// Used to add a cooldown for how often you can press buttons, any
// presses within 250 milliseconds of a button press will be ignored
var buttonPressedTime = System.currentTimeMillis()

// Data that is shared between the objective and subjective QRs.
var matchNumber: Int = 1
var allianceColor: Constants.AllianceColor = Constants.AllianceColor.NONE
var timestamp: Long = 0
var scoutName: String = Constants.NONE_VALUE

// Data specific to Objective Match Collection QR.
var teamNumber: String = ""
var scoutId: String = Constants.NONE_VALUE
var orientation by mutableStateOf(true)
var startingPosition: Int? by mutableStateOf(null)
var preloaded = false
var timeline = mutableListOf<Map<String, String>>()

// Data specific to Subjective Match Collection QR.
var quicknessScore: SubjectiveTeamRankings = SubjectiveTeamRankings()
var fieldAwarenessScore: SubjectiveTeamRankings = SubjectiveTeamRankings()
var timeLeftToClimb: MutableList<Int> = MutableList(3) { 0 }
var climbAfterList: ArrayList<String> = ArrayList()

// Function to reset References.kt variables for new match.
fun resetCollectionReferences() {
    numActionScoreAmp = 0
    numActionScoreSpeaker = 0
    numActionFail = 0
    numActionDrop = 0
    numActionFerry = 0
    numActionFerryDrive = 0
    numActionFerryShoot = 0
    numActionIntakeAmp = 0
    numActionIntakePoach = 0
    numActionIntakeCenter = 0
    numActionIntakeFar = 0
    numActionScoreAmplify = 0
    numActionScoreTrap = 0
    numActionIntakeOther = 0

    var i = 0
    while (i < 8) {
        autoIntakeList = autoIntakeList.toMutableList().apply { set(i, false) }
        i++
    }

    isTeleopActivated = false

    popupOpen = false

    stageLeftLevel = Constants.StageLevel.N
    stageRightLevel = Constants.StageLevel.N
    stageCenterLevel = Constants.StageLevel.N
    parked = false

    timestamp = 0

    timeline = ArrayList()

    quicknessScore = SubjectiveTeamRankings()
    fieldAwarenessScore = SubjectiveTeamRankings()
    timeLeftToClimb = MutableList(3) { 0 }

}

data class SubjectiveTeamRankings(
    val teamOne: TeamRank? = null, val teamTwo: TeamRank? = null, val teamThree: TeamRank? = null
) {
    private val list: List<TeamRank?>
        get() = listOf(teamOne, teamTwo, teamThree)

    val notNullList: List<TeamRank>
        get() = this.list.filterNotNull()


    fun hasDuplicate(): Boolean {
        val ranks = mutableListOf<Int>()
        for (team in this.notNullList) {
            ranks.add(team.rank)
        }
        return ranks.toSet().toList() != ranks
    }
}

data class TeamRank(var teamNumber: String, val rank: Int)

fun resetStartingReferences() {
    startingPosition = null
    teamNumber = ""
    preloaded = false
}
