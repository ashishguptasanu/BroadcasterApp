package broadcast.edge.com.broadcasterapp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bambuser.broadcaster.BroadcastStatus;
import com.bambuser.broadcaster.Broadcaster;
import com.bambuser.broadcaster.CameraError;
import com.bambuser.broadcaster.ConnectionError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import broadcast.edge.com.broadcasterapp.Adapters.CommentAdapter;
import broadcast.edge.com.broadcasterapp.Models.Comments;

public class CameraActivity extends AppCompatActivity{
    SurfaceView mPreviewSurface;
    Broadcaster mBroadcaster;
    private static final String APPLICATION_ID = "";
    ImageView mBroadcastButton, mRoatateButton;
    TextView tvLiveStatus, tvViewerCount;
    DatabaseReference mDatabase;
    RecyclerView recyclerViewComments;
    LinearLayoutManager linearLayoutManager;
    CommentAdapter commentAdapter;
    List<Comments> commentsList = new ArrayList<>();
    LinearLayout layoutCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        View mContentView = findViewById(R.id.content_fullscreen);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();
        recyclerViewComments = (RecyclerView)findViewById(R.id.recycler_comments);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewComments.setLayoutManager(linearLayoutManager);
        commentAdapter = new CommentAdapter(getApplicationContext(), commentsList);
        recyclerViewComments.setAdapter(commentAdapter);
        tvLiveStatus = findViewById(R.id.tv_live_status);
        tvViewerCount = findViewById(R.id.tv_viewer_count);
        layoutCount = findViewById(R.id.layout_count);
        tvLiveStatus.setVisibility(View.GONE);
        layoutCount.setVisibility(View.GONE);
        mPreviewSurface = (SurfaceView) findViewById(R.id.PreviewSurfaceView);
        mBroadcaster = new Broadcaster(this, APPLICATION_ID, mBroadcasterObserver);
        mBroadcaster.setRotation(getWindowManager().getDefaultDisplay().getRotation());
        mBroadcastButton = (ImageView) findViewById(R.id.BroadcastButton);
        mRoatateButton = findViewById(R.id.RotateButton);
        mRoatateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        mBroadcaster.switchCamera();
            }
        });
        mBroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBroadcaster.canStartBroadcasting()){
                    mBroadcaster.startBroadcast();
                    //mViewerCountObserver.onCurrentViewersUpdated(1);
                    mBroadcaster.setViewerCountObserver(mViewerCountObserver);
                    loadComments();
                    animateLiveStatus();}

                else
                    mBroadcaster.stopBroadcast();
                    tvLiveStatus.setVisibility(View.GONE);
                    layoutCount.setVisibility(View.GONE);
                    mBroadcastButton.setImageResource(R.mipmap.go_live);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mBroadcaster.setCameraSurface(mPreviewSurface);
        mBroadcaster.onActivityResume();
        mBroadcaster.setRotation(getWindowManager().getDefaultDisplay().getRotation());
        // ...
        if (!hasPermission(Manifest.permission.CAMERA)
                && !hasPermission(Manifest.permission.RECORD_AUDIO))
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, 1);
        else if (!hasPermission(Manifest.permission.RECORD_AUDIO))
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 1);
        else if (!hasPermission(Manifest.permission.CAMERA))
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mBroadcaster.onActivityDestroy();
    }
    @Override
    public void onPause() {
        super.onPause();
        mBroadcaster.onActivityPause();
    }

    private boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
    private Broadcaster.ViewerCountObserver mViewerCountObserver = new Broadcaster.ViewerCountObserver() {
        @Override
        public void onCurrentViewersUpdated(long l) {
            Log.d("Current Viewers", String.valueOf(l));
            tvViewerCount.setText(" " + String.valueOf(l));
        }

        @Override
        public void onTotalViewersUpdated(long l) {

        }
    };
    private Broadcaster.Observer mBroadcasterObserver = new Broadcaster.Observer() {
        @Override
        public void onConnectionStatusChange(BroadcastStatus broadcastStatus) {
            if (broadcastStatus == BroadcastStatus.STARTING)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                animateLiveStatus();
            if (broadcastStatus == BroadcastStatus.IDLE)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //tvLiveStatus.setVisibility(View.GONE);
            mBroadcastButton.setImageResource(R.mipmap.stop_live);
            Log.i("Mybroadcastingapp", "Received status change: " + broadcastStatus);
        }
        @Override
        public void onStreamHealthUpdate(int i) {
        }
        @Override
        public void onConnectionError(ConnectionError connectionError, String s) {
            Log.w("Mybroadcastingapp", "Received connection error: " + connectionError + ", " + s);
        }
        @Override
        public void onCameraError(CameraError cameraError) {
        }
        @Override
        public void onChatMessage(String s) {
        }
        @Override
        public void onResolutionsScanned() {
        }
        @Override
        public void onCameraPreviewStateChanged() {
        }
        @Override
        public void onBroadcastInfoAvailable(String s, String s1) {
            Log.d("Broadcast Info", s + "=" + s1);
        }
        @Override
        public void onBroadcastIdAvailable(String s) {
        }

    };


    private void animateLiveStatus() {
        tvLiveStatus.setVisibility(View.VISIBLE);
        layoutCount.setVisibility(View.VISIBLE);
        ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(tvLiveStatus, View.ALPHA, 1, 0);
        scaleAnim.setDuration(500);
        scaleAnim.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnim.setRepeatMode(ValueAnimator.REVERSE);
        scaleAnim.start();

    }

    private void loadComments() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("comments").child("1");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("dataSnapShot", dataSnapshot.getChildren().toString());
                Comments comments = dataSnapshot.getValue(Comments.class);
                commentsList.add(comments);
                Log.d("CommentsSize", String.valueOf(commentsList.size()));

                commentAdapter.notifyDataSetChanged();
                recyclerViewComments.smoothScrollToPosition(commentAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
