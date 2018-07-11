package com.diu.bloodbank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchResultActivity extends AppCompatActivity {

    private static final String TAG = "REQUEST";
    private String searchKey;
    private Toolbar mToolbar;
    private DatabaseReference mSearchUserDataBaseRef;
    private Button mCreatePdfButton;

    Bitmap bitmap;
    boolean boolean_save, boolean_permission;

    private RelativeLayout relativeLayout;

    private ProgressDialog progressDialog;
    public static int REQUEST_PERMISSIONS = 1;
    private RecyclerView mResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        //TOOLBAR
        mToolbar = findViewById(R.id.searchResultAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Search Result");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCreatePdfButton = findViewById(R.id.create_pdf);
        relativeLayout = findViewById(R.id.relative_layout);
        searchKey = getIntent().getStringExtra("searchKey");

        mResultList = findViewById(R.id.searchResultRecyclerView);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));

        fn_permission();

        mSearchUserDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mCreatePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (boolean_save) {
                    Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
                    startActivity(intent);

                } else {
                    if (boolean_permission) {
                        progressDialog = new ProgressDialog(SearchResultActivity.this);
                        progressDialog.setMessage("Please wait");
                        bitmap = loadBitmapFromView(relativeLayout, relativeLayout.getWidth(), relativeLayout.getHeight());
                        createPDF();
//                        saveBitmap(bitmap);
                    } else {

                    }

                }


                createPDF();

            }
        });

    }





    private void createPDF() {

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float hight = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;

        int convertHighet = (int) hight, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHighet, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        canvas.drawPaint(paint);


        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHighet, true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);

        // write the document content
        String targetPdf = "/sdcard/DIUBLOODBANK.pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            mCreatePdfButton.setText("Check PDF");
            boolean_save=true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();


    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);

        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                boolean_permission = true;


            } else {
                Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(SearchResultActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(SearchResultActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }

            if ((ActivityCompat.shouldShowRequestPermissionRationale(SearchResultActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(SearchResultActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;


        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Results, ResultsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Results, ResultsViewHolder>(

                Results.class,
                R.layout.single_search_layout,
                ResultsViewHolder.class,
                mSearchUserDataBaseRef.orderByChild("searchKey").equalTo(searchKey)

        ) {
            @Override
            protected void populateViewHolder(ResultsViewHolder viewHolder, Results model, int position) {

                viewHolder.setResultUserName(model.getName());
                viewHolder.setResultUserImage(model.getPhotoUri(), getApplicationContext());
                viewHolder.setResultUserGroup(model.getBloodGroup());
                viewHolder.setResultLocation(model.getArea());
                viewHolder.setResultLastDonate(model.getLastDonate());

                final String resultUserId = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent resultUserProfile = new Intent(SearchResultActivity.this, ResultUserProfileActivity.class);
                        resultUserProfile.putExtra("resultUserId", resultUserId);
                        startActivity(resultUserProfile);

                    }
                });

            }
        };

        mResultList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class ResultsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ResultsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setResultUserName(String name) {

            TextView resultUserName = mView.findViewById(R.id.singleSearchUserName);
            resultUserName.setText(name);

        }

        public void setResultUserImage(String photoUri, Context context) {

            CircleImageView resultUserImage = mView.findViewById(R.id.singleSearchUserImage);
            Picasso.with(context).load(photoUri).placeholder(R.drawable.avatar).into(resultUserImage);


        }

        public void setResultUserGroup(String bloodGroup) {

            TextView resultUserGroup = mView.findViewById(R.id.singleSearchUserGroup);
            resultUserGroup.setText(bloodGroup);

        }

        public void setResultLocation(String area) {

            TextView resultUserArea = mView.findViewById(R.id.singleSearchUserArea);
            resultUserArea.setText(area);

        }

        public void setResultLastDonate(String lastDonate) {

            TextView resultUserLastDonate = mView.findViewById(R.id.singleSearchLastDonate);
            resultUserLastDonate.setText("Last Donate: "+lastDonate);

        }
    }

}
