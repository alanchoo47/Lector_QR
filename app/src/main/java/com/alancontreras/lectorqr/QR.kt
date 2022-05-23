package com.alancontreras.lectorqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QR : AppCompatActivity(), ZXingScannerView.ResultHandler{

    private val PERMISO_CAMARA = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView( this )
        setContentView(scannerView)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                //se concedio el permiso
            }else{
                solicitarPermiso()
            }
        }

        scannerView?.setResultHandler (this)
        scannerView?.startCamera()
    }

    private fun solicitarPermiso() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
    }

    private fun checarPermiso(): Boolean {
        return(ContextCompat.checkSelfPermission(this@QR, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun handleResult(p0: Result?) {
        //codigo QR leido
        val scanResult = p0?.text

        Log.d("QR_LEIDO", scanResult!!)

    }

    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                if(scannerView == null) {
                    scannerView = ZXingScannerView(this)
                    setContentView(scannerView)
                }
                scannerView?.setResultHandler (this)
                scannerView?.startCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView?.stopCamera()
    }
}