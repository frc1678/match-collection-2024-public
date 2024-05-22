// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection.objective

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.frc1678.match_collection.CollectionActivity
import com.frc1678.match_collection.Constants
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Companion.previousScreen
import com.frc1678.match_collection.MatchInformationEditActivity
import com.frc1678.match_collection.PreloadedFragment
import com.frc1678.match_collection.R
import com.frc1678.match_collection.TimerUtility
import com.frc1678.match_collection.UndoRedoFragment
import com.frc1678.match_collection.allianceColor
import com.frc1678.match_collection.assignMode
import com.frc1678.match_collection.autoIntakeList
import com.frc1678.match_collection.buttonPressedTime
import com.frc1678.match_collection.isMatchTimeEnded
import com.frc1678.match_collection.isTeleopActivated
import com.frc1678.match_collection.matchTime
import com.frc1678.match_collection.matchTimer
import com.frc1678.match_collection.numActionDrop
import com.frc1678.match_collection.numActionFail
import com.frc1678.match_collection.numActionFerry
import com.frc1678.match_collection.numActionFerryDrive
import com.frc1678.match_collection.numActionFerryShoot
import com.frc1678.match_collection.numActionIntakeAmp
import com.frc1678.match_collection.numActionIntakeCenter
import com.frc1678.match_collection.numActionIntakeFar
import com.frc1678.match_collection.numActionIntakeOther
import com.frc1678.match_collection.numActionIntakePoach
import com.frc1678.match_collection.numActionScoreAmp
import com.frc1678.match_collection.numActionScoreAmplify
import com.frc1678.match_collection.numActionScoreSpeaker
import com.frc1678.match_collection.numActionScoreTrap
import com.frc1678.match_collection.preloaded
import com.frc1678.match_collection.resetCollectionReferences
import com.frc1678.match_collection.scoring
import com.frc1678.match_collection.stageCenterLevel
import com.frc1678.match_collection.stageLeftLevel
import com.frc1678.match_collection.stageRightLevel
import com.frc1678.match_collection.teamNumber
import com.frc1678.match_collection.timeline
import kotlinx.android.synthetic.main.collection_objective_activity.*

/**
 * Activity for Objective Match Collection to scout the objective gameplay of a single team in a
 * match.
 */
class CollectionObjectiveActivity : CollectionActivity() {

    /**
     * Determines which scoring/intake screen should be displayed based on the current mode
     * (Auto, Teleop, or Endgame)
     */
    var fragmentScreen = Constants.Stage.AUTO
        set(value) {
            field = value
            /* Set the current fragment to scoring or intake depending on the new value,
            if teleop is activated set intakePanel. If auto, set intakeAutoPanel */
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_frame,
                when (value) {
                    Constants.Stage.AUTO -> autoPanel
                    Constants.Stage.TELEOP -> teleopPanel
                    else -> endgamePanel
                }
            ).commit()
            enableButtons()
        }

    /**
     * Determines if the change preload fragment or undo/redo fragment should be displayed
     */
    var topFragment = true
        set(value) {
            field = value
            /* Set the current fragment to scoring or intake depending on the new value,
            if teleop is activated set intakePanel. If auto, set intakeAutoPanel */
            supportFragmentManager.beginTransaction().replace(
                R.id.undo_redo_btn_frame,
                when (value) {
                    true -> preloadedFragment
                    else -> undoRedoFragment
                }
            ).commit()
        }

    /**
     * The fragment with the map and buttons for teleop.
     */
    private val teleopPanel = TeleopFragment()

    /**
     * The fragment with the endgame buttons.
     */
    private val endgamePanel = EndgameFragment()

    /**
     * The fragment with the map and buttons for auto
     */
    private val autoPanel = AutoFragment()

    /**
     * The fragment with the button for changing the preload
     */
    private val preloadedFragment = PreloadedFragment()

    /**
     * The fragment with the undo and redo buttons
     */
    private val undoRedoFragment = UndoRedoFragment()

    /**
     * True if the match timer is running or the timer has ended.
     */
    var isTimerRunning by mutableStateOf(false)

    /**
     * Whether the robot is currently incap.
     */
    var isIncap by mutableStateOf(false)

    var removedTimelineActions = mutableListOf<Map<String, String>>()

    var failing by mutableStateOf(false)

    /**
     * Set timer to start match when timer is started or reset.
     * Resets all actions and resets the timeline
     */
    private fun timerReset() {
        resetCollectionReferences()
        isTeleopActivated = false
        isTimerRunning = false
        matchTimer?.cancel()
        matchTimer = null
        timeline.clear()
        isIncap = false
        removedTimelineActions.clear()
        btn_timer.text = getString(R.string.btn_timer_start)
        isMatchTimeEnded = false
        fragmentScreen = Constants.Stage.AUTO
        scoring = preloaded
        failing = false
    }

    /**
     * Add performed action to timeline, including action type and time of action.
     */
    private fun timelineAdd(matchTime: String, actionType: Constants.ActionType) {
        timeline.add(mapOf("match_time" to matchTime, "action_type" to "$actionType"))
        removedTimelineActions.clear()
        enableButtons()
    }

    /**
     * If stage and time contradict when action is recorded, add action to timeline with time value
     * dictated by stage.
     */
    fun timelineAddWithStage(action_type: Constants.ActionType) = when {
        !isTeleopActivated and (matchTime.toInt() < getString(R.string.final_auto_time).toInt()) -> {
            timelineAdd(
                matchTime = getString(R.string.final_auto_time),
                actionType = action_type
            )
        }

        isTeleopActivated and (matchTime.toInt() > getString(R.string.initial_teleop_time).toInt()) -> {
            timelineAdd(
                matchTime = getString(R.string.initial_teleop_time),
                actionType = action_type
            )
        }

        else -> timelineAdd(matchTime = matchTime, actionType = action_type)
    }

    /**
     * Remove previously inputted action from timeline.
     */
    fun timelineRemove() {
        //Decrement action values displayed on action counters.
        if (timeline.isNotEmpty()) {
            when (timeline.last()["action_type"].toString()) {
                /* Removes auto intake and scoring screen in timeline for specific
                game piece when undo button is used */
                Constants.ActionType.AUTO_INTAKE_SPIKE_1.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(0, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_SPIKE_2.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(1, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_SPIKE_3.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(2, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_1.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(3, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_2.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(4, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_3.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(5, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_4.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(6, false) }
                    scoring = false
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_5.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(7, false) }
                    scoring = false
                }

                Constants.ActionType.SCORE_AMP.toString() -> {
                    if ((
                        timeline.size >= 2 &&
                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.size < 2) numActionScoreAmp--
                    scoring = true
                    failing = false
                }

                Constants.ActionType.SCORE_SPEAKER.toString() -> {
                    if ((
                        timeline.size >= 2 &&
                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.size < 2) numActionScoreSpeaker--
                    scoring = true
                    failing = false
                }

                Constants.ActionType.FAIL.toString() -> {
                    numActionFail--
                    failing = false
                }

                Constants.ActionType.DROP.toString() -> {
                    numActionDrop--
                    scoring = true
                }

                // Auto ferry
                Constants.ActionType.FERRY.toString() -> {
                    if ((
                        timeline.size >= 2 &&
                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.size < 2) numActionFerry--
                    scoring = true
                    failing = false
                }

                // Ferry shoot
                Constants.ActionType.FERRY_SHOOT.toString() -> {
                    if ((
                                timeline.size >= 2 &&
                                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2) numActionFerryShoot--
                    scoring = true
                    failing = false
                }

                // Ferry drive
                Constants.ActionType.FERRY_DRIVE.toString() -> {
                    if ((
                                timeline.size >= 2 &&
                                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.size < 2) numActionFerryDrive--
                    scoring = true
                    failing = false
                }

                Constants.ActionType.INTAKE_AMP.toString() -> {
                    numActionIntakeAmp--
                    scoring = false
                }

                Constants.ActionType.INTAKE_POACH.toString() -> {
                    numActionIntakePoach--
                    scoring = false
                }

                Constants.ActionType.INTAKE_CENTER.toString() -> {
                    numActionIntakeCenter--
                    scoring = false
                }

                Constants.ActionType.INTAKE_FAR.toString() -> {
                    numActionIntakeFar--
                    scoring = false
                }

                Constants.ActionType.SCORE_AMPLIFY.toString() -> {
                    if ((
                        timeline.size >= 2 &&
                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.size < 2) numActionScoreAmplify--
                    scoring = true
                    failing = false
                }

                Constants.ActionType.START_INCAP.toString() -> {
                    tb_incap.isChecked = false
                    isIncap = false
                }
                Constants.ActionType.END_INCAP.toString() -> {
                    tb_incap.isChecked = true
                    isIncap = true
                }

                Constants.ActionType.TO_TELEOP.toString() -> {
                    fragmentScreen = Constants.Stage.AUTO
                    isTeleopActivated = false
                }
                Constants.ActionType.TO_ENDGAME.toString() -> {
                    fragmentScreen = Constants.Stage.TELEOP
                }

                Constants.ActionType.SCORE_TRAP.toString() -> {
                    if ((
                        timeline.size >= 2 &&
                        timeline[timeline.size-2]["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.size < 2) numActionScoreTrap--
                    scoring = true
                    failing = false
                }
            }

            // Add removed action to removedTimelineActions, so it can be redone if needed.
            removedTimelineActions.add(timeline.last())

            // Remove most recent timeline entry.
            timeline.removeAt(timeline.lastIndex)
            enableButtons()

            // If the last action in the timeline is a fail, undo again
            if (timeline.isNotEmpty()) if (timeline.last()["action_type"].toString() == Constants.ActionType.FAIL.toString()) timelineRemove()
        }
    }

    /**
     * Redoes timeline actions after undo.
     */
    fun timelineReplace() {
        if (removedTimelineActions.isNotEmpty()) {
            // Increment action values and display on action counters if re-adding a counter action from the timeline.
            when (removedTimelineActions.last()["action_type"].toString()) {
                Constants.ActionType.AUTO_INTAKE_SPIKE_1.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(0, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_SPIKE_2.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(1, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_SPIKE_3.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(2, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_1.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(3, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_2.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(4, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_3.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(5, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_4.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(6, true) }
                    scoring = true
                }

                Constants.ActionType.AUTO_INTAKE_CENTER_5.toString() -> {
                    autoIntakeList = autoIntakeList.toMutableList().apply { set(7, true) }
                    scoring = true
                }

                Constants.ActionType.SCORE_AMP.toString() -> {
                    if ((
                        timeline.isNotEmpty() &&
                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.isEmpty()) numActionScoreAmp++
                    scoring = false
                    failing = false
                }

                Constants.ActionType.SCORE_SPEAKER.toString() -> {
                    if ((
                        timeline.isNotEmpty() &&
                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.isEmpty()) numActionScoreSpeaker++
                    scoring = false
                    failing = false
                }

                Constants.ActionType.FERRY_SHOOT.toString() -> {
                    if ((
                                timeline.isNotEmpty() &&
                                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                                ) || timeline.isEmpty()) numActionFerryShoot++
                    scoring = false
                    failing = false
                }

                Constants.ActionType.FERRY_DRIVE.toString() -> {
                    numActionFerryDrive++
                    scoring = false
                }

                Constants.ActionType.FAIL.toString() -> {
                    numActionFail++
                    failing = true
                }

                Constants.ActionType.DROP.toString() -> {
                    numActionDrop++
                    scoring = false
                }

                Constants.ActionType.FERRY.toString() -> {
                    if ((
                        timeline.isNotEmpty() &&
                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.isEmpty()) numActionFerry++
                    scoring = false
                    failing = false
                }

                Constants.ActionType.INTAKE_AMP.toString() -> {
                    numActionIntakeAmp++
                    scoring = true
                }

                Constants.ActionType.INTAKE_POACH.toString() -> {
                    numActionIntakePoach++
                    scoring = true
                }

                Constants.ActionType.INTAKE_CENTER.toString() -> {
                    numActionIntakeCenter++
                    scoring = true
                }

                Constants.ActionType.INTAKE_FAR.toString() -> {
                    numActionIntakeFar++
                    scoring = true
                }

                Constants.ActionType.SCORE_AMPLIFY.toString() -> {
                    if ((
                        timeline.isNotEmpty() &&
                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.isEmpty()) numActionScoreAmplify++
                    scoring = false
                    failing = false
                }

                Constants.ActionType.START_INCAP.toString() -> {
                    tb_incap.isChecked = true
                    isIncap = true
                }

                Constants.ActionType.END_INCAP.toString() -> {
                    tb_incap.isChecked = false
                    isIncap = false
                }

                Constants.ActionType.TO_TELEOP.toString() -> {
                    fragmentScreen = Constants.Stage.TELEOP
                    isTeleopActivated = true
                }

                Constants.ActionType.TO_ENDGAME.toString() -> {
                    fragmentScreen = Constants.Stage.ENDGAME
                }

                Constants.ActionType.SCORE_TRAP.toString() -> {
                    if ((
                        timeline.isNotEmpty() &&
                        timeline.last()["action_type"] != Constants.ActionType.FAIL.toString()
                    ) || timeline.isEmpty()) numActionScoreTrap++
                    scoring = false
                    failing = false
                }
            }

            // Add most recently undone action from removedTimelineActions back to timeline.
            timeline.add(removedTimelineActions.last())

            // Remove the redone action from removedTimelineActions.
            removedTimelineActions.removeAt(removedTimelineActions.lastIndex)
            enableButtons()

            // If the last action in the timeline is a fail, redo again
            if (timeline.isNotEmpty()) if (timeline.last()["action_type"].toString() == Constants.ActionType.FAIL.toString()) timelineReplace()
        }
    }

    /**
     * Enable and disable buttons based on actions in timeline and timer stage. If in teleop, enable [teleopPanel];
     * if teleop is not activated, enable [autoPanel]
     */
    fun enableButtons() {
        // Determines if the change preload button should still be shown
        topFragment =
            when (timeline.size) {
                1 -> timeline.last()["action_type"].toString() == Constants.ActionType.TO_TELEOP.toString()
                2 -> timeline.last()["action_type"].toString() == Constants.ActionType.TO_ENDGAME.toString()
                else -> timeline.isEmpty()
            }

        // Updates the undo and redo buttons if necessary
        if (!topFragment) undoRedoFragment.enableButtons()

        // Enables the incap toggle button if not in auto, not recording a failed score, and the match hasn't ended
        tb_incap.isEnabled = fragmentScreen != Constants.Stage.AUTO && !isMatchTimeEnded &&
            !failing && !(stageRightLevel == Constants.StageLevel.O ||
            stageLeftLevel == Constants.StageLevel.O || stageCenterLevel == Constants.StageLevel.O)

        // Updates the text on the amplify/intake other button
        btn_action_intakeOther_amplify.text = if(fragmentScreen == Constants.Stage.AUTO) {
            getString(R.string.btn_action_intake_other, numActionIntakeOther.toString())
        } else {
            getString(R.string.btn_action_amplify, numActionScoreAmplify.toString())
       }

        // Updates the text on the failed button
        btn_action_failed.text = getString(R.string.btn_action_failed, numActionFail.toString())

        // Enables the button timer if no buttons have been pressed and a popup isn't open
        btn_timer.isEnabled = timeline.size <= 0

        // Enables the fail button if not incap and not already recording a failed score
        btn_action_failed.isEnabled = !isIncap && !failing && scoring && (isTimerRunning || isMatchTimeEnded)
        if (fragmentScreen == Constants.Stage.ENDGAME && !scoring) {
            btn_action_failed.isEnabled = false
        }

        // Enables the score amplify button if not incap and scoring and on the teleop screen
        btn_action_intakeOther_amplify.isEnabled =
            (isTimerRunning || isMatchTimeEnded) &&
            (
                (!isIncap && scoring && fragmentScreen == Constants.Stage.TELEOP) ||
                (!scoring && fragmentScreen == Constants.Stage.AUTO)
            )

        // Enables the proceed button if the match has started or is over and if not failing.
        btn_proceed_edit.isEnabled = (isTimerRunning || isMatchTimeEnded) && !failing

        /*
        Sets the proceed button text to "To Teleop" if it's auto, to "To Endgame" if it's teleop
        and otherwise sets it to "Proceed"
         */
        btn_proceed_edit.text = when (fragmentScreen) {
            Constants.Stage.AUTO -> getString(R.string.btn_to_teleop)
            Constants.Stage.TELEOP -> getString(R.string.btn_to_endgame)
            else -> getString(R.string.btn_proceed)
        }
    }

    /**
     * Ends incap if still activated at end of the match.
     */
    fun endAction() {
        if (tb_incap.isChecked) {
            tb_incap.isChecked = false
            isIncap = false
            timelineAdd(matchTime = matchTime, actionType = Constants.ActionType.END_INCAP)
        }
    }

    /**
     * Initialize button and toggle button `onClickListeners`.
     */
    @SuppressLint("SuspiciousIndentation")
    private fun initOnClicks() {
        // When clicked, switches to next screen
        btn_proceed_edit.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                // Switches the screen from auto to teleop to endgame
                if (fragmentScreen == Constants.Stage.AUTO) {
                    fragmentScreen = Constants.Stage.TELEOP
                    timelineAdd(matchTime, Constants.ActionType.TO_TELEOP)
                    enableButtons()
                    isTeleopActivated = true
                    objective_match_collection_layout.setBackgroundColor(Color.WHITE)
                }
                else if (fragmentScreen == Constants.Stage.TELEOP) {
                    fragmentScreen = Constants.Stage.ENDGAME
                    enableButtons()
                    timelineAdd(matchTime, Constants.ActionType.TO_ENDGAME)
                }
                else {
                    /* If you are in Override and the match has not finished and currently in Teleop mode,
                    brings up an alert that confirms you want to proceed, then moves onto MatchInformationEdit */
                    if (!isMatchTimeEnded && assignMode == Constants.AssignmentMode.OVERRIDE) {
                        AlertDialog.Builder(this)
                            .setMessage(R.string.warning_proceed_override)
                            .setPositiveButton("Yes") { _, _ ->
                                matchTimer?.cancel()
                                startActivity(
                                    Intent(this, MatchInformationEditActivity::class.java).putExtra(
                                        PREVIOUS_SCREEN,
                                        Constants.Screens.COLLECTION_OBJECTIVE
                                    ),
                                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                                )
                            }
                            .show()
                    } else if (isMatchTimeEnded) {
                        endAction()
                        val intent = Intent(this, MatchInformationEditActivity::class.java)
                            .putExtra(PREVIOUS_SCREEN, Constants.Screens.COLLECTION_OBJECTIVE)
                            startActivity(
                                intent, ActivityOptions.makeSceneTransitionAnimation(
                                    this,
                                    btn_proceed_edit, "proceed_button"
                                ).toBundle()
                            )
                    }
                }
            }
        }
        // When long clicked, switches to previous screen
        btn_proceed_edit.setOnLongClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                // Switches the screen from endgame to teleop to auto
                if (timeline.isNotEmpty()) {
                    if (fragmentScreen == Constants.Stage.TELEOP && timeline.last()["action_type"] == Constants.ActionType.TO_TELEOP.toString()) {
                        fragmentScreen = Constants.Stage.AUTO
                        enableButtons()
                        isTeleopActivated = false
                        timelineRemove()
                    } else if (
                        (
                            timeline.last()["action_type"] == Constants.ActionType.TO_ENDGAME.toString() ||
                            timeline.last()["action_type"] == Constants.ActionType.SCORE_TRAP.toString() ||
                            timeline.last()["action_type"] == Constants.ActionType.END_INCAP.toString()
                        ) &&
                        stageCenterLevel != Constants.StageLevel.O &&
                        stageLeftLevel != Constants.StageLevel.O &&
                        stageRightLevel != Constants.StageLevel.O
                    ) {
                        fragmentScreen = Constants.Stage.TELEOP
                        enableButtons()
                        timeline.removeIf { it["action_type"] == Constants.ActionType.TO_ENDGAME.toString() }
                    }
                }
            }
            return@setOnLongClickListener true
        }

        // Start timer on normal click if timer is not running.
        btn_timer.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if (!isTimerRunning) {
                    TimerUtility.MatchTimerThread().initTimer(
                        context = this,
                        btn_timer = btn_timer,
                        btn_proceed = btn_proceed_edit,
                        layout = objective_match_collection_layout
                    )
                    isTimerRunning = true
                    enableButtons()
                    btn_proceed_edit.isEnabled = true
                }
            }
        }

        // Reset timer on long click if timer is running.
        btn_timer.setOnLongClickListener(
            View.OnLongClickListener {
                val newPressTime = System.currentTimeMillis()
                if (buttonPressedTime + 250 < newPressTime) {
                    buttonPressedTime = newPressTime
                    if ((isTimerRunning && fragmentScreen == Constants.Stage.AUTO) or isMatchTimeEnded) {
                        fragmentScreen = Constants.Stage.AUTO
                        timerReset()
                        timeline = ArrayList()
                        isTimerRunning = false
                        enableButtons()
                        btn_proceed_edit.isEnabled = false
                        objective_match_collection_layout.setBackgroundColor(Color.WHITE)
                    }
                }
                return@OnLongClickListener true
            }
        )

        /*
        Start incap if clicking the incap toggle button checks the toggle button.
        Otherwise, end incap.
         */
        tb_incap.setOnClickListener {
            if (!isMatchTimeEnded) {
                if (tb_incap.isChecked) {
                    timelineAdd(matchTime = matchTime, actionType = Constants.ActionType.START_INCAP)
                    isIncap = true
                    enableButtons()
                } else {
                    timelineAdd(matchTime = matchTime, actionType = Constants.ActionType.END_INCAP)
                    isIncap = false
                    enableButtons()
                }
            } else {
                tb_incap.isChecked = false
                isIncap = false
                tb_incap.isEnabled = false
            }
        }

        /*
        Adds a score in the amplified speaker to the timeline, then
        increases the count if not failing the score, then switches
        to intaking and enables buttons.
         */
        btn_action_intakeOther_amplify.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                if(fragmentScreen == Constants.Stage.AUTO) {
                    timelineAddWithStage(action_type = Constants.ActionType.AUTO_INTAKE_OTHER)
                    numActionIntakeOther++
                    scoring = true
                    enableButtons()
                } else {
                    timelineAddWithStage(action_type = Constants.ActionType.SCORE_AMPLIFY)
                    if (!failing) numActionScoreAmplify++
                    scoring = false
                    failing = false
                    enableButtons()
                }
            }
        }

        // Adds a fail to the timeline, sets failing to true, increments the count, and enables buttons
        btn_action_failed.setOnClickListener {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                timelineAddWithStage(action_type = Constants.ActionType.FAIL)
                numActionFail++
                failing = true
                enableButtons()
            }
        }
    }

    /**
     * Set team number view to team number defined in `References` and set team number to alliance
     * color.
     */
    private fun initTeamNum() {
        tv_team_number.text = teamNumber

        if (allianceColor == Constants.AllianceColor.RED) {
            tv_team_number.setTextColor(resources.getColor(R.color.alliance_red_light, null))
        }
        else {
            tv_team_number.setTextColor(resources.getColor(R.color.alliance_blue_light, null))
        }
    }

    /**
     * Initialize intent used in [onKeyLongPress] to restart app from
     * [StartingPositionObjectiveActivity].
     */
    private fun intentToPreviousActivity() {
        timerReset()
        startActivity(
            Intent(this, StartingPositionObjectiveActivity::class.java)
                .putExtra(PREVIOUS_SCREEN, Constants.Screens.COLLECTION_OBJECTIVE),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
    }

    /**
     * Resets and enables everything if the user entered this screen by pressing the back button.
     */
    private fun comingBack() {
        isTimerRunning = false
        isMatchTimeEnded = true
        isTeleopActivated = true
        btn_proceed_edit.text = getString(R.string.btn_proceed)
        btn_proceed_edit.isEnabled = true
        btn_timer.isEnabled = false
        btn_timer.text = getString(R.string.timer_run_down)
        tb_incap.isEnabled = false
        fragmentScreen = Constants.Stage.ENDGAME
        enableButtons()
        topFragment = timeline.last()["action_type"].toString() == Constants.ActionType.TO_ENDGAME.toString() && timeline.size == 2
    }

    /**
     * Restart app from [StartingPositionObjectiveActivity] when back button is long pressed.
     */
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                AlertDialog.Builder(this).setMessage(R.string.error_back_reset)
                    .setPositiveButton("Yes") { _, _ -> intentToPreviousActivity() }
                    .show()
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.collection_objective_activity)
        fragmentScreen = fragmentScreen
        topFragment = topFragment

        if (previousScreen != Constants.Screens.MATCH_INFORMATION_EDIT && previousScreen != Constants.Screens.QR_GENERATE) timerReset()
        else comingBack()

        enableButtons()
        initOnClicks()
        initTeamNum()
    }
}
