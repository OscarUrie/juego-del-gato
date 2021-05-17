package com.oucs.tictactoe.ui;

//import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
//import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
//import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.oucs.tictactoe.R;
import com.oucs.tictactoe.common.Constantes;
import com.oucs.tictactoe.model.Jugada;

public class BuscarGameActivity extends AppCompatActivity {
    LinearLayout progressBarFind;
    //ProgressBar progressBar;
    TextView textViewMensaje;
    ScrollView scrollView;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseUser fbuser;
    private String uId;
    private String jugadaId="";
    private ListenerRegistration listenerRegistration=null;
    //private LottieAnimationView lottie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_game);
        iniciaComponentes();
        //iniciaProgressBar();
        initFirebase();
        cambiaVisibilidad(false,"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cambiaVisibilidad(false,"");
    }

    @Override
    protected void onStop() {
        if(listenerRegistration!=null){
            listenerRegistration.remove();
        }
        if(!jugadaId.equals("")){
            eliminarPartida();
        }
        super.onStop();
    }
    //metodo para iniciar firebase
    private void initFirebase() {
        firebaseAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        fbuser=firebaseAuth.getCurrentUser();
        //obtiene el id del usuario desde firebaseauth
        assert fbuser != null;
        uId=fbuser.getUid();
    }
    //metodo para iniciar los componentes
    private void iniciaComponentes() {
        progressBarFind=findViewById(R.id.progress_bar_text_view);
        //progressBar=findViewById(R.id.progress_bar_find_game);
        textViewMensaje =findViewById(R.id.mensaje_mientras_carga);
        scrollView=findViewById(R.id.menu_juego);
        //lottie=findViewById(R.id.lottie_view);
    }

    /*private void iniciaProgressBar() {
        //progressBar.setIndeterminate(true);
        cambiaVisibilidad(false);
    }*/

    //Metodo para el boton jugar online
    public void jugarJuego(View view){
        buscarJugadaLibre();

    }
    //Metodo para el boton ver ranking
    public void verRanking(View view){

    }
    //metodo de buscar una paratida en firestore
    private void buscarJugadaLibre() {
        cambiaVisibilidad(true,"loading...");
        //Es una consulta a Firestore que buscara
        // en los documentos con el campo "jugador2Id" y este "" (vacio)
        db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                .whereEqualTo(Jugada.JUGADOR_DOS,"")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().size()==0) {
                        //Si la consutal tiene 0 resultados se crea una partida
                        crearNuevaJugada();
                    }
                    else {
                        //se obtendra la primera partida de la lista ".get(0)"
                        //DocumentSnapshot documentJugagda=task.getResult().getDocuments().get(0);
                        DocumentSnapshot documentJugagda = null;
                        for (DocumentSnapshot ds:task.getResult().getDocuments()){
                            //Se evita que el jugador juege consigo mismo
                            if(!ds.get(Jugada.JUGADOR_UNO).equals(uId)){
                                documentJugagda=ds;
                                break;
                            }
                        }
                        /*if (documentJugagda==null){
                            crearNuevaJugada();
                        }*/
                        if (documentJugagda!=null){
                            jugadaId=documentJugagda.getId();
                            Jugada jugada=documentJugagda.toObject(Jugada.class);
                            //nosotros seremos el jugador 2
                            jugada.setJugador2Id(uId);
                            //Se editara el documento y seremos el jugador 2
                            db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                                    .document(jugadaId)
                                    .set(jugada)
                                    .addOnSuccessListener(aVoid ->{
                                        textViewMensaje.setText("Partida Libre Empezando...");
                                        //cambiarAnimacion();
                                        startGame();
                                    })
                                    .addOnFailureListener(e -> {
                                        cambiaVisibilidad(false,"");
                                        Toast.makeText(BuscarGameActivity.this,"Error al encontrar una Partida",Toast.LENGTH_LONG).show();
                                    });
                        }
                        else {
                            cambiaVisibilidad(false,"");
                            Toast.makeText(BuscarGameActivity.this,"Estas esperando otra partida",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //Cuando en la consulta no hay partidas disponibles
    private void crearNuevaJugada() {
        textViewMensaje.setText("Creando nueva partida...");
        Jugada nuevaJugada=new Jugada(uId);
        //Este es como un post a una base de datos
        //Se crea un nuevo documento con judador 1
        db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                .add(nuevaJugada)
                .addOnSuccessListener(documentReference ->{
                    jugadaId=documentReference.getId();
                    //cambiaVisibilidad(false,"");
                    //Toast.makeText(BuscarGameActivity.this,"Partida Lista",Toast.LENGTH_LONG).show();
                    //hay que esperar otro jugador
                    esperarJugador();
                })
                .addOnFailureListener(e -> {
                    cambiaVisibilidad(false,"");
                    Toast.makeText(BuscarGameActivity.this,"Error al crear una Partida",Toast.LENGTH_LONG).show();
                });
    }

    private void esperarJugador() {
        textViewMensaje.setText("Esperando otro jugador...");
        //Este metodo espera a que haya una edicion en el documento
        //creado por nosotros
        listenerRegistration=db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                .document(jugadaId)
                .addSnapshotListener((value, error) -> {
                    if(!value.get(Jugada.JUGADOR_DOS).equals("")){
                        textViewMensaje.setText("Jugador nuevo Empezando...");
                        //cambiarAnimacion();
                        /*
                        //Esperar un tiempo para mostrar el mensaje
                        final Handler handler=new Handler();
                        final Runnable r= new Runnable() {
                            @Override
                            public void run() {
                                startGame();
                            }
                        };
                        handler.postDelayed(r,500);
                        */
                        startGame();
                    }
                });
    }
    //Metodo para ir a la siguente activity donde se realizara la jugada
    private void startGame() {
        if(listenerRegistration!=null){
            listenerRegistration.remove();
        }

        Intent intent=new Intent(BuscarGameActivity.this,GameActivity.class);
        Log.i("Firebase","Pasando: "+jugadaId);
        intent.putExtra(Constantes.JUGADA_ID,jugadaId);
        jugadaId="";
        startActivity(intent);
    }
    //Es un metodo para mostrar y ocultar el menu por la barra de espera
    private void cambiaVisibilidad(boolean visible,final String mensaje) {
        if(visible){
            textViewMensaje.setText(mensaje);
            progressBarFind.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
        }
        else{
            progressBarFind.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }
    //Es un metood para cambiar el icono de lottie airbnb
    /*private void cambiarAnimacion(){
        lottie.setRepeatCount(0);
        lottie.setAnimation("checked_animation");
        lottie.playAnimation();
    }/*/
    //metodo para elimnar el documento creado por el usuario
    @WorkerThread
    private void eliminarPartida(){
        db.collection(Constantes.NOMBRE_COLECCION_JUGADAS)
                .document(jugadaId)
                .delete()
                .addOnCompleteListener(task -> jugadaId="");
    }
}


/*Quiero ver las diferencias entre el normal y la funicon lamda
private void esperarJugador() {
        textViewMensaje.setText("Esperando otro jugador...");
        //Este metodo espera a que haya una edicion en el documento
        //creado por nosotros
        listenerRegistration=db.collection(Constantes.NOMBRE_COLECCION)
                .document(jugadaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(!value.get("jugador2Id").equals("")){
                            textViewMensaje.setText("Jugador nuevo Empezando...");
                            cambiarAnimacion();
                            /g*
                            //Esperar un tiempo para mostrar el mensaje
                            final Handler handler=new Handler();
                            final Runnable r= new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            };
                            handler.postDelayed(r,500);
                            *g/
startGame();
}
                    }
                            });
                            }

* */