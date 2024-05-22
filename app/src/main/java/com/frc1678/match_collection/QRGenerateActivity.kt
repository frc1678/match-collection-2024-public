// Copyright (c) 2023 FRC Team 1678: Citrus Circuits
package com.frc1678.match_collection

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frc1678.match_collection.Constants.Companion.PREVIOUS_SCREEN
import com.frc1678.match_collection.Constants.Companion.previousScreen
import com.frc1678.match_collection.objective.CollectionObjectiveActivity
import com.frc1678.match_collection.objective.StartingPositionObjectiveActivity
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.color.Color
import org.yaml.snakeyaml.Yaml
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.regex.Pattern
import kotlin.text.Regex.Companion.escape

// Activity to display QR code of data collected in the match.

class QRGenerateActivity : CollectionActivity() {
    // Define regex to validate that QR only contains acceptable characters.
    private var regex: Pattern = Pattern.compile("[A-Z0-9" + escape("$%*+-./#^ ") + "]+")

    // Read YAML schema file and return its contents as a HashMap.
    private fun schemaRead(context: Context): HashMap<String, HashMap<String, Any>> {
        val inputStream = context.resources.openRawResource(R.raw.match_collection_qr_schema)
        return Yaml().load(inputStream) as HashMap<String, HashMap<String, Any>>
    }

    // Write message to a text file in the specified directory.
    private fun writeToFile(fileName: String, message: String) {
        val file = BufferedWriter(
            FileWriter(
                "/storage/emulated/0/${Environment.DIRECTORY_DOWNLOADS}/$fileName.txt", false
            )
        )
        file.write("$message\n")
        file.close()
    }

    // Generate and display QR.
    @Composable
    fun DisplayQR(contents: String) {
        val renderOption = RenderOption()
        //Have a mutable state to remember the bitmap that is created with AwesomeQrRenderer
        var mutableState: ImageBitmap? by remember { mutableStateOf(null) }
        //Customizing the QR
        renderOption.content = contents
        renderOption.size = 800
        renderOption.borderWidth = 20
        renderOption.clearBorder = false
        renderOption.patternScale = 1F
        val color = Color()
        color.light = 0xFFFFFFFF.toInt() //For blank spaces
        color.dark = 0xFF000000.toInt() //For non-blank spaces
        color.background = 0xFFFFFFFF.toInt()
        renderOption.color = color
        //Render the QR
        LaunchedEffect(true) {
            AwesomeQrRenderer.renderAsync(renderOption, { result ->
                if (result.bitmap != null) {
                    mutableState = result.bitmap!!.asImageBitmap()
                }
            }, { exception ->
                exception.printStackTrace()
                //The callback function will print the error
            })
        }
        //Create the image composable if the mutable state isn't null
        mutableState?.let { Image(it, "") }

    }

    // Initialize proceed button to increment and store match number and return to
    // MatchInformationInputActivity.kt when clicked.

    private fun initProceedButton(isAlreadyCompressed: Boolean, qrContents: String) {
        if (!isAlreadyCompressed) {
            // Write compressed QR string to file.
            // File name is dependent on mode (objective or subjective).
            val fileName = if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
                "${matchNumber}_${teamNumber}_$timestamp"
            } else {
                "${matchNumber}_$timestamp"
            }
            writeToFile(fileName = fileName, message = qrContents)
            matchNumber += 1
        }
        putIntoStorage(context = this, key = "match_number", value = matchNumber)
        val prevScreenInput: Boolean = (previousScreen == Constants.Screens.MATCH_INFORMATION_INPUT)
        val intent = Intent(this, MatchInformationInputActivity::class.java).putExtra(
                "old_qr",
                prevScreenInput
            ).putExtra(PREVIOUS_SCREEN, Constants.Screens.QR_GENERATE).putExtras(intent)

        startActivity(
            intent
        )
    }

    /*
    Begin intent used in onKeyLongPress to go back to a previous activity depending
    on your mode and starting position.
     */
    private fun intentToPreviousActivity() {
        if (previousScreen != Constants.Screens.MATCH_INFORMATION_INPUT) {
            isTeleopActivated = true
            val intent = if (collectionMode == Constants.ModeSelection.OBJECTIVE) {
                if (startingPosition != 0) {
                    Intent(this, CollectionObjectiveActivity::class.java)
                } else {
                    Intent(this, StartingPositionObjectiveActivity::class.java)
                }
            } else {
                Intent(this, CollectionSubjectiveActivity::class.java).putExtras(intent)
            }.putExtra(PREVIOUS_SCREEN, Constants.Screens.QR_GENERATE)

            startActivity(
                intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            )
        } else {
            val intent = Intent(this, MatchInformationInputActivity::class.java).putExtra(
                    PREVIOUS_SCREEN,
                    Constants.Screens.QR_GENERATE
                ).putExtra("old_qr", true).putExtras(intent)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }

    // Restart app from MatchInformationInputActivity.kt when back button is long pressed.
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val newPressTime = System.currentTimeMillis()
            if (buttonPressedTime + 250 < newPressTime) {
                buttonPressedTime = newPressTime
                intentToPreviousActivity()
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timestamp = System.currentTimeMillis() / 1000
        val compressedQR = intent.extras?.getString(Constants.COMPRESSED_QR_TAG)

        setContent {
            if (compressedQR == null) {
                // Populate QR code content and display QR if valid (only contains compression characters).
                val qrContents = compress(schema = schemaRead(context = this))

                Row {
                    if (regex.matcher(qrContents).matches()) {
                        DisplayQR(contents = qrContents)
                    } else {
                        AlertDialog.Builder(this@QRGenerateActivity).setMessage(R.string.error_qr)
                            .show()
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Match: $matchNumber",
                            fontSize = 25.sp,
                            modifier = Modifier.padding(top = 60.dp, bottom = 5.dp)
                        )
                        when (collectionMode) {
                            Constants.ModeSelection.OBJECTIVE -> Text(
                                text = "Team Number: $teamNumber", fontSize = 25.sp
                            )

                            else -> Text(
                                text = "Team Numbers:\n" + "${intent.extras?.getString("team_one")}, " + "${
                                    intent.extras?.getString(
                                        "team_two"
                                    )
                                }, " + "${intent.extras?.getString("team_three")}",
                                fontSize = 25.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Box(modifier = Modifier.padding(100.dp, 70.dp)) {
                            Button(
                                onClick = { initProceedButton(false, qrContents) },
                                // change button color to light gray
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = androidx.compose.ui.graphics.Color.LightGray
                                ),
                                // make the button a square and align it in the center of the space
                                modifier = Modifier
                                    .size(width = 250.dp, height = 250.dp)
                                    .align(Alignment.Center)
                                    .background(color = androidx.compose.ui.graphics.Color.LightGray)
                            ) {
                                Text(text = "PROCEED", fontSize = 20.sp, color = Black)
                            }
                        }
                    }
                }
            } else {
                Row {
                    DisplayQR(compressedQR)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Match: ${intent.extras?.getString("qr_match_num")}",
                            color = androidx.compose.ui.graphics.Color.Magenta,
                            fontSize = 25.sp,
                            modifier = Modifier.padding(top = 60.dp, bottom = 5.dp)
                        )
                        when (collectionMode) {
                            Constants.ModeSelection.OBJECTIVE -> Text(
                                text = "Team Number: ${
                                    intent.extras?.getString(
                                        "qr_team_num"
                                    )
                                }"
                            )

                            else -> {
                                val teamData = compressedQR.substringAfter("%").split("#")
                                Text(
                                    text = "Team Numbers:\n" + teamData[0].substringBefore("$")
                                        .substringAfter("A") + ", " + teamData[1].substringBefore("$")
                                        .substringAfter("A") + ", " + teamData[2].substringBefore("$")
                                        .substringAfter("A"),
                                    fontSize = 25.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Box(modifier = Modifier.padding(100.dp, 70.dp)) {
                            Button(
                                onClick = { initProceedButton(true, qrContents = "") },
                                // change button color to light gray
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = androidx.compose.ui.graphics.Color.LightGray
                                ),
                                // make the button a square and align it in the center of the space
                                modifier = Modifier
                                    .size(width = 250.dp, height = 250.dp)
                                    .align(Alignment.Center)
                                    .background(color = androidx.compose.ui.graphics.Color.LightGray)
                            ) {
                                Text(text = "PROCEED", fontSize = 20.sp, color = Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

