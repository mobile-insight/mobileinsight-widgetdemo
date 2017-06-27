package net.mobileinsight.widgetdemo;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.mobileinsight.widgetdemo.adapter.PageAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mContext = getApplicationContext();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Usage Guide"));
        tabLayout.addTab(tabLayout.newTab().setText("Widget Intro"));
        tabLayout.addTab(tabLayout.newTab().setText("Plugin Intro"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PageAdapter adapter = new PageAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},
                        1);
            }
        }
        AssetManager am = mContext.getAssets();
        try {
            String[] filePathList = am.list("plugins");
            for (String fileName : filePathList) {
                copyFilesAssets(mContext, "plugins/" + fileName, Environment.getExternalStorageDirectory()+"/mobile_insight/plugins/"+fileName);
                Log.i("Copy", fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String[] filePathList = am.list("demo");
            for (String fileName : filePathList) {
                copyFilesAssets(mContext, "demo/" + fileName, Environment.getExternalStorageDirectory()+"/mobile_insight/demo/"+fileName);
                Log.i("Copy", fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    public void copyFilesAssets(Context context, String oldPath, String newPath) {
        try {
            Log.i("Copying", oldPath.concat(" "+newPath));
            String fileNames[] = context.getAssets().list(oldPath);

            if (fileNames.length > 0) {
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    Log.i("Copy-subdir", oldPath.concat(" "+newPath));
                    copyFilesAssets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {
                File file = new File(newPath);
                Integer intStr = newPath.length();
                if (!(file.exists() && newPath.substring(intStr-6).equals("mi2log"))){
                    Log.i("Copy-file", oldPath.concat(" "+newPath));
                    InputStream is = context.getAssets().open(oldPath);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int byteCount;
                    while((byteCount=is.read(buffer))!=-1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();//flush buffer
                    is.close();
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

