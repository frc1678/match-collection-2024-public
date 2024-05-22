package com.frc1678.match_collection.objective

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.fragment.app.Fragment
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.AllianceColor
import com.frc1678.match_collection.R
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.numActionDrop
import com.frc1678.match_collection.numActionFerryDrive
import com.frc1678.match_collection.numActionFerryShoot
import com.frc1678.match_collection.numActionIntakeAmp
import com.frc1678.match_collection.numActionIntakeCenter
import com.frc1678.match_collection.numActionIntakeFar
import com.frc1678.match_collection.numActionIntakePoach
import com.frc1678.match_collection.numActionScoreAmp
import com.frc1678.match_collection.numActionScoreSpeaker
import com.frc1678.match_collection.orientation
import com.frc1678.match_collection.scoring
import kotlinx.android.synthetic.main.collection_objective_teleop_fragment.view.teleop_compose_view

/**
 * [Fragment] used for showing intake buttons in [TeleopFragment]
 */
class TeleopFragment : Fragment(R.layout.collection_objective_teleop_fragment) {

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
     * This is the compose function that creates the layout for the compose view in collection_objective_teleop_fragment.
     */
    private fun setContent() {
        mainView!!.teleop_compose_view.setContent {
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
                Image(
                    painter = painterResource(id = R.drawable.crescendo_map),
                    contentDescription = "Field Map",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.matchParentSize(),
                )
                // SCORING BUTTONS
                /*
                Refer to the comment on scoringButtonPress() for details on pressing the
                buttons.
                The background, border, and text colors are set depending on if they
                are incap and the alliance color. The contents of the buttons are
                rotated depending on the orientation so that they are not upside
                down for certain orientations.
                 */
                if (scoring) {
                    // FERRY DRIVE BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(5 * maxWidth / 20)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 0 * maxWidth / 20 else 0 * maxWidth / -20,
                                0.dp
                            )
                            .clickable { scoringButtonPress(actionType = Constants.ActionType.FERRY_DRIVE) }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || collectionObjectiveActivity.failing) Color(
                                    142,
                                    142,
                                    142
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.BLUE) Color.Blue.copy(alpha = 0.6f)
                                else Color.Red.copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || collectionObjectiveActivity.failing) Color(
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
                                if (
                                    (allianceColor == Constants.AllianceColor.BLUE && orientation) ||
                                    (allianceColor == Constants.AllianceColor.RED && !orientation)
                                ) {
                                    maxWidth / 5
                                } else maxWidth / -5,
                                if (orientation) maxHeight / 4 else maxHeight / -4
                            )
                        ) {
                            Text(text = "FERRY", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(text = "DRIVE:", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(
                                text = "$numActionFerryDrive",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // FERRY SHOOT BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(5 * maxWidth / 20)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 5 * maxWidth / 20 else 5 * maxWidth / -20,
                                0.dp
                            )
                            .clickable { scoringButtonPress(actionType = Constants.ActionType.FERRY_SHOOT) }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap) Color(
                                    142,
                                    142,
                                    142
                                ).copy(alpha = 0.6f)
                                else Color(255, 87, 34).copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap) Color(
                                    239,
                                    239,
                                    239
                                ).copy(alpha = 0.6f)
                                else Color(255, 152, 0).copy(alpha = 0.6f)
                            )
                            .rotate(if (orientation) 0f else 180f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(
                                if (
                                    (allianceColor == Constants.AllianceColor.BLUE && orientation) ||
                                    (allianceColor == Constants.AllianceColor.RED && !orientation)
                                ) {
                                    maxWidth / 5
                                } else maxWidth / -5,
                                0.dp
                            )
                        ) {
                            Text(text = "FERRY", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(text = "SHOOT:", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(
                                text = "$numActionFerryShoot",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // DROP BUTTON
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(10 * maxWidth / 20)
                            .offset(
                                if (allianceColor == AllianceColor.BLUE) 10 * maxWidth / 20 else 10 * maxWidth / -20,
                                0.dp
                            )
                            .clickable { scoringButtonPress(actionType = Constants.ActionType.DROP) }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap || collectionObjectiveActivity.failing) Color(
                                    142,
                                    142,
                                    142
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.RED) Color.Blue.copy(alpha = 0.6f)
                                else Color.Red.copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap || collectionObjectiveActivity.failing) Color(
                                    239,
                                    239,
                                    239
                                ).copy(alpha = 0.6f)
                                else if (allianceColor == AllianceColor.RED) Color(
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
                                if (
                                    (allianceColor == Constants.AllianceColor.BLUE && orientation) ||
                                    (allianceColor == Constants.AllianceColor.RED && !orientation)
                                ) {
                                    maxWidth / -11
                                } else maxWidth / 11,
                                0.dp
                            )
                        ) {
                            Text(text = "DROP:", style = TextStyle(fontWeight = FontWeight.Bold))
                            Text(
                                text = "$numActionDrop",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    // SCORE AMP BUTTON
                    Box(
                        modifier = Modifier
                            .size(21 * maxWidth / 60, 15 * maxHeight / 100)
                            .clickable { scoringButtonPress(actionType = Constants.ActionType.SCORE_AMP) }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap) Color(142, 142, 142).copy(
                                    alpha = 0.6f
                                )
                                else if (allianceColor == AllianceColor.BLUE) Color(
                                    30,
                                    20,
                                    125
                                ).copy(alpha = 0.6f)
                                else Color(125, 20, 20).copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap) Color(239, 239, 239).copy(
                                    alpha = 0.6f
                                )
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
                                color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                            )
                        )
                    }
                    // SCORE SPEAKER BUTTON
                    Box(
                        modifier = Modifier
                            .size(12 * maxWidth / 60, 20 * maxHeight / 42)
                            .offset(0.dp, 9 * maxHeight / 54)
                            .clickable { scoringButtonPress(actionType = Constants.ActionType.SCORE_SPEAKER) }
                            .border(
                                4.dp,
                                if (collectionObjectiveActivity.isIncap) Color(142, 142, 142).copy(
                                    alpha = 0.6f
                                )
                                else if (allianceColor == AllianceColor.BLUE) Color(
                                    30,
                                    20,
                                    125
                                ).copy(alpha = 0.6f)
                                else Color(125, 20, 20).copy(alpha = 0.6f)
                            )
                            .background(
                                if (collectionObjectiveActivity.isIncap) Color(239, 239, 239).copy(
                                    alpha = 0.6f
                                )
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
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "SPEAKER:",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "$numActionScoreSpeaker",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                        }
                    }
                }
                // INTAKE BUTTONS
                /*
                Refer to the comment on .intakeButtonModifier() for more
                details on the intake buttons.
                The contents of the buttons are rotated depending on the
                orientation so that they are not upside down for certain
                orientations.
                 */
                else {
                    // INTAKE AMP BUTTON
                    Box(
                        modifier = Modifier.intakeButtonModifier(
                            actionType = Constants.ActionType.INTAKE_AMP,
                            borderColorBlue = Color(30, 20, 125),
                            borderColorRed = Color(125, 20, 20),
                            backgroundColorBlue = Color.Blue,
                            backgroundColorRed = Color.Red,
                            width = 7 * maxWidth / 20,
                            height = maxHeight / 2,
                            offsetX = 0.dp,
                            offsetY = 0.dp
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "INTAKE AMP:",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "$numActionIntakeAmp",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                        }
                    }
                    // INTAKE POACH BUTTON
                    Box(
                        modifier = Modifier.intakeButtonModifier(
                            actionType = Constants.ActionType.INTAKE_POACH,
                            borderColorBlue = Color(109, 20, 125),
                            borderColorRed = Color(109, 20, 125),
                            backgroundColorBlue = Color(218, 0, 255),
                            backgroundColorRed = Color(218, 0, 255),
                            width = 7 * maxWidth / 20,
                            height = maxHeight / 2,
                            offsetX = 0.dp,
                            offsetY = maxHeight / 2
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "INTAKE POACH:",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "$numActionIntakePoach",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                        }
                    }
                    // INTAKE CENTER BUTTON
                    Box(
                        modifier = Modifier.intakeButtonModifier(
                            actionType = Constants.ActionType.INTAKE_CENTER,
                            borderColorBlue = Color(255, 87, 34),
                            borderColorRed = Color(255, 87, 34),
                            backgroundColorBlue = Color(255, 152, 0),
                            backgroundColorRed = Color(255, 152, 0),
                            width = 3 * maxWidth / 10,
                            height = maxHeight,
                            offsetX = 7 * maxWidth / 20,
                            offsetY = 0.dp
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "INTAKE CENTER:",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$numActionIntakeCenter",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    // INTAKE FAR BUTTON
                    Box(
                        modifier = Modifier.intakeButtonModifier(
                            actionType = Constants.ActionType.INTAKE_FAR,
                            borderColorBlue = Color(125, 20, 20),
                            borderColorRed = Color(30, 20, 125),
                            backgroundColorBlue = Color.Red,
                            backgroundColorRed = Color.Blue,
                            width = 7 * maxWidth / 20,
                            height = maxHeight,
                            offsetX = 13 * maxWidth / 20,
                            offsetY = 0.dp
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "INTAKE FAR:",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                            Text(
                                text = "$numActionIntakeFar",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = if (collectionObjectiveActivity.isIncap) Color.Black else Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /*
    Returns a modifier for the intake buttons that sets the
    position, dimensions, color, and click event depending
    on the given action type, alliance color, and if they
    are incap or not.
    Refer to the comment on intakeButtonPress for more details
    on the click event.
     */
    @SuppressLint("ModifierFactoryUnreferencedReceiver")
    private fun Modifier.intakeButtonModifier(
        actionType: Constants.ActionType,
        borderColorBlue: Color,
        borderColorRed: Color,
        backgroundColorBlue: Color,
        backgroundColorRed: Color,
        width: Dp,
        height: Dp,
        offsetX: Dp,
        offsetY: Dp
    ): Modifier {
        return size(width, height)
            .offset(
                if (allianceColor == Constants.AllianceColor.BLUE) offsetX else -offsetX,
                offsetY
            )
            .clickable { intakeButtonPress(actionType = actionType) }
            .border(
                4.dp,
                if (collectionObjectiveActivity.isIncap) Color(142, 142, 142).copy(alpha = 0.6f)
                else if (allianceColor == AllianceColor.BLUE) borderColorBlue.copy(alpha = 0.6f)
                else borderColorRed.copy(alpha = 0.6f)
            )
            .background(
                if (collectionObjectiveActivity.isIncap) Color(239, 239, 239).copy(alpha = 0.6f)
                else if (allianceColor == AllianceColor.BLUE) backgroundColorBlue.copy(alpha = 0.6f)
                else backgroundColorRed.copy(alpha = 0.6f)
            )
            .rotate(if (orientation) 0f else 180f)
    }

    /*
    When an intake button is clicked, it calls this function.
    If they are not incap then:
    Adds the given action to the timeline, adds to the count for that action,
    then switches to scoring and enables buttons.
     */
    private fun intakeButtonPress(actionType: Constants.ActionType) {
        if (!collectionObjectiveActivity.isIncap) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (matchTimer != null) {
                    collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                    when (actionType) {
                        Constants.ActionType.INTAKE_AMP -> numActionIntakeAmp++
                        Constants.ActionType.INTAKE_FAR -> numActionIntakeFar++
                        Constants.ActionType.INTAKE_CENTER -> numActionIntakeCenter++
                        else -> numActionIntakePoach++
                    }
                    scoring = true
                    collectionObjectiveActivity.enableButtons()
                }
            }
        }
    }

    /*
    When a scoring button is clicked, it calls this function.
    If they are not incap and if either they aren't failing or they are
    pressing the speaker, or amp button, then:
    Adds the given action to the timeline, adds to the count for that action
    if they are not failing the score, then switches to intaking and
    enables buttons.
     */
    private fun scoringButtonPress(actionType: Constants.ActionType) {
        if (!collectionObjectiveActivity.isIncap) {
            if (
                !collectionObjectiveActivity.failing ||
                actionType == Constants.ActionType.SCORE_SPEAKER ||
                actionType == Constants.ActionType.SCORE_AMP ||
                actionType == Constants.ActionType.FERRY_SHOOT
            ) {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    if (matchTimer != null) {
                        collectionObjectiveActivity.timelineAddWithStage(action_type = actionType)
                        if (!collectionObjectiveActivity.failing) {
                            when (actionType) {
                                Constants.ActionType.FERRY_DRIVE -> numActionFerryDrive++
                                Constants.ActionType.FERRY_SHOOT -> numActionFerryShoot++
                                Constants.ActionType.DROP -> numActionDrop++
                                Constants.ActionType.SCORE_AMP -> numActionScoreAmp++
                                else -> numActionScoreSpeaker++
                            }
                        }
                        collectionObjectiveActivity.failing = false
                        scoring = false
                        collectionObjectiveActivity.enableButtons()
                    }
                }
            }
        }
    }
}