package com.simpledataflow.facedetector

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    // Dataflow tutorial

    // Connect to Firebase with Assistant (create a firebase project) -> check Firebase console
    // Connect to MLKit
    // Add face detector model dependency
    // Auto-download ML model (in Manifest)

    // Detect Images without camera (use logs)
    // Import image to drawables
    // Detect face
    // Draw

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        detectFace()
    }

    private fun detectFace() {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()

        val bitmap = BitmapFactory.decodeResource(
            this.applicationContext.resources,
            R.drawable.face // make sure the image is not big
        )

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        // detecting image will take a while (only for this project) because images itself were artificial.
        // if you use android camera it will fast real time
        detector.detectInImage(image)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                Log.d("sdf", faces.toString())
                draw(faces[0], bitmap)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Log.d("sdf", e.toString())
            }
    }

    private fun draw(
        face: FirebaseVisionFace,
        myBitmap: Bitmap
    ) {
        //Create a new image bitmap and attach a brand new canvas to it
        val tempBitmap =
            Bitmap.createBitmap(myBitmap.width, myBitmap.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(tempBitmap)

        canvas.drawBitmap(myBitmap, 0f, 0f, null)

        // init (if you use an android camera and detecting faces continuously then don't create Paint objects every time)
        val selectedColor = Color.RED
        val facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        val boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH

        // Draws a circle at the position of the detected face, with the face's track id below.
        val x = face.boundingBox.centerX().toFloat()
        val y = face.boundingBox.centerY().toFloat()

        // Draws a bounding box around the face.
        val xOffset = face.boundingBox.width() / 2.0f
        val yOffset = face.boundingBox.height() / 2.0f
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        canvas.drawRect(left, top, right, bottom, boxPaint)

        val contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS)
        for (point in contour.points) {
            val px = point.x
            val py = point.y
            canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
        }

        //Attach the canvas to the ImageView
        iv_face.setImageDrawable(BitmapDrawable(resources, tempBitmap))
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 10.0f
        private const val BOX_STROKE_WIDTH = 15.0f
    }
}
