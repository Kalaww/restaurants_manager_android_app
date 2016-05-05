package com.ced.restobook;

import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.ced.restobook.fragment.AcceuilFragment;
import com.ced.restobook.fragment.DetailRestaurantFragment;
import com.ced.restobook.fragment.EditRestaurantFragment;
import com.ced.restobook.fragment.MesRestaurantsFragment;
import com.ced.restobook.fragment.ProximityFragment;
import com.ced.restobook.fragment.ProximityPlaceFragment;
import com.ced.restobook.util.InfoRestaurant;

import java.util.HashMap;


public class Main extends AppCompatActivity{

    private final static String TAG = "Main";

    private final static String SAVE_CURRENT_FRAGMENT = "save current frag";
    private final static String SAVE_CURRENT_RESTAURANT = "save current restaurant";

    /**
     * Fragment afficher
     */
    private Fragment currentFragment;

    /**
     * HashMap entre les class de fragment et leur titre
     */
    private HashMap<Class, String> fragmentTitleMap;

    /**
     * Infos du restaurant selectionné
     */
    private InfoRestaurant currentSelectedRestaurant;

    private Toolbar toolbar;
    private NavigationView menuDrawer;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initHashMapSections();

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        menuDrawer = (NavigationView) findViewById(R.id.navigation_drawer);
        menuDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                changeFragment(item.getTitle().toString());
                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // saved instance state
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(SAVE_CURRENT_FRAGMENT)){
                Log.d(TAG, "Reloading current fragment");
                currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, SAVE_CURRENT_FRAGMENT);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
                changeTitle();
            }else
                changeFragment(getString(R.string.section_acceuil));

            if(savedInstanceState.containsKey(SAVE_CURRENT_RESTAURANT)){
                currentSelectedRestaurant = (InfoRestaurant) savedInstanceState.getSerializable(SAVE_CURRENT_RESTAURANT);
            }
        }else
            changeFragment(getString(R.string.section_acceuil));
    }

    /**
     * Change le fragment affiché
     * @param fragmentTAG titre du nouveau fragment
     */
    public void changeFragment(String fragmentTAG){
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment newFrag = null;

        if(fragmentTAG.equals(getString(R.string.section_mes_resto)))
            newFrag = MesRestaurantsFragment.newInstance();
        else if(fragmentTAG.equals(getString(R.string.section_proximite)))
            newFrag = ProximityFragment.newInstance();
        else if(fragmentTAG.equals(getString(R.string.section_detail_resto)))
            newFrag = DetailRestaurantFragment.newInstance();
        else if(fragmentTAG.equals(getString(R.string.section_edit_resto)))
            newFrag = EditRestaurantFragment.newInstance(true);
        else if(fragmentTAG.equals(getString(R.string.section_nouveau_resto)))
            newFrag = EditRestaurantFragment.newInstance(false);
        else if(fragmentTAG.equals(getString(R.string.section_proximite_place)))
            newFrag = ProximityPlaceFragment.newInstance();
        else
            newFrag = AcceuilFragment.newInstance();

        currentFragment = newFrag;
        fragmentManager.beginTransaction().replace(R.id.container, currentFragment).addToBackStack(null).commit();

        changeTitle();
    }

    @Override
    public void onBackPressed() {
        int backCount = getSupportFragmentManager().getBackStackEntryCount();
        super.onBackPressed();

        if (backCount == 1){
            super.onBackPressed();
        }else {
            currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
            changeTitle();
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(item.getItemId() == R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getSupportFragmentManager().putFragment(outState, SAVE_CURRENT_FRAGMENT, currentFragment);
        outState.putSerializable(SAVE_CURRENT_RESTAURANT, currentSelectedRestaurant);
        super.onSaveInstanceState(outState);
    }

    /**
     * Remplit le hashmap
     */
    private void initHashMapSections(){
        fragmentTitleMap = new HashMap<Class, String>();
        fragmentTitleMap.put(AcceuilFragment.class, getString(R.string.section_acceuil));
        fragmentTitleMap.put(MesRestaurantsFragment.class, getString(R.string.section_mes_resto));
        fragmentTitleMap.put(ProximityFragment.class, getString(R.string.section_proximite));
        fragmentTitleMap.put(DetailRestaurantFragment.class, getString(R.string.section_detail_resto));
        fragmentTitleMap.put(EditRestaurantFragment.class, getString(R.string.section_edit_resto));
        fragmentTitleMap.put(ProximityPlaceFragment.class, getString(R.string.section_proximite_place));
    }

    /**
     * Met à jour le titre dans la ToolBar
     */
    private void changeTitle(){
        String titre = "";
        if(currentFragment.getClass().equals(EditRestaurantFragment.class)){
            if(((EditRestaurantFragment) currentFragment).isInEditMode())
                titre = getString(R.string.section_edit_resto);
            else
                titre = getString(R.string.section_nouveau_resto);
        }else
             titre = fragmentTitleMap.get(currentFragment.getClass());
        toolbar.setTitle(titre);
    }

    public void setCurrentSelectedRestaurant(InfoRestaurant info){
        currentSelectedRestaurant = info;
    }

    public InfoRestaurant getCurrentSelectedRestaurant(){
        return currentSelectedRestaurant;
    }

}
