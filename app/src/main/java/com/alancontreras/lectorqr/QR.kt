package com.alancontreras.lectorqr

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.UriMatcher
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.MalformedJsonException
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import com.google.zxing.client.result.URIParsedResult
import com.google.zxing.client.result.URIResultParser
import me.dm7.barcodescanner.core.BarcodeScannerView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.URL


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
        var a = 0
        Log.d("QR_LEIDO", scanResult!!)
        var type = scanResult
        try{

            /*
            Se realiza un when para diferencias los casos y asi trabajar en dado caso que se requiera, usando el MIME de cado uno
            text/plain es para mensjae de texto
            text/html es para url
            http.plain_text_type es para correo electronico

             */
            var i: Intent = when(type){
                "text/plain" -> MensajeSMS(scanResult)
                "text/html" -> DirUrl(scanResult)
                "HTTP.PLAIN_TEXT_TYPE" -> MensajeCorreo(scanResult)
                else ->  nada(scanResult)
            }

            i.setData(Uri.parse(scanResult))
            startActivity(i)
            finish()

        }catch(e: MalformedJsonException){

            AlertDialog.Builder( this@QR)
                .setTitle("Error")
                .setMessage("El codigo QR no es valido para la aplicacion")
                .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                })
                .create()
                .show()
        }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode ){
            PERMISO_CAMARA -> {
                if(grantResults.isNotEmpty()){
                    if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                                AlertDialog.Builder(this@QR)
                                    .setTitle("Permiso requerido")
                                    .setMessage(("Se necesita acceder a la cámara para leer los códigos QR"))
                                    .setPositiveButton("Aceptar", DialogInterface.OnClickListener{ dialogInterface, i ->
                                        requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
                                    })
                                    .setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialogInterface, i ->
                                        dialogInterface.dismiss()
                                        finish()
                                    })
                                    .create()
                                    .show()
                            }else{
                                Toast.makeText(this@QR, "El permiso de la cámara no se ha concedido", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun MensajeSMS(scanResult: String?): Intent{
    var i = Intent(Intent.ACTION_SEND).apply {
        data = Uri.parse("smsto:")
        putExtra("sms_body",scanResult)
    }
    i.setData(Uri.parse(scanResult))
    return i
}
fun MensajeCorreo(scanResult: String?): Intent{
    var i = Intent(Intent.ACTION_SEND).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, scanResult)
        putExtra(Intent.EXTRA_SUBJECT, scanResult)
        putExtra(Intent.EXTRA_TEXT,scanResult)

    }
    i.setData(Uri.parse(scanResult))
    return i
}
fun DirUrl(scanResult: String?): Intent{
    var url = URL(scanResult)
    var i = Intent(Intent.ACTION_VIEW)
    i.setData(Uri.parse(scanResult))
    return i
}
fun nada(scanResult: String?): Intent{
    var url = URL(scanResult)
    var i = Intent(Intent.ACTION_VIEW)
    i.setData(Uri.parse(scanResult))
    return i
}
