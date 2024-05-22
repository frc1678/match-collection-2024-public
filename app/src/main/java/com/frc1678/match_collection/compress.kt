// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.util.Log
import java.util.Locale

// Function to create compressed string displayed in QR.
// String takes collected data stored in References.kt compressed by Match Collection schema.
fun compress(
    schema: HashMap<String, HashMap<String, Any>>
): String {
    var compressedMatchInformation: String

    val schemaVersion = schema.getValue("schema_file").getValue("version").toString()

    // Define HashMaps for categories of data based on match_collection_qr_schema.yml.
    val genericData = schema.getValue("generic_data")
    val objectiveData = schema.getValue("objective_tim")
    val subjectiveData = schema.getValue("subjective_aim")
    val actionTypeData = schema.getValue("action_type")

    // Define compression characters for generic separators.
    val genericSeparator = genericData.getValue("_separator").toString()
    val genericSectionSeparator = genericData.getValue("_section_separator").toString()
    // Define compression characters for generic data.
    val compressSchemaVersion = genericData.getValue("schema_version").toString().split(",")[0]
    val compressMatchNumber = genericData.getValue("match_number").toString().split(",")[0]
    val compressTimestamp = genericData.getValue("timestamp").toString().split(",")[0]
    val compressVersionNum =
        genericData.getValue("match_collection_version_number").toString().split(",")[0]
    val compressScoutName = genericData.getValue("scout_name").toString().split(",")[0]
    val compressAllianceColor =
        genericData.getValue("alliance_color_is_red").toString().split(",")[0]

    // Define compression characters for objective separators.
    val objectiveStartCharacter = objectiveData.getValue("_start_character").toString()
    val objectiveSeparator = objectiveData.getValue("_separator").toString()
    // Define compression characters for objective data.
    val compressTeamNumber = objectiveData.getValue("team_number").toString().split(",")[0]
    val compressScoutId = objectiveData.getValue("scout_id").toString().split(",")[0]
    val compressStartingPosition = objectiveData.getValue("start_position").toString().split(",")[0]
    val compressTimeline = objectiveData.getValue("timeline").toString().split(",")[0]
    val compressPreloaded = objectiveData.getValue("has_preload").toString().split(",")[0]
    val compressChainRight = objectiveData.getValue("chain_right").toString().split(",")[0]
    val compressChainLeft = objectiveData.getValue("chain_left").toString().split(",")[0]
    val compressChainCenter = objectiveData.getValue("chain_center").toString().split(",")[0]
    val compressPark = objectiveData.getValue("park").toString().split(",")[0]

    // Define compression characters for subjective separators.
    val subjectiveStartCharacter = subjectiveData.getValue("_start_character").toString()
    val subjectiveSeparator = subjectiveData.getValue("_separator").toString()
    val subjectiveTeamSeparator = subjectiveData.getValue("_team_separator").toString()
    val subjectiveAllianceDataSeparator =
        subjectiveData.getValue("_alliance_data_separator").toString()
    val subjectiveTeamNumberSeparator =
        subjectiveData.getValue("team_number").toString().split(",")[0]
    // Define compression characters for subjective data.
    val compressQuicknessScore = subjectiveData.getValue("quickness_score").toString().split(",")[0]
    val compressAwareScore =
        subjectiveData.getValue("field_awareness_score").toString().split(",")[0]
    val compressClimbAfter =
        subjectiveData.getValue("climb_after").toString().split(",")[0]
    val compressTimeLeftToClimb =
        subjectiveData.getValue("time_left_to_climb").toString().split(",")[0]

    // Compress and add data shared between the objective and subjective modes.
    compressedMatchInformation =
        compressSchemaVersion + schemaVersion + genericSeparator +
                compressMatchNumber + matchNumber + genericSeparator +
                compressTimestamp + timestamp + genericSeparator +
                compressVersionNum + Constants.VERSION_NUMBER + genericSeparator +
                compressScoutName + scoutName.uppercase(Locale.US) + genericSeparator +
                compressAllianceColor + if (allianceColor == Constants.AllianceColor.RED) "TRUE" else "FALSE"

    // Compress and add data specific to Objective Match Collection.
    if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
        // Compress timeline actions if timeline exists      .
        var compressTimelineActions = ""
        if (timeline.isNotEmpty()) {
            for (actions in timeline) {
                // Compress and add timeline action attributes present for all actions.
                compressTimelineActions = compressTimelineActions +
                        actions.getValue("match_time") + actionTypeData.getValue(
                    actions.getValue("action_type").toString().lowercase(
                        Locale.US
                    )
                )
            }
        }

        // Compress and add all Objective Match Collection data, including previously compressed
        // timeline actions.
        compressedMatchInformation =
            objectiveStartCharacter + compressedMatchInformation + genericSectionSeparator +
                    compressTeamNumber + teamNumber + objectiveSeparator +
                    compressScoutId + scoutId + objectiveSeparator +
                    compressStartingPosition + startingPosition.toString() + objectiveSeparator +
                    compressTimeline + compressTimelineActions + objectiveSeparator +
                    compressPreloaded + (if (preloaded) "TRUE" else "FALSE") + objectiveSeparator +
                    compressChainRight + stageRightLevel + objectiveSeparator +
                    compressChainLeft + stageLeftLevel + objectiveSeparator +
                    compressChainCenter + stageCenterLevel + objectiveSeparator +
                    compressPark + (if (parked) "TRUE" else "FALSE")
    }
    // Compress and add data specific to Subjective Match Collection.
    else if (collectionMode == Constants.ModeSelection.SUBJECTIVE) {
        var subjDataString = ""
        val teamNumbers = subjectiveTeamRankingsToList(quicknessScore)

        teamNumbers.forEachIndexed { i, teamNum ->
            subjDataString += subjectiveTeamNumberSeparator
            subjDataString += teamNum
            val quickness = getRankForTeam(quicknessScore, teamNum)
            val fieldAwareness = getRankForTeam(fieldAwarenessScore, teamNum)
            val timeLeftToClimb = timeLeftToClimb[i].toString()
            val climbAfter = climbAfterList.contains(teamNum)

            subjDataString += subjectiveSeparator
            subjDataString += compressQuicknessScore
            subjDataString += quickness.toString()

            subjDataString += subjectiveSeparator
            subjDataString += compressAwareScore
            subjDataString += fieldAwareness.toString()

            subjDataString += subjectiveSeparator
            subjDataString += compressTimeLeftToClimb
            subjDataString += timeLeftToClimb.toString()

            subjDataString += subjectiveSeparator
            subjDataString += compressClimbAfter
            subjDataString += if (climbAfter) "TRUE" else "FALSE"

            if (i + 1 != teamNumbers.size) subjDataString += subjectiveTeamSeparator
        }

        // Compress and add all Subjective Match Collection data including previously compressed timeline actions.
        compressedMatchInformation =
            subjectiveStartCharacter + compressedMatchInformation + genericSectionSeparator + subjDataString
    }

    // Remove unnecessary brackets left from type conversion.
    compressedMatchInformation = compressedMatchInformation.replace("[", "")

    Log.d("compression", compressedMatchInformation)

    return compressedMatchInformation
}

fun getRankForTeam(teamRankings: SubjectiveTeamRankings, teamNumber: String): Int {
    return teamRankings.notNullList.first { it.teamNumber == teamNumber }.rank
}

fun subjectiveTeamRankingsToList(teamRankings: SubjectiveTeamRankings): List<String> {
    return teamRankings.notNullList.map { it.teamNumber }
}

