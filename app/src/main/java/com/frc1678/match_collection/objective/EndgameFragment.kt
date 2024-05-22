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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
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
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.numActionScoreTrap
import com.frc1678.match_collection.orientation
import com.frc1678.match_collection.parked
import com.frc1678.match_collection.scoring
import com.frc1678.match_collection.stageCenterLevel
import com.frc1678.match_collection.stageLeftLevel
import com.frc1678.match_collection.stageRightLevel
import kotlinx.android.synthetic.main.collection_objective_endgame_fragment.view.endgame_compose_view

/**
 * [Fragment] used for showing intake buttons in [TeleopFragment]
 */
class EndgameFragment : Fragment(R.layout.collection_objective_endgame_fragment) {

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
     * This is the compose function that creates the layout for the compose view in collection_objective_endgame_fragment.
     */
    private fun setContent() {
        mainView!!.endgame_compose_view.setContent {
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
                and displays one of two images based on your alliance color
                */
                Image(
                    painter = painterResource(
                        id = when {
                            (allianceColor == AllianceColor.BLUE) -> R.drawable.stage_blue
                            else -> R.drawable.stage_red
                        }
                    ),
                    contentDescription = "Stage Map",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize(),
                )
                Column(
                    modifier = Modifier
                        .size(maxWidth / 4, maxHeight / 4)
                        .align(Alignment.Center)
                        .offset(
                            if (allianceColor == AllianceColor.BLUE) maxWidth / 20 else maxWidth / -20,
                            0.dp
                        )
                ) {
                    // SCORE TRAP BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .clickable {
                                if (!collectionObjectiveActivity.isIncap && scoring && (numActionScoreTrap < 3)) {
                                    val newPressTime = System.currentTimeMillis()
                                    if (buttonPressedTime + 250 < newPressTime) {
                                        buttonPressedTime = newPressTime
                                        if (matchTimer != null) {
                                            collectionObjectiveActivity.timelineAddWithStage(action_type = Constants.ActionType.SCORE_TRAP)
                                            if (!collectionObjectiveActivity.failing) numActionScoreTrap++
                                            collectionObjectiveActivity.failing = false
                                            scoring = false
                                            collectionObjectiveActivity.enableButtons()
                                        }
                                    }
                                }
                            }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || !scoring || numActionScoreTrap >= 3)
                                    Color(142, 142, 142).copy(alpha = 0.6f)
                                else Color(19, 105, 21, 255).copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || !scoring || numActionScoreTrap >= 3)
                                    Color(239, 239, 239).copy(
                                    alpha = 0.6f
                                )
                                else Color(0, 153, 2, 255).copy(alpha = 0.6f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SCORE TRAP: $numActionScoreTrap",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (collectionObjectiveActivity.isIncap || !scoring || numActionScoreTrap >= 3)
                                    Color.Black else Color.White
                            )
                        )
                    }
                    // PARK TOGGLE BUTTON
                    /*
                    Disabled if they are onstage on any chain.
                    When clicked, toggles parked between true and false. The border and
                    background colors and text are changed accordingly. The contents
                    of the buttons are rotated depending on the orientation so that
                    they are not upside down for certain orientations.
                     */
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .clickable {
                                if (
                                    stageCenterLevel != Constants.StageLevel.O &&
                                    stageLeftLevel != Constants.StageLevel.O &&
                                    stageRightLevel != Constants.StageLevel.O
                                ) {
                                    val newPressTime = System.currentTimeMillis()
                                    if (buttonPressedTime + 250 < newPressTime) {
                                        buttonPressedTime = newPressTime
                                        if (matchTimer != null) parked = !parked
                                    }
                                }
                            }
                            .border(
                                4.dp,
                                if (
                                    stageCenterLevel == Constants.StageLevel.O ||
                                    stageLeftLevel == Constants.StageLevel.O ||
                                    stageRightLevel == Constants.StageLevel.O
                                ) {
                                    Color(142, 142, 142).copy(alpha = 0.6f)
                                } else if (parked) Color(24, 125, 20).copy(alpha = 0.6f)
                                else Color(255, 87, 34).copy(alpha = 0.6f)
                            )
                            .background(
                                if (
                                    stageCenterLevel == Constants.StageLevel.O ||
                                    stageLeftLevel == Constants.StageLevel.O ||
                                    stageRightLevel == Constants.StageLevel.O
                                ) {
                                    Color(239, 239, 239).copy(alpha = 0.6f)
                                } else if (parked) Color.Green.copy(alpha = 0.6f)
                                else Color(255, 152, 0).copy(alpha = 0.6f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (parked) "PARKED" else "NOT PARKED",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                // CHAIN TOGGLE BUTTONS
                /*
                Disabled if they are onstage on any other chain or if they are incap.
                When clicked, toggles the respective stage level variable from N
                (not attempted), to O (onstage), to F (failed), then back to N.
                The border and background colors and text are changed accordingly.
                Also sets parked to false if switched to onstage.
                The contents of the buttons are rotated depending on the orientation
                so that they are not upside down for certain orientations.
                 */
                // CENTER CHAIN TOGGLE BUTTON
                Box(
                    modifier = Modifier
                        .size(maxWidth / 4, maxHeight / 8)
                        .offset(
                            if (allianceColor == AllianceColor.BLUE) 45 * maxWidth / 64 else 45 * maxWidth / -64,
                            7 * maxHeight / 16
                        )
                        .clickable {
                            if (
                                stageLeftLevel != Constants.StageLevel.O &&
                                stageRightLevel != Constants.StageLevel.O &&
                                !collectionObjectiveActivity.isIncap
                            ) {
                                val newPressTime = System.currentTimeMillis()
                                if (buttonPressedTime + 250 < newPressTime) {
                                    buttonPressedTime = newPressTime
                                    if (matchTimer != null) {
                                        when (stageCenterLevel) {
                                            Constants.StageLevel.N -> {
                                                stageCenterLevel = Constants.StageLevel.O
                                                parked = false
                                            }

                                            Constants.StageLevel.O -> stageCenterLevel =
                                                Constants.StageLevel.F

                                            else -> stageCenterLevel = Constants.StageLevel.N
                                        }
                                        // use enableButtons if stage level is on stage
                                        collectionObjectiveActivity.enableButtons()
                                    }
                                }
                            }
                        }
                        .border(
                            4.dp,
                            if (
                                stageLeftLevel == Constants.StageLevel.O ||
                                stageRightLevel == Constants.StageLevel.O ||
                                collectionObjectiveActivity.isIncap
                            ) {
                                Color(142, 142, 142).copy(alpha = 0.6f)
                            } else if (stageCenterLevel == Constants.StageLevel.O) Color(
                                24,
                                125,
                                20
                            ).copy(alpha = 0.6f)
                            else if (stageCenterLevel == Constants.StageLevel.F) Color(
                                125,
                                20,
                                20
                            ).copy(alpha = 0.6f)
                            else Color(255, 87, 34).copy(alpha = 0.6f)
                        )
                        .background(
                            if (
                                stageLeftLevel == Constants.StageLevel.O ||
                                stageRightLevel == Constants.StageLevel.O ||
                                collectionObjectiveActivity.isIncap
                            ) {
                                Color(239, 239, 239).copy(alpha = 0.6f)
                            } else if (stageCenterLevel == Constants.StageLevel.O) Color.Green.copy(
                                alpha = 0.6f
                            )
                            else if (stageCenterLevel == Constants.StageLevel.F) Color.Red.copy(
                                alpha = 0.6f
                            )
                            else Color(255, 152, 0).copy(alpha = 0.6f)
                        )
                        .rotate(if (orientation) 0f else 180f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (stageCenterLevel) {
                                Constants.StageLevel.O -> "ONSTAGE"
                                Constants.StageLevel.F -> "FAILED"
                                else -> "NOT ATTEMPTED"
                            },
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (
                                    stageCenterLevel != Constants.StageLevel.F ||
                                    stageLeftLevel == Constants.StageLevel.O ||
                                    stageRightLevel == Constants.StageLevel.O ||
                                    collectionObjectiveActivity.isIncap
                                ) {
                                    Color.Black
                                } else Color.White
                            )
                        )
                    }
                }
                // LEFT CHAIN TOGGLE BUTTON
                Box(
                    modifier = Modifier
                        .size(maxWidth / 4, maxHeight / 8)
                        .offset(
                            if (allianceColor == AllianceColor.BLUE) 7 * maxWidth / 32 else 7 * maxWidth / -32,
                            if (allianceColor == AllianceColor.BLUE) 3 * maxHeight / 16 else 11 * maxHeight / 16
                        )
                        .clickable {
                            if (
                                stageCenterLevel != Constants.StageLevel.O &&
                                stageRightLevel != Constants.StageLevel.O &&
                                !collectionObjectiveActivity.isIncap
                            ) {
                                val newPressTime = System.currentTimeMillis()
                                if (buttonPressedTime + 250 < newPressTime) {
                                    buttonPressedTime = newPressTime
                                    if (matchTimer != null) {
                                        when (stageLeftLevel) {
                                            Constants.StageLevel.N -> {
                                                stageLeftLevel = Constants.StageLevel.O
                                                parked = false
                                            }

                                            Constants.StageLevel.O -> stageLeftLevel =
                                                Constants.StageLevel.F

                                            else -> stageLeftLevel = Constants.StageLevel.N
                                        }
                                        collectionObjectiveActivity.enableButtons()
                                    }
                                }
                            }
                        }
                        .border(
                            4.dp,
                            if (
                                stageCenterLevel == Constants.StageLevel.O ||
                                stageRightLevel == Constants.StageLevel.O ||
                                collectionObjectiveActivity.isIncap
                            ) {
                                Color(142, 142, 142).copy(alpha = 0.6f)
                            } else if (stageLeftLevel == Constants.StageLevel.O) Color(
                                24,
                                125,
                                20
                            ).copy(alpha = 0.6f)
                            else if (stageLeftLevel == Constants.StageLevel.F) Color(
                                125,
                                20,
                                20
                            ).copy(alpha = 0.6f)
                            else Color(255, 87, 34).copy(alpha = 0.6f)
                        )
                        .background(
                            if (
                                stageCenterLevel == Constants.StageLevel.O ||
                                stageRightLevel == Constants.StageLevel.O ||
                                collectionObjectiveActivity.isIncap
                            ) {
                                Color(239, 239, 239).copy(alpha = 0.6f)
                            } else if (stageLeftLevel == Constants.StageLevel.O) Color.Green.copy(
                                alpha = 0.6f
                            )
                            else if (stageLeftLevel == Constants.StageLevel.F) Color.Red.copy(alpha = 0.6f)
                            else Color(255, 152, 0).copy(alpha = 0.6f)
                        )
                        .rotate(if (orientation) 0f else 180f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (stageLeftLevel) {
                                Constants.StageLevel.O -> "ONSTAGE"
                                Constants.StageLevel.F -> "FAILED"
                                else -> "NOT ATTEMPTED"
                            },
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (
                                    stageLeftLevel != Constants.StageLevel.F ||
                                    stageCenterLevel == Constants.StageLevel.O ||
                                    stageRightLevel == Constants.StageLevel.O ||
                                    collectionObjectiveActivity.isIncap
                                ) {
                                    Color.Black
                                } else Color.White
                            )
                        )
                    }
                }
                // RIGHT CHAIN TOGGLE BUTTON
                Box(
                    modifier = Modifier
                        .size(maxWidth / 4, maxHeight / 8)
                        .offset(
                            if (allianceColor == AllianceColor.BLUE) 7 * maxWidth / 32 else 7 * maxWidth / -32,
                            if (allianceColor == AllianceColor.BLUE) 11 * maxHeight / 16 else 3 * maxHeight / 16
                        )
                        .clickable {
                            if (
                                stageCenterLevel != Constants.StageLevel.O &&
                                stageLeftLevel != Constants.StageLevel.O &&
                                !collectionObjectiveActivity.isIncap
                            ) {
                                val newPressTime = System.currentTimeMillis()
                                if (buttonPressedTime + 250 < newPressTime) {
                                    buttonPressedTime = newPressTime
                                    if (matchTimer != null) {
                                        when (stageRightLevel) {
                                            Constants.StageLevel.N -> {
                                                stageRightLevel = Constants.StageLevel.O
                                                parked = false
                                            }

                                            Constants.StageLevel.O -> stageRightLevel =
                                                Constants.StageLevel.F

                                            else -> stageRightLevel = Constants.StageLevel.N
                                        }
                                        collectionObjectiveActivity.enableButtons()
                                    }
                                }
                            }
                        }
                        .border(
                            4.dp,
                            if (
                                stageCenterLevel == Constants.StageLevel.O ||
                                stageLeftLevel == Constants.StageLevel.O ||
                                collectionObjectiveActivity.isIncap
                            ) {
                                Color(142, 142, 142).copy(alpha = 0.6f)
                            } else if (stageRightLevel == Constants.StageLevel.O) Color(
                                24,
                                125,
                                20
                            ).copy(alpha = 0.6f)
                            else if (stageRightLevel == Constants.StageLevel.F) Color(
                                125,
                                20,
                                20
                            ).copy(alpha = 0.6f)
                            else Color(255, 87, 34).copy(alpha = 0.6f)
                        )
                        .background(
                            if (
                                stageCenterLevel == Constants.StageLevel.O ||
                                stageLeftLevel == Constants.StageLevel.O ||
                                collectionObjectiveActivity.isIncap
                            ) {
                                Color(239, 239, 239).copy(alpha = 0.6f)
                            } else if (stageRightLevel == Constants.StageLevel.O) Color.Green.copy(
                                alpha = 0.6f
                            )
                            else if (stageRightLevel == Constants.StageLevel.F) Color.Red.copy(alpha = 0.6f)
                            else Color(255, 152, 0).copy(alpha = 0.6f)
                        )
                        .rotate(if (orientation) 0f else 180f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (stageRightLevel) {
                                Constants.StageLevel.O -> "ONSTAGE"
                                Constants.StageLevel.F -> "FAILED"
                                else -> "NOT ATTEMPTED"
                            },
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (
                                    stageRightLevel != Constants.StageLevel.F ||
                                    stageCenterLevel == Constants.StageLevel.O ||
                                    stageLeftLevel == Constants.StageLevel.O ||
                                    collectionObjectiveActivity.isIncap
                                ) {
                                    Color.Black
                                } else Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}