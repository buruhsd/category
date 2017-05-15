package com.a.buruhsd.category;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.a.buruhsd.category.Adapter.CategoryAdapter;
import com.a.buruhsd.category.Model.Category;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.a.buruhsd.category.R.id.fab;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    ListView list;
    SwipeRefreshLayout swipe;
    List<Category> itemList = new ArrayList<Category>();
    CategoryAdapter adapter;
    String success;
    AlertDialog.Builder dialog;
    LayoutInflater inflater;
    View dialogView;
    EditText text_id,text_kategori;
    String id,kategori,status;
    Button buttonChooseImage;
    ImageView imageView;
    int PICK_IMAGE_REQUEST = 1;
    Bitmap bitmap;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String url = "http://192.168.1.68/mrmht/public/api/category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        list = (ListView) findViewById(R.id.list);
        fab =(FloatingActionButton) findViewById(R.id.fab);
        adapter = new CategoryAdapter(MainActivity.this, itemList);
        list.setAdapter(adapter);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemList.clear();
                adapter.notifyDataSetChanged();
                callVolley();

            }
        });
        swipe.post(new Runnable() {
            @Override
            public void run() {
                swipe.setRefreshing(true);
                itemList.clear();
                adapter.notifyDataSetChanged();
                callVolley();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogForm("","","","Save");
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view,final int position, long id) {
                final String idx = itemList.get(position).getId();
                final CharSequence[] dialogitem = {"Edit","Delete"};
                dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setCancelable(true);
                dialog.setItems(dialogitem, new DialogInterface.OnClickListener(){
                    @Override
                    public  void onClick(DialogInterface dialog,int which){
                        switch (which){
                            case 0:
                                edit(idx); //edit(idx);
                                break;
                            case 1:
                                delete(idx); //delete(idx);
                                break;
                        }
                    }
                }).show();

                return false;
            }
        });


    }


    private void kosong(){
        text_id.setText(null);
        text_kategori.setText(null);

    }

    private void DialogForm(String idx, String kategorix, String imagex,  String button){
        dialog = new AlertDialog.Builder(MainActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_kategori,null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
//        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle("Add Category");

        imageView = (ImageView) dialogView.findViewById(R.id.image);
        text_id = (EditText) dialogView.findViewById(R.id.text_id);
        text_kategori = (EditText) dialogView.findViewById(R.id.text_kategori);
        buttonChooseImage = (Button) dialogView.findViewById(R.id.btn_pilihGambar);
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        if(!idx.isEmpty()){
            text_id.setText(idx);
            text_kategori.setText(kategorix);


        }else {
            kosong();
        }

        dialog.setPositiveButton(button,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                id = text_id.getText().toString();
                kategori = text_kategori.getText().toString();

                simpan_update();
                dialog.dismiss();
            }

        });

        dialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
                kosong();
            }
        });
        dialog.show();
    }



    private void callVolley(){
        itemList.clear();
        adapter.notifyDataSetChanged();
        swipe.setRefreshing(true);

        JsonArrayRequest jArr = new JsonArrayRequest(url, new Response.Listener<JSONArray>(){

            @Override
            public void onResponse(JSONArray response){
                Log.d(TAG,response.toString());

                for (int i=0; i<response.length(); i++){
                    try {
                        JSONObject obj = response.getJSONObject(i);

                        Category item = new Category();
                        item.setId(obj.getString("id"));
                        item.setName(obj.getString("name"));
                        item.setDescription(obj.getString("description"));

                        itemList.add(item);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
                swipe.setRefreshing(false);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                VolleyLog.d(TAG,"Error" + error.getMessage());
                swipe.setRefreshing(false);
            }
        });
        RequestHandler.getInstance(this).addToRequestQueue(jArr);
    }

    private void simpan_update(){
        if(id.isEmpty()){
            RequestQueue queue= Volley.newRequestQueue(this);

            Map<String, String> jsonParams = new HashMap<String, String>();
            jsonParams.put("name",kategori);
            jsonParams.put("description","category");
            jsonParams.put("image",getStringImage(bitmap));
            Log.d(TAG,getStringImage(bitmap));

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, new JSONObject(jsonParams), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("save", response.toString());
                        status = response.getString("status");
                        if(status.equals("ok")){
                            callVolley();
                            kosong();
                            Toast.makeText(MainActivity.this, "Success saved category",Toast.LENGTH_LONG).show();
                            adapter.notifyDataSetChanged();

                        }else{
                            Toast.makeText(MainActivity.this, "Failed saved category",Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"Error"+ error.getMessage());
                    Toast.makeText(MainActivity.this, "Failed connect to server",Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String,String>();
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            queue.add(jsonObjectRequest);
//            RequestHandler.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }else {
            RequestQueue queue= Volley.newRequestQueue(this);

            Map<String, String> jsonParams = new HashMap<String, String>();
            jsonParams.put("name",kategori);
            jsonParams.put("description","category");
            Log.d(TAG,"Json:"+ new JSONObject(jsonParams));

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PUT, url+"/"+id, new JSONObject(jsonParams), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("update", response.toString());
                        status = response.getString("status");
                        if(status.equals("ok")){
                            callVolley();
                            kosong();
                            Toast.makeText(MainActivity.this, "Success update category",Toast.LENGTH_LONG).show();
                            adapter.notifyDataSetChanged();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed update category",Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"Error"+ error.getMessage());
                    Toast.makeText(MainActivity.this, "Failed connect to server",Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String,String>();
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            queue.add(jsonObjectRequest);
            //RequestHandler.getInstance(this).addToRequestQueue(strReq);
        }
    }

    private void edit(final String idx){
        StringRequest strReq = new StringRequest(
                Request.Method.GET, url+"/"+idx, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"Response:" + response.toString());
                try{
                    JSONObject jObj = new JSONObject(response);

                    Log.d("get edit data", jObj.toString());
                    String idx = jObj.getString("id");
                    String kategorix = jObj.getString("name");
                    String descriptionx = jObj.getString("description");
                    String imagex = jObj.getString("image");

                    DialogForm(idx, kategorix, imagex ,"UPDATE");
                    adapter.notifyDataSetChanged();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.e(TAG,"Error" + error.getMessage());
                Toast.makeText(MainActivity.this, "Failed connect to server",Toast.LENGTH_LONG).show();
            }
        });
        RequestHandler.getInstance(this).addToRequestQueue(strReq);
    }

    private void delete(final String idx){
        StringRequest strReq = new StringRequest(
                Request.Method.DELETE, url +"/"+ idx, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"Response:" + response.toString());
                try{
                    JSONObject jObj = new JSONObject(response);
                    status = jObj.getString("status");
                    if(status.equals("ok")){
                        callVolley();
                        Toast.makeText(MainActivity.this, "Success delete category",Toast.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(MainActivity.this, "Success delete category",Toast.LENGTH_LONG).show();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.e(TAG,"Error" + error.getMessage());
                Toast.makeText(MainActivity.this, "Failed connect to server",Toast.LENGTH_LONG).show();
            }
        });
        RequestHandler.getInstance(this).addToRequestQueue(strReq);
    }

    public String getStringImage (Bitmap bmp){
        if(bitmap!=null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedIMage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return encodedIMage;
        }else{
            String img = "";
            return img;
        }
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri filePatch = data.getData();
            if(filePatch!=null){
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePatch);
                    imageView.setImageBitmap(bitmap);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else {
                bitmap=null;
                Toast.makeText(this, "No Image is selected.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


}
