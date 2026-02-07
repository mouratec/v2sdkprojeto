package br.com.auttar.v2sdkprojeto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.auttar.mobile.libctfclient.sdk.AuttarSDK
import br.com.auttar.mobile.libctfclient.sdk.LibCTFClient
import br.com.auttar.libctfclient.ui.CTFClientActivity
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private val REQ_CODE_AUTTAR = 12345
    private val REQ_PERMISSIONS = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- LAYOUT ---
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        // Título
        val txtTitulo = TextView(this).apply {
            text = "Selecione a Operação:"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }

        // Botão Configuração
        val btnConfig = Button(this).apply {
            text = "Configurações / Login"
            setOnClickListener { abrirMenuConfiguracao() }
        }

        // 1. Botão CRÉDITO (Pinpad)
        val btnCredito = Button(this).apply {
            text = "Crédito à Vista"
            // Ao clicar, chama a função que PERGUNTA O VALOR primeiro
            setOnClickListener { solicitarValorEExecutar(112, "Crédito à Vista") }
        }

        // 2. Botão DÉBITO (Pinpad)
        val btnDebito = Button(this).apply {
            text = "Débito à Vista"
            setOnClickListener { solicitarValorEExecutar(101, "Débito à Vista") }
        }

        // 3. Botão DIGITADA (Sem Pinpad - para teste emulador)
        val btnDigitada = Button(this).apply {
            text = "Crédito Digitado (Manual)"
            setOnClickListener { solicitarValorEExecutar(120, "Crédito Digitado") }
        }

        layout.addView(txtTitulo)
        layout.addView(btnCredito)
        layout.addView(btnDebito)
        layout.addView(btnDigitada)
        layout.addView(btnConfig)

        setContentView(layout)

        verificarPermissoes()
    }

    // --- PASSO 1: ABRE O DIÁLOGO PARA DIGITAR O VALOR ---
    private fun solicitarValorEExecutar(codigoOperacao: Int, nomeOperacao: String) {
        val inputValor = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "0.00"
            gravity = Gravity.CENTER
            textSize = 24f
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(nomeOperacao)
            .setMessage("Digite o valor da venda:")
            .setView(inputValor)
            .setPositiveButton("CONFIRMAR") { _, _ ->
                // Quando clicar em OK, lê o valor e chama o SDK
                val valorTexto = inputValor.text.toString()
                executarVendaNoSDK(codigoOperacao, valorTexto)
            }
            .setNegativeButton("CANCELAR", null)
            .create()

        dialog.show()
    }

    // --- PASSO 2: EXECUTA A VENDA COM O VALOR INFORMADO ---
    private fun executarVendaNoSDK(codigoOperacao: Int, valorTexto: String) {
        try {
            // Conversão segura do valor
            val valorBigDecimal = try {
                val formatado = valorTexto.replace(",", ".")
                BigDecimal(formatado)
            } catch (e: Exception) {
                BigDecimal.ZERO
            }

            if (valorBigDecimal <= BigDecimal.ZERO) {
                Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show()
                return
            }

            // Configura o Builder do SDK [cite: 408-410]
            val builder = LibCTFClient.IntentBuilder.from(codigoOperacao)
            builder.setAmount(valorBigDecimal) // [cite: 412]
            builder.setInstallments(1)

            val libCTFClient = LibCTFClient(this)
            libCTFClient.setCustomViewCTFClient(CTFClientActivity::class.java) // [cite: 567]
            libCTFClient.executeTransaction(builder, REQ_CODE_AUTTAR) // [cite: 481]

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao iniciar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- RESULTADO ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_AUTTAR && data != null) {
            val resultado = LibCTFClient.createTefResult(data)

            if (resultado.returnCode == 0) {
                // Sucesso
                val cupom = resultado.customerSalesReceipt?.joinToString("\n") ?: "Sem cupom"
                exibirDialogo("APROVADA!", "NSU: ${resultado.authorizerNsu}\n\n$cupom")
            } else {
                // Erro
                val msg = resultado.display?.joinToString(" ") ?: resultado.responseCode
                Toast.makeText(this, "Erro: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Funções Auxiliares
    private fun exibirDialogo(titulo: String, mensagem: String) {
        val scrollView = ScrollView(this)
        val textView = TextView(this).apply {
            text = mensagem
            setPadding(40, 40, 40, 40)
            setTextIsSelectable(true)
        }
        scrollView.addView(textView)
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun abrirMenuConfiguracao() {
        // Exibe opções de Login ou Configuração
        val opcoes = arrayOf("Configurar Terminal", "Fazer Login/Carga")
        AlertDialog.Builder(this)
            .setTitle("Configurações")
            .setItems(opcoes) { _, which ->
                val auttarSDK = AuttarSDK(applicationContext)
                if (which == 0) startActivity(auttarSDK.configuration.createDefaultIntent())
                else startActivity(auttarSDK.createDefaultLoginIntent())
            }
            .show()
    }

    private fun verificarPermissoes() {
        val permissoes = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissoes.add(Manifest.permission.BLUETOOTH_SCAN)
            permissoes.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissoes.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        val aSolicitar = permissoes.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (aSolicitar.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, aSolicitar.toTypedArray(), REQ_PERMISSIONS)
        } else {
            AuttarSDK(applicationContext)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, p: Array<out String>, r: IntArray) {
        super.onRequestPermissionsResult(requestCode, p, r)
        AuttarSDK(applicationContext)
    }
}