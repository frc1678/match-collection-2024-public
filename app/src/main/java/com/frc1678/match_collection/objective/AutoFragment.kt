package com.frc1678.match_collection.objective

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.AllianceColor
import com.frc1678.match_collection.R
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.autoIntakeList
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.numActionFerry
import com.frc1678.match_collection.numActionFerryDrive
import com.frc1678.match_collection.numActionFerryShoot
import com.frc1678.match_collection.numActionScoreAmp
import com.frc1678.match_collection.numActionScoreSpeaker
import com.frc1678.match_collection.orientation
import com.frc1678.match_collection.scoring
import kotlinx.android.synthetic.main.collection_objective_auto_fragment.view.auto_compose_view

/**
 * [Fragment] used for showing intake buttons in [AutoFragment]
 */
class AutoFragment : Fragment(R.layout.collection_objective_auto_fragment) {

    /**
     * The main view of this fragment.
     */
    private var mainView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mainView = super.onCreateView(inflater, container, savedInstanceState)!!
        setContent()
        return mainView
    }

    /**
     * Parent activity of this fragment
     */
    private val collectionObjectiveActivity get() = activity as CollectionObjectiveActivity

    /**
     * This is the compose function that creates the layout for the compose view in collection_objective_auto_fragment.
     */
    private fun setContent() {
        mainView!!.auto_compose_view.setContent {
            /*
            This box contains all the elements that will be displayed, it is rotated based on your orientation.
            The elements within the box are aligned to the left or the right depending on the alliance color.
             */
            BoxWithConstraints(
                contentAlignment = if (allianceColor == AllianceColor.BLUE) Alignment.TopStart else Alignment.TopEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (orientation) 0f else 180f)
            ) {
                /*
                This image view is behind everything else in the box
                and displays one of two images based on your alliance.
                */
                Image(
                    painter = painterResource(
                        id = when {
                            (allianceColor == AllianceColor.BLUE) -> R.drawable.crescendo_map_auto_blue
                            else -> R.drawable.crescendo_map_auto_red
                        }
                    ),
                    contentDescription = "Map with game pieces",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize(),
                )

                if (scoring) {
                    // Ferry Button
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(110 * maxWidth / 176)
                            .clickable {
                                if (collectionObjectiveActivity.isTimerRunning) {
                                    val newPressTime = System.currentTimeMillis()
                                    if (buttonPressedTime + 250 < newPressTime) {
                                        buttonPressedTime = newPressTime
                                        if (matchTimer != null) {
                                            collectionObjectiveActivity.timelineAddWithStage(
                                                action_type = Constants.ActionType.FERRY
                                            )
                                            if (!collectionObjectiveActivity.failing) numActionFerry++
                                            scoring = false
                                            collectionObjectiveActivity.failing = false
                                            collectionObjectiveActivity.enableButtons()
                                        }
                                    }
                                }
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !collectionObjectiveActivity.isTimerRunning) Color(
                                    142,
                                    142,
                                    142
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color.Blue.copy(alpha = 0.6f)
                                else Color.Red.copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !collectionObjectiveActivity.isTimerRunning) Color(
                                    239,
                                    239,
                                    239
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color(
                                    33,
                                    150,
                                    243
                                ).copy(alpha = 0.6f)
                                else Color(243, 33, 33).copy(alpha = 0.6f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(
                                0.dp,
                                if (orientation) maxHeight / 4 else maxHeight / -4
                            )
                        ) {
                            Text(text = "FERRY:", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(
                                text = "$numActionFerry",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                //SCORING BUTTONS
                /*
                When a scoring button is clicked, adds the action to the timeline,
                adds to the count for that action if they are not failing the score,
                then switches to intaking and enables buttons.
                The background, border, and text colors are set depending on if they
                are incap or not and the alliance. The contents of the buttons are
                rotated depending on the orientation so that they are not upside
                down for certain orientations.
                 */
                if (scoring) {
                    //AMP BUTTON
                    Box(
                        modifier = Modifier
                            .size(10 * maxWidth / 22, 15 * maxHeight / 100)
                            .clickable {
                                if (collectionObjectiveActivity.isTimerRunning) {
                                    val newPressTime = System.currentTimeMillis()
                                    if (buttonPressedTime + 250 < newPressTime) {
                                        buttonPressedTime = newPressTime
                                        if (matchTimer != null) {
                                            collectionObjectiveActivity.timelineAddWithStage(
                                                action_type = Constants.ActionType.SCORE_AMP
                                            )
                                            if (!collectionObjectiveActivity.failing) numActionScoreAmp++
                                            scoring = false
                                            collectionObjectiveActivity.failing = false
                                            collectionObjectiveActivity.enableButtons()
                                        }
                                    }
                                }
                            }
                            .border(
                                4.dp,
                                if (!collectionObjectiveActivity.isTimerRunning) Color(
                                    142,
                                    142,
                                    142
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color(
                                    30,
                                    20,
                                    125
                                ).copy(alpha = 0.6f)
                                else Color(125, 20, 20).copy(alpha = 0.6f)
                            )
                            .background(
                                if (!collectionObjectiveActivity.isTimerRunning) Color(
                                    239,
                                    239,
                                    239
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color.Blue.copy(alpha = 0.6f)
                                else Color.Red.copy(alpha = 0.6f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SCORE AMP: $numActionScoreAmp",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (!collectionObjectiveActivity.isTimerRunning) Color.Black else Color.White
                            )
                        )
                    }
                    // SPEAKER BUTTON
                    Box(
                        modifier = Modifier
                            .size(20.25 * maxWidth / 100, 20 * maxHeight / 42)
                            .offset(0.dp, 9 * maxHeight / 54)
                            .clickable {
                                if (collectionObjectiveActivity.isTimerRunning) {
                                    val newPressTime = System.currentTimeMillis()
                                    if (buttonPressedTime + 250 < newPressTime) {
                                        buttonPressedTime = newPressTime
                                        if (matchTimer != null) {
                                            collectionObjectiveActivity.timelineAddWithStage(
                                                action_type = Constants.ActionType.SCORE_SPEAKER
                                            )
                                            if (!collectionObjectiveActivity.failing) numActionScoreSpeaker++
                                            scoring = false
                                            collectionObjectiveActivity.failing = false
                                            collectionObjectiveActivity.enableButtons()
                                        }
                                    }
                                }
                            }
                            .border(
                                4.dp,
                                if (!collectionObjectiveActivity.isTimerRunning) Color(
                                    142,
                                    142,
                                    142
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color(
                                    30,
                                    20,
                                    125
                                ).copy(alpha = 0.6f)
                                else Color(125, 20, 20).copy(alpha = 0.6f)
                            )
                            .background(
                                if (!collectionObjectiveActivity.isTimerRunning) Color(
                                    239,
                                    239,
                                    239
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color.Blue.copy(alpha = 0.6f)
                                else Color.Red.copy(alpha = 0.6f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCORE",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!collectionObjectiveActivity.isTimerRunning) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "SPEAKER:",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!collectionObjectiveActivity.isTimerRunning) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "$numActionScoreSpeaker",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!collectionObjectiveActivity.isTimerRunning) Color.Black else Color.White
                                )
                            )
                        }
                    }
                }
                // INTAKE BUTTONS
                // Refer to the comment for AutoIntakeButton() for details on the intake buttons
                else {
                    // SPIKE NOTE INTAKE BUTTONS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 20 * maxWidth / 77 else 20 * maxWidth / -77,
                                maxHeight / 17
                            )
                            .size(maxWidth / 11, 10 * maxHeight / 19)
                    ) {
                        AutoIntakeButton(
                            intakeNum = 0,
                            actionType = Constants.ActionType.AUTO_INTAKE_SPIKE_1,
                            modifier = Modifier.weight(1 / 3f)
                        )
                        AutoIntakeButton(
                            intakeNum = 1,
                            actionType = Constants.ActionType.AUTO_INTAKE_SPIKE_2,
                            modifier = Modifier.weight(1 / 3f)
                        )
                        AutoIntakeButton(
                            intakeNum = 2,
                            actionType = Constants.ActionType.AUTO_INTAKE_SPIKE_3,
                            modifier = Modifier.weight(1 / 3f)
                        )
                    }
                    // CENTER NOTE INTAKE BUTTONS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(if (allianceColor == AllianceColor.BLUE) 46 * maxWidth / 55 else 46 * maxWidth / -55)
                            .width(maxWidth / 11)
                            .fillMaxHeight()
                    ) {
                        AutoIntakeButton(
                            intakeNum = 3,
                            actionType = Constants.ActionType.AUTO_INTAKE_CENTER_1,
                            modifier = Modifier.weight(1 / 5f)
                        )
                        AutoIntakeButton(
                            intakeNum = 4,
                            actionType = Constants.ActionType.AUTO_INTAKE_CENTER_2,
                            modifier = Modifier.weight(1 / 5f)
                        )
                        AutoIntakeButton(
                            intakeNum = 5,
                            actionType = Constants.ActionType.AUTO_INTAKE_CENTER_3,
                            modifier = Modifier.weight(1 / 5f)
                        )
                        AutoIntakeButton(
                            intakeNum = 6,
                            actionType = Constants.ActionType.AUTO_INTAKE_CENTER_4,
                            modifier = Modifier.weight(1 / 5f)
                        )
                        AutoIntakeButton(
                            intakeNum = 7,
                            actionType = Constants.ActionType.AUTO_INTAKE_CENTER_5,
                            modifier = Modifier.weight(1 / 5f)
                        )
                    }
                }
            }
        }
    }

    /*
    When a intake button is clicked, adds the action to the timeline,
    adds to the count for that action, then switches to scoring and
    enables buttons.
    The background, border, and text colors are set depending on if they
    are incap or not. The contents of the buttons are rotated depending
    on the orientation so that they are not upside down for certain
    orientations.
     */
    @Composable
    fun AutoIntakeButton(intakeNum: Int, actionType: Constants.ActionType, modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable {
                    if (!autoIntakeList[intakeNum] && collectionObjectiveActivity.isTimerRunning) {
                        val newPressTime = System.currentTimeMillis()
                        if (buttonPressedTime + 250 < newPressTime) {
                            buttonPressedTime = newPressTime
                            if (matchTimer != null) {
                                autoIntakeList =
                                    autoIntakeList
                                        .toMutableList()
                                        .apply { set(intakeNum, true) }
                                collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                                scoring = true
                                collectionObjectiveActivity.enableButtons()
                            }
                        }
                    }
                }
                .border(
                    4.dp,
                    if (autoIntakeList[intakeNum] || !collectionObjectiveActivity.isTimerRunning) Color(
                        142,
                        142,
                        142
                    ).copy(alpha = 0.6f)
                    else Color(255, 87, 34).copy(alpha = 0.6f)
                )
                .background(
                    if (autoIntakeList[intakeNum] || !collectionObjectiveActivity.isTimerRunning) Color(
                        239,
                        239,
                        239
                    ).copy(alpha = 0.6f)
                    else Color(255, 152, 0).copy(alpha = 0.6f)
                )
                .rotate(if (orientation) 0f else 180f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (autoIntakeList[intakeNum]) "TAKEN" else "${intakeNum + 1}",
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
        }
    }
}