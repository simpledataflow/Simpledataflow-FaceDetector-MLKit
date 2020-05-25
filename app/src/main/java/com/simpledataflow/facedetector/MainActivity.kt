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
    // Connect to MLKit + manually add dependecies to gradle
    // Add face detector model dependency
    // Auto-download ML model (in Manifest)

    // Detect Images
    // Import image to drawables
    // Detect face
    // Draw

    // You can attach camera to face detector the same way we did in different tutorial: https://simpledataflow.com/dataflow-tutorial-build-not-hotdog-android-app-from-silicon-valley-tv-series-with-machine-learning/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        detectFace()
    }

    private fun detectFace() {
        // create a Bitmap object (programmatic representation of an image) from a drawable
        val bitmap = BitmapFactory.decodeResource(
            this.applicationContext.resources,
            R.drawable.face// make sure the image is not big
        )

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // if you use an android camera and detecting faces continuously then don't create options object every time
        // create an options objects, which specifies that we need faster analysis of an image and we want to detect all countours of the face
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()

        // create a detector object to analyze the image
        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

        // detecting image will take a while (only for this project) because images itself were artificial.
        // if you use android camera it will fast real time
        // analyze the image
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
        // We need to do 4 things:
        // - create a canvas so we can draw on it
        // - draw original image on the canvas
        // - draw contours on the canvas
        // - set canvas on the ImageView in the UI as a drawable

        // 1. create a canvas
        val tempBitmap =
            Bitmap.createBitmap(myBitmap.width, myBitmap.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(tempBitmap)

        // 2. draw original image on the canvas
        canvas.drawBitmap(myBitmap, 0f, 0f, null)

        // 3. draw contours on the canvas
        // 3.1. create colors and stokes for drawing
        val selectedColor = Color.RED
        val facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        val boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH

        // 3.2. Get center x and y coordinates of the face
        val x = face.boundingBox.centerX().toFloat()
        val y = face.boundingBox.centerY().toFloat()

        // 3.3. Draws a bounding box around the face.
        val xOffset = face.boundingBox.width() / 2.0f
        val yOffset = face.boundingBox.height() / 2.0f
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        canvas.drawRect(left, top, right, bottom, boxPaint)

        // 3.4. Draws contours
        val contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS)
        for (point in contour.points) {
            val px = point.x
            val py = point.y
            canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint)
        }

        // 4. set canvas on the ImageView in the UI
        iv_face.setImageDrawable(BitmapDrawable(resources, tempBitmap))
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 10.0f
        private const val BOX_STROKE_WIDTH = 15.0f
    }
}
