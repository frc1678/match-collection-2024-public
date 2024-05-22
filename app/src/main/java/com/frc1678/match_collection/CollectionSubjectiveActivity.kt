// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Screens.COLLECTION_SUBJECTIVE


/**
 * Activity for Subjective Match Collection to scout the subjective gameplay of an alliance team in a match.
 */
class CollectionSubjectiveActivity : CollectionActivity() {

    private lateinit var teamNumberOne: String
    private lateinit var teamNumberTwo: String
    private lateinit var teamNumberThree: String

    /**
     * Finds the teams that are playing in that match
     */
    private fun getExtras() {
        teamNumberOne = intent.extras?.getString("team_one").toString()
        teamNumberTwo = intent.extras?.getString("team_two").toString()
        teamNumberThree = intent.extras?.getString("team_three").toString()
    }

    // Begin intent used in onKeyLongPress to restart app from StartingGamePieceActivity.kt.
    private fun intentToMatchInput() {
        startActivity(
            Intent(this, MatchInformationInputActivity::class.java).putExtras(intent)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.COLLECTION_SUBJECTIVE),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    // Restart app from StartingGamePieceActivity.kt when back button is long pressed.
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                AlertDialog.Builder(this).setMessage(R.string.error_back_reset)
                    .setPositiveButton("Yes") { _, _ -> intentToMatchInput() }
                    .show()
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubjectiveTeams()
        }
        getExtras()
    }

    @Composable
    fun SubjectiveTeams(
        context: Context = LocalContext.current
    ) {
        //Sets variables so they have default values and are remembered when returning from the match edit screen
        var teamOneQuickness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_one_quickness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamTwoQuickness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_two_quickness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamThreeQuickness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_three_quickness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamOneFieldAwareness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_one_field_awareness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamTwoFieldAwareness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_two_field_awareness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamThreeFieldAwareness by remember {
            mutableStateOf(
                intent.extras?.getInt("team_three_field_awareness")?.takeUnless { it == 0 } ?: 2)
        }
        var teamOneTimeLeftToClimb by remember {
            mutableStateOf(
                intent.extras?.getInt("team_one_time_left_to_climb")
            )
        }
        var teamTwoTimeLeftToClimb by remember {
            mutableStateOf(
                intent.extras?.getInt("team_two_time_left_to_climb")
            )
        }
        var teamThreeTimeLeftToClimb by remember {
            mutableStateOf(
                intent.extras?.getInt("team_three_time_left_to_climb")
            )
        }
        var teamOneClimbAfter by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_one_climb_after") ?: false
            )
        }
        var teamTwoClimbAfter by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_two_climb_after") ?: false
            )
        }
        var teamThreeClimbAfter by remember {
            mutableStateOf(
                intent.extras?.getBoolean("team_three_climb_after") ?: false
            )
        }
        // Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Team numbers
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text("")
                    Text(
                        modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                        text = teamNumberOne,
                        color = if (allianceColor == Constants.AllianceColor.BLUE) Color(
                            20,
                            112,
                            205
                        ) else Color(208, 55, 44),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                    Text(
                        modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                        text = teamNumberTwo,
                        color = if (allianceColor == Constants.AllianceColor.BLUE) Color(
                            20,
                            112,
                            205
                        ) else Color(208, 55, 44),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                    Text(
                        modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                        text = teamNumberThree,
                        color = if (allianceColor == Constants.AllianceColor.BLUE) Color(
                            20,
                            112,
                            205
                        ) else Color(208, 55, 44),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )


                }
                // Climb after checkboxes
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Climb After")
                    SwitchButton(
                        setSwitchState = { teamOneClimbAfter = it },
                        switchState = teamOneClimbAfter
                    )
                    SwitchButton(
                        setSwitchState = { teamTwoClimbAfter = it },
                        switchState = teamTwoClimbAfter
                    )
                    SwitchButton(
                        setSwitchState = { teamThreeClimbAfter = it },
                        switchState = teamThreeClimbAfter
                    )
                }
                // Secs Climb at int inputs
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Secs Climb At")
                    CounterButton(
                        setCounterValue = {
                            teamOneTimeLeftToClimb = it
                            timeLeftToClimb[0] = it
                        }, counterValue =
                        teamOneTimeLeftToClimb!!, seconds = true
                    )
                    CounterButton(
                        setCounterValue = {
                            teamTwoTimeLeftToClimb = it
                            timeLeftToClimb[1] = it
                        }, counterValue =
                        teamTwoTimeLeftToClimb!!, seconds = true
                    )
                    CounterButton(
                        setCounterValue = {
                            teamThreeTimeLeftToClimb = it
                            timeLeftToClimb[2] = it
                        }, counterValue =
                        teamThreeTimeLeftToClimb!!, seconds = true
                    )
                }
                // Quickness inputs
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Quickness")
                    CounterButton(
                        setCounterValue = { teamOneQuickness = it }, counterValue =
                        teamOneQuickness
                    )
                    CounterButton(
                        setCounterValue = { teamTwoQuickness = it }, counterValue =
                        teamTwoQuickness
                    )
                    CounterButton(
                        setCounterValue = { teamThreeQuickness = it }, counterValue =
                        teamThreeQuickness
                    )
                }
                //Field Awareness inputs
                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("Field Awareness")
                    CounterButton(
                        setCounterValue = { teamOneFieldAwareness = it }, counterValue =
                        teamOneFieldAwareness
                    )
                    CounterButton(
                        setCounterValue = { teamTwoFieldAwareness = it }, counterValue =
                        teamTwoFieldAwareness
                    )
                    CounterButton(
                        setCounterValue = { teamThreeFieldAwareness = it }, counterValue =
                        teamThreeFieldAwareness
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeightIn(100.dp)
                    .padding(bottom = 25.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                //Proceed button
                Button(
                    onClick = {
                        //creates lists of switch states for all datapoints
                        val climbAfterStateList =
                            listOf(teamOneClimbAfter, teamTwoClimbAfter, teamThreeClimbAfter)
                        val teamList = listOf(teamNumberOne, teamNumberTwo, teamNumberThree)

                        //run through the lists of switch states and add corresponding team to list if datapoint is true

                        for (i in 0..2) {
                            if (climbAfterStateList[i]) {
                                climbAfterList.add(teamList[i])
                            }
                        }

                        //stores datapoints as team ranks
                        quicknessScore = SubjectiveTeamRankings(
                            TeamRank(teamNumberOne, teamOneQuickness),
                            TeamRank(teamNumberTwo, teamTwoQuickness),
                            TeamRank(teamNumberThree, teamThreeQuickness)
                        )

                        fieldAwarenessScore = SubjectiveTeamRankings(
                            TeamRank(teamNumberOne, teamOneFieldAwareness),
                            TeamRank(teamNumberTwo, teamTwoFieldAwareness),
                            TeamRank(teamNumberThree, teamThreeFieldAwareness)
                        )

                        val intent = Intent(context, MatchInformationEditActivity::class.java)
                            .putExtra(PREVIOUS_SCREEN, COLLECTION_SUBJECTIVE)
                            .putExtra("team_one", teamNumberOne)
                            .putExtra("team_two", teamNumberTwo)
                            .putExtra("team_three", teamNumberThree)
                            .putExtra("team_one_climb_after", teamOneClimbAfter)
                            .putExtra("team_two_climb_after", teamTwoClimbAfter)
                            .putExtra("team_three_climb_after", teamThreeClimbAfter)
                            .putExtra("team_one_quickness", teamOneQuickness)
                            .putExtra("team_two_quickness", teamTwoQuickness)
                            .putExtra("team_three_quickness", teamThreeQuickness)
                            .putExtra("team_one_time_left_to_climb", teamOneTimeLeftToClimb)
                            .putExtra("team_two_time_left_to_climb", teamTwoTimeLeftToClimb)
                            .putExtra("team_three_time_left_to_climb", teamThreeTimeLeftToClimb)
                            .putExtra("team_one_field_awareness", teamOneFieldAwareness)
                            .putExtra("team_two_field_awareness", teamTwoFieldAwareness)
                            .putExtra("team_three_field_awareness", teamThreeFieldAwareness)

                        //Alert dialog if duplicate rankings
                        if (quicknessScore.hasDuplicate() or fieldAwarenessScore.hasDuplicate()) {
                            AlertDialog.Builder(context)
                                .setMessage("Only proceed if the robots were dead for the match. Otherwise, close. ")
                                .setNegativeButton("Close") { dialog, _ -> }
                                .setPositiveButton("Proceed") { _, _ ->
                                    startActivity(intent)
                                }.show()
                        } else if (teamOneClimbAfter && teamTwoClimbAfter && teamThreeClimbAfter) {
                            AlertDialog.Builder(context)
                                .setMessage("Are you sure you want to proceed with all teams having climbed second?")
                                .setNegativeButton("No") { dialog, _ ->
                                    dialog.cancel()
                                }.setPositiveButton("Yes") { _, _ ->
                                    startActivity(intent)
                                }.show()
                        } else {
                            startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .requiredHeight(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                ) {
                    Text("Proceed")
                }
            }
        }
    }

    @Composable
    private fun CounterButton(
        setCounterValue: (Int) -> Unit,
        counterValue: Int,
        seconds: Boolean = false
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (seconds) {
                        if (counterValue > 0) {
                            setCounterValue(counterValue - 10)
                        }
                    } else {
                        if (counterValue > 1) {
                            setCounterValue(counterValue - 1)
                        }
                    }
                },
                modifier = Modifier
                    .requiredWidth(50.dp)
                    .padding(5.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xfff4c7c3)),
            ) {
                Text("-")
            }
            Text(counterValue.toString())
            Button(
                onClick = {
                    if (seconds) {
                        if (counterValue < 60) {
                            setCounterValue(counterValue + 10)
                        }
                    } else {
                        if (counterValue < 3) {
                            setCounterValue(counterValue + 1)
                        }
                    }
                },
                modifier = Modifier
                    .requiredWidth(50.dp)
                    .padding(5.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xffb7e1cd)),
            ) {
                Text("+")
            }
        }
    }

    // The yes/no switches, they swap back and forth between checked or not every time you hit them
    @Composable
    fun SwitchButton(setSwitchState: (Boolean) -> Unit, switchState: Boolean) {
        var checked by remember { mutableStateOf(false) }
        checked = switchState
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                setSwitchState(!switchState)
            },
            modifier = Modifier
                .scale(1.5f)
                .padding(5.dp),
        )
    }
}

