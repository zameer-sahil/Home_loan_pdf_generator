package com.example.foodrecipe

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class HomeLoanCalculatorActivity : AppCompatActivity() {
    private lateinit var loanAmountProgressBar: SeekBar
    private lateinit var interestRateProgressBar: SeekBar
    private lateinit var loanTenureProgressBar: SeekBar
    private lateinit var loanAmountTextView: TextView
    private lateinit var interestRateTextView: TextView
    private lateinit var loanTenureTextView: TextView
    private lateinit var emiResultTextView: TextView
    private lateinit var generatePdfButton: Button

    private var loanAmount = 100000.0
    private var interestRate = 0.5
    private var loanTenureYears = 1

    private lateinit var pdfFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_loan_calculator)

        loanAmountProgressBar = findViewById(R.id.loanAmountProgressBar)
        interestRateProgressBar = findViewById(R.id.interestRateProgressBar)
        loanTenureProgressBar = findViewById(R.id.loanTenureProgressBar)
        loanAmountTextView = findViewById(R.id.loanAmountTextView)
        interestRateTextView = findViewById(R.id.interestRateTextView)
        loanTenureTextView = findViewById(R.id.loanTenureTextView)
        emiResultTextView = findViewById(R.id.emiResultTextView)
        generatePdfButton = findViewById(R.id.pdfBtn)

        loanAmountProgressBar.max = 90
        interestRateProgressBar.max = 145
        loanTenureProgressBar.max = 29

        loanAmountProgressBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        interestRateProgressBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        loanTenureProgressBar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        generatePdfButton.setOnClickListener {
            val generatedFile = generatePDF()

            if (generatedFile != null) {
                pdfFile = generatedFile
                sendEmailWithPdfAttachment()
            } else {

                Log.e("HomeLoanCalculator", "PDF generation failed!")
            }
        }
    }

    private val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekBar?.id) {
                R.id.loanAmountProgressBar -> {
                    loanAmount = 100000 + progress.toDouble() * 10000
                    loanAmountTextView.text = "Loan Amount: %.0f".format(loanAmount)
                }
                R.id.interestRateProgressBar -> {
                    interestRate = 0.5 + (progress.toDouble() / 10)
                    interestRateTextView.text = "Interest Rate: %.1f%%".format(interestRate)
                }
                R.id.loanTenureProgressBar -> {
                    loanTenureYears = 1 + progress
                    loanTenureTextView.text = "Loan Tenure: $loanTenureYears years"
                }
            }
            calculateEMI()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun calculateEMI() {
        if (loanAmount > 0 && interestRate > 0 && loanTenureYears > 0) {
            val monthlyInterestRate = interestRate / 12 / 100
            val loanTenureMonths = loanTenureYears * 12
            val emi = (loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanTenureMonths.toDouble())) /
                    (Math.pow(1 + monthlyInterestRate, loanTenureMonths.toDouble()) - 1)
            emiResultTextView.text = "Monthly EMI: %.2f".format(emi)
        }
    }

    private fun generatePDF(): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawText("Loan Amount: $loanAmount", 80f, 100f, Paint())
        canvas.drawText("Interest Rate: $interestRate%", 80f, 150f, Paint())
        canvas.drawText("Loan Tenure: $loanTenureYears years", 80f, 200f, Paint())
        canvas.drawText("Monthly EMI: ${emiResultTextView.text}", 80f, 250f, Paint())

        pdfDocument.finishPage(page)

        val directoryPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
        val filePath = "$directoryPath/LoanEMIReport.pdf"
        val file = File(filePath)

        return try {
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("HomeLoanCalculator", "PDF generation failed", e)
            null
        }
    }

    private fun sendEmailWithPdfAttachment() {
        val pdfUri: Uri = FileProvider.getUriForFile(
            this@HomeLoanCalculatorActivity,
            "$packageName.provider",
            pdfFile
        )

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Home Loan Calculation PDF")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached Home Loan Calculation PDF.")
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
}
