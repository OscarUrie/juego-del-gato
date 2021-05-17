package com.oucs.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oucs.tictactoe.R;
import com.oucs.tictactoe.model.User;

import java.util.Objects;

public class SingupActivity extends AppCompatActivity {
    EditText eTNameSingup,eTEmailSingup,eTPasswordSingup;
    ProgressBar progressBarSingup;
    ScrollView scrollViewSingup;
    String name,email,password;
    boolean datosCorrectos=false,registroCorrecto=false;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);
        iniciarComponentes();
        firebaseAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        cambiaVisibilidad(false);
    }
    //Es para los componenetes de la vista
    private void iniciarComponentes() {
        eTNameSingup=findViewById(R.id.edt_name_singup);
        eTEmailSingup=findViewById(R.id.edt_email_singup);
        eTPasswordSingup=findViewById(R.id.edt_password_singup);
        progressBarSingup=findViewById(R.id.progress_bar_singup);
        scrollViewSingup=findViewById(R.id.scroll_view_singup);
    }
    //Aqui se registar el usuario en Firebase
    public void registroUsuario(View view){
        if(!datosCorrectos){
            capturaLosDatos();
            if(!datosCorrectos) {
                Toast.makeText(this,"Algun dato es incorrecto",Toast.LENGTH_SHORT).show();
            }
        }
        cambiaVisibilidad(true);
        if (datosCorrectos){
            Log.i("TicTacToe","Datos correctos");
            if(!registroCorrecto){
                createUser();
                if (!registroCorrecto){
                    Toast.makeText(this,"No se pudo crear tu usuario",Toast.LENGTH_SHORT).show();
                }
            }
            if (registroCorrecto){
                Log.i("TicTacToe","Usuario registrado");
                guardarUsuarioEnDB(Objects.requireNonNull(firebaseAuth.getCurrentUser()));
            }
        }
        cambiaVisibilidad(false);
    }
    private void capturaLosDatos(){
        name=eTNameSingup.getText().toString();
        email=eTEmailSingup.getText().toString();
        password=eTPasswordSingup.getText().toString();
        if(!name.isEmpty()&&!email.isEmpty()&&!password.isEmpty()){
            if(password.length()>6){
                datosCorrectos=true;
            }
            else{
                eTPasswordSingup.setError("6 caracteres minimo");
                datosCorrectos=false;
            }
        }
        else{
            datosCorrectos=false;
            if(name.isEmpty()){
                eTNameSingup.setError("Nombre requerido");
            }
            if(email.isEmpty()){
                eTEmailSingup.setError("Email requerido");
            }
            if(password.isEmpty()){
                eTPasswordSingup.setError("Password requerido");
            }
        }
    }

    private void createUser() {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this,task -> {
                    /*FirebaseUser user=firebaseAuth.getCurrentUser();
                        updateUI(user);*/
                    registroCorrecto= task.isSuccessful();
                });
    }

    private void guardarUsuarioEnDB(@NonNull FirebaseUser user){
        //Se almacena la informacion en la firestore
        /*Se va usar user.getUid() ya que lo que se quiere
         * es que el id en firestore se el documentoto con el mismo id de autemtificacion*/
        User newUser=new User(name,0,0);
        db.collection("usuarios")
                .document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid ->{
                    Log.i("TicTacToe","Usuario guardado en FireStore");
                    Intent intent=new Intent(SingupActivity.this, BuscarGameActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(failure->
                        Toast.makeText(SingupActivity.this,"No se pudo guardar tu usuario",Toast.LENGTH_SHORT).show()
                );
    }
    private void cambiaVisibilidad(boolean visible) {
        progressBarSingup.setVisibility(visible? View.VISIBLE:View.GONE);
        scrollViewSingup.setVisibility(visible? View.GONE:View.VISIBLE);
    }
}
/*
private void updateUI(boolean voyAlJuego) {
        if(voyAlJuego){
            Intent intent=new Intent(SingupActivity.this,GameActivity.class);
            startActivity(intent);
        }
        else{
            cambiaVisibilidad(false);
            eTPasswordSingup.setError("Datos introducidos incorrectos");
            eTPasswordSingup.requestFocus();
        }
    }
* */