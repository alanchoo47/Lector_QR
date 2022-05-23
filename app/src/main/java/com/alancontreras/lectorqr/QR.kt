package com.alancontreras.lectorqr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QR : AppCompatActivity(), ZXingScannerView.ResultHandler{

    private val PERMISO_CAMARA = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView( this )
        setContentView(scannerView)

        scannerView?.setResultHandler (this)
        scannerView?.startCamera()
    }

    override fun handleResult(p0: Result?) {
        //codigo QR leido
        val scanResult = p0?.text

        Log.d("QR_LEIDO", scanResult!!)

    }
}