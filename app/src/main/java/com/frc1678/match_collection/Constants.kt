// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.app.Activity

// Contains constant values and enum classes.
class Constants {
    companion object {
        const val NONE_VALUE: String = "NONE"
        const val NUMBER_OF_ACTIVE_SCOUTS: Int = 18
        const val COMPRESSED_QR_TAG = "QR"
        const val PREVIOUS_SCREEN = "previous_screen"
        const val VERSION_NUMBER = "1.0.1"
        const val EVENT_KEY = "2024arc"

        /**
         * The previous activity that was visited before this one. This is found by looking for the
         * intent extra with key [PREVIOUS_SCREEN].
         */
        val Activity.previousScreen
            get() = intent.getSerializableExtra(PREVIOUS_SCREEN) as? Screens
    }

    /**
     * Every screen in the app.
     */
    enum class Screens {
        COLLECTION_OBJECTIVE,
        COLLECTION_SUBJECTIVE,
        MATCH_INFORMATION_INPUT,
        MATCH_INFORMATION_EDIT,
        MODE_COLLECTION_SELECT,
        QR_GENERATE,
        STARTING_POSITION_OBJECTIVE
    }

    enum class ModeSelection {
        SUBJECTIVE,
        OBJECTIVE,
        NONE
    }

    enum class AllianceColor {
        RED,
        BLUE,
        NONE
    }

    enum class StageLevel {
        N, // NONE
        F, // FAILED
        O, // ONSTAGE
    }

    enum class ActionType {
        SCORE_AMP,
        SCORE_SPEAKER,
        FAIL,
        DROP,
        FERRY,
        FERRY_SHOOT,
        FERRY_DRIVE,
        INTAKE_AMP,
        INTAKE_POACH,
        INTAKE_CENTER,
        INTAKE_FAR,
        SCORE_AMPLIFY,
        AUTO_INTAKE_SPIKE_1,
        AUTO_INTAKE_SPIKE_2,
        AUTO_INTAKE_SPIKE_3,
        AUTO_INTAKE_CENTER_1,
        AUTO_INTAKE_CENTER_2,
        AUTO_INTAKE_CENTER_3,
        AUTO_INTAKE_CENTER_4,
        AUTO_INTAKE_CENTER_5,
        AUTO_INTAKE_OTHER,
        START_INCAP,
        END_INCAP,
        TO_TELEOP,
        TO_ENDGAME,
        SCORE_TRAP
    }

    enum class Stage {
        AUTO,
        TELEOP,
        ENDGAME
    }

    enum class AssignmentMode {
        NONE,
        AUTOMATIC_ASSIGNMENT,
        OVERRIDE
    }
}
