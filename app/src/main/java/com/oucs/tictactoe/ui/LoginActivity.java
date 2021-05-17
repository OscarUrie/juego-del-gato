package com.oucs.tictactoe.ui;

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
import com.oucs.tictactoe.R;

public class LoginActivity extends AppCompatActivity {
    EditText editTextEmail,editTextPassword;
    ProgressBar progressBar;
    ScrollView scrollView;
    FirebaseAuth firebaseAuth;
    String email,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        iniciarComponentes();
        firebaseAuth=FirebaseAuth.getInstance();
        cambiaVisibilidad(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if(currentUser!=null){
            Log.i("Firebase","Un usuario ya ha iniciado sesion");
            updateUI(currentUser);
        }
    }

    private void iniciarComponentes(){
        editTextEmail=findViewById(R.id.edt_email);
        editTextPassword=findViewById(R.id.edt_password);
        progressBar=findViewById(R.id.progress_bar_login);
        scrollView=findViewById(R.id.scroll_view_login);
    }
    public void iniciarSesion(View view){
        email=editTextEmail.getText().toString();
        password=editTextPassword.getText().toString();
        if(!email.isEmpty()&&!password.isEmpty()){
            cambiaVisibilidad(true);
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this,task -> {
                if(task.isSuccessful()){
                    FirebaseUser user=firebaseAuth.getCurrentUser();
                    updateUI(user);
                }
                else{
                    Toast.makeText(LoginActivity.this,"Error al iniciar sesion",Toast.LENGTH_LONG).show();
                    updateUI(null);
                }
            });
        }
        else {
            if (email.isEmpty()){
                editTextEmail.setError("Llena este campo");
            }
            if(password.isEmpty()){
                editTextPassword.setError("Llena este campo");
            }
        }
    }
    public void registrarUsuario(View view){
        Intent intent=new Intent(LoginActivity.this, SingupActivity.class);
        startActivity(intent);
        finish();
    }
    private void updateUI(FirebaseUser user) {
        if(user!=null){
            //Se almacena la informacion en la firestore
            cambiaVisibilidad(false);
            Intent intent=new Intent(LoginActivity.this, BuscarGameActivity.class);
            startActivity(intent);
        }
        else{
            cambiaVisibilidad(false);
            editTextPassword.setError("Algun dato es incorrectos");
            editTextPassword.requestFocus();
        }
    }
    private void cambiaVisibilidad(boolean visible) {
        progressBar.setVisibility(visible? View.VISIBLE:View.INVISIBLE);
        scrollView.setVisibility(visible? View.INVISIBLE:View.VISIBLE);
    }
}