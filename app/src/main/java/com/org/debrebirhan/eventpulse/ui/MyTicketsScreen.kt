package com.org.debrebirhan.eventpulse.ui

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.org.debrebirhan.eventpulse.navigation.BottomNavItem
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel
import java.io.File
import java.io.FileOutputStream

fun generateQRCode(content: String): Bitmap? {

    return try {

        val writer = QRCodeWriter()

        val bitMatrix = writer.encode(
            content,
            BarcodeFormat.QR_CODE,
            512,
            512
        )

        val width = bitMatrix.width
        val height = bitMatrix.height

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        for (x in 0 until width) {
            for (y in 0 until height) {

                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y])
                        android.graphics.Color.BLACK
                    else
                        android.graphics.Color.WHITE
                )
            }
        }

        bitmap

    } catch (e: Exception) {

        null
    }
}

fun saveTicketAsImage(
    context: Context,
    eventName: String,
    ticketId: String
) {

    val width = 1000
    val height = 500

    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = android.graphics.Color.WHITE

    canvas.drawRoundRect(
        RectF(0f, 0f, 650f, 500f),
        40f,
        40f,
        paint
    )

    paint.color = android.graphics.Color.parseColor("#D35400")

    canvas.drawRoundRect(
        RectF(650f, 0f, 1000f, 500f),
        40f,
        40f,
        paint
    )

    canvas.drawRect(650f, 0f, 680f, 500f, paint)

    paint.color = android.graphics.Color.BLACK
    paint.textSize = 50f

    paint.typeface =
        Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

    canvas.drawText(eventName, 80f, 100f, paint)

    generateQRCode("Ticket:$ticketId")?.let {

        val scaledQr =
            Bitmap.createScaledBitmap(
                it,
                250,
                250,
                false
            )

        canvas.drawBitmap(
            scaledQr,
            200f,
            160f,
            paint
        )
    }

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 35f
    paint.textAlign = Paint.Align.CENTER

    canvas.drawText(
        "EVENT PULSE",
        825f,
        220f,
        paint
    )

    try {

        val directory =
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )

        val file = File(
            directory,
            "EventPulse_${eventName.replace(" ", "_")}.jpg"
        )

        val out = FileOutputStream(file)

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            out
        )

        out.flush()
        out.close()

        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)
            )
        )

        Toast.makeText(
            context,
            "Ticket saved to Gallery",
            Toast.LENGTH_LONG
        ).show()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {

                setDataAndType(uri, "image/jpeg")

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }
        )

    } catch (e: Exception) {

        Toast.makeText(
            context,
            "Error: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    navController: NavController,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel
) {

    val myTickets by eventViewModel.myTickets

    val userId =
        authViewModel.currentUserId ?: ""

    LaunchedEffect(userId) {

        if (userId.isNotEmpty()) {

            eventViewModel.fetchMyTickets(userId)
        }
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {
                    Text(
                        "My Tickets",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = {

                            navController.navigate(
                                BottomNavItem.Home.route
                            ) {

                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,

                            contentDescription =
                                "Back to Home"
                        )
                    }
                }
            )
        }

    ) { padding ->

        if (myTickets.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    "No tickets found",
                    color = Color.Gray
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                items(myTickets) { ticket ->

                    TicketItem(
                        eventName = ticket.eventTitle,
                        ticketId = ticket.bookingId,

                        onDelete = {

                            eventViewModel.deleteTicket(
                                ticket.bookingId
                            )

                            Toast.makeText(
                                navController.context,
                                "Ticket Deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TicketItem(
    eventName: String,
    ticketId: String,
    onDelete: () -> Unit
) {

    val context = LocalContext.current

    val eventPulseOrange =
        Color(0xFFD35400)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),

        shape = RoundedCornerShape(16.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column {

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .background(Color.White)
                        .padding(12.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text = eventName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    val qrBitmap =
                        remember(ticketId) {

                            generateQRCode(
                                "Ticket:$ticketId"
                            )
                        }

                    qrBitmap?.let {

                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",

                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,

                            tint = eventPulseOrange,

                            modifier =
                                Modifier.size(14.dp)
                        )

                        Text(
                            text = " Download Ticket",
                            fontSize = 10.sp,
                            color = eventPulseOrange
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .background(eventPulseOrange),

                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "EVENT PULSE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Button(
                    onClick = {

                        saveTicketAsImage(
                            context,
                            eventName,
                            ticketId
                        )
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            eventPulseOrange
                    )
                ) {

                    Icon(
                        Icons.Default.Download,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("Download")
                }

                Button(
                    onClick = onDelete,

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {

                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text("Delete")
                }
            }
        }
    }
}