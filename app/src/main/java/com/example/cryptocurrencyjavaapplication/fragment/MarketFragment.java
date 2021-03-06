package com.example.cryptocurrencyjavaapplication.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptocurrencyjavaapplication.MainActivity;
import com.example.cryptocurrencyjavaapplication.R;
import com.example.cryptocurrencyjavaapplication.adapter.marketRV_Adapter;
import com.example.cryptocurrencyjavaapplication.databinding.FragmentMarketBinding;
import com.example.cryptocurrencyjavaapplication.models.cryptolistmodel.AllMarketModel;
import com.example.cryptocurrencyjavaapplication.models.cryptolistmodel.CryptoMarketDataModel;
import com.example.cryptocurrencyjavaapplication.models.cryptolistmodel.DataItem;
import com.example.cryptocurrencyjavaapplication.room.entity.MarketDataEntity;
import com.example.cryptocurrencyjavaapplication.room.entity.MarketListEntity;
import com.example.cryptocurrencyjavaapplication.viewmodel.AppViewModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MarketFragment extends Fragment {

    FragmentMarketBinding binding;
    MainActivity mainActivity;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppViewModel appViewModel;
    CompositeDisposable compositeDisposable;
    List<DataItem> dataItemList;
    marketRV_Adapter marketRVAdapter;
    ArrayList<DataItem> filteredList = new ArrayList<>();
    ArrayList<DataItem> updatedList = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_market, container, false);

        compositeDisposable = new CompositeDisposable();
        setupSearchBox();
        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        getMarketListDataFromDb();
        getCryptoDataFromDb();
        return binding.getRoot();
    }

    private void setupSearchBox() {
        binding.searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }

    private void filter(String name) {
        filteredList.clear();
        for (DataItem item : dataItemList) {
            if (item.getSymbol().toLowerCase().contains(name.toLowerCase()) || item.getName().toLowerCase().contains(name.toLowerCase())) {
                filteredList.add(item);
            }
        }
        marketRVAdapter.updateData(filteredList);
        marketRVAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onChecked();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                onChecked();

            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onChecked();

            }

            void onChecked() {
                if (marketRVAdapter.getItemCount() == 0) {
                    binding.itemnotFoundTxt.setVisibility(View.VISIBLE);
                } else {
                    binding.itemnotFoundTxt.setVisibility(View.GONE);
                }
            }
        });

    }



    private void getMarketListDataFromDb() {
        Disposable completable = appViewModel.getAllMarketData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MarketListEntity>() {
                    @Override
                    public void accept(MarketListEntity marketListEntity) throws Throwable {
                        AllMarketModel allMarketModel = marketListEntity.getAllMarketModel();
                        dataItemList = allMarketModel.getRootData().getCryptoCurrencyList();


                        if (binding.marketRv.getAdapter() == null) {
                            marketRVAdapter = new marketRV_Adapter((ArrayList<DataItem>) dataItemList);
                            binding.marketRv.setAdapter(marketRVAdapter);
                        } else {
                            marketRVAdapter = (marketRV_Adapter) binding.marketRv.getAdapter();

                            if (filteredList.isEmpty() || filteredList.size() == 700){
                                marketRVAdapter.updateData((ArrayList<DataItem>) dataItemList);
                            }else {
                                marketRVAdapter.updateData(filteredList);
                            }
                        }

                    }
                });

        compositeDisposable.add(completable);
    }

    private void getCryptoDataFromDb(){
        Disposable disposable = appViewModel.getCryptoMarketData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MarketDataEntity>() {
                    @Override
                    public void accept(MarketDataEntity marketDataEntity) throws Throwable {

                        CryptoMarketDataModel cryptoMarketDataModel = marketDataEntity.getCryptoMarketModel();

                        //set BTC.D on UI
                        binding.CryptoBTCD.setText(cryptoMarketDataModel.getBtc_dominance());
                        String[] str3 = cryptoMarketDataModel.getBtcd_change().split("%");
                        if (Float.parseFloat(str3[0]) > 0){
                            binding.BTCDIcon.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24);
                            binding.BTCChange.setTextColor(Color.GREEN);
                        }else if (Float.parseFloat(str3[0]) < 0){
                            binding.BTCDIcon.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24);
                            binding.BTCChange.setTextColor(Color.RED);
                        }else {
                            binding.BTCDIcon.setBackgroundResource(R.drawable.ic_baseline_horizontal_rule_24);
                            binding.BTCChange.setTextColor(Color.WHITE);
                        }
                        binding.BTCChange.setText(cryptoMarketDataModel.getBtcd_change());


                        //set market cap  on UI
                        binding.CryptoMarketCap.setText(cryptoMarketDataModel.getMarketCap());
                        // get marketcap without %
                        String[] str = cryptoMarketDataModel.getMarketCap_change().split("%");
                        Log.e("TAG", "accept: " + str[0]);
                        if (Float.parseFloat(str[0]) > 0){
                            binding.marketcapIcon.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24);
                            binding.MarketCapChange.setTextColor(Color.GREEN);
                        }else if (Float.parseFloat(str[0]) < 0){
                            binding.marketcapIcon.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24);
                            binding.MarketCapChange.setTextColor(Color.RED);
                        }else {
                            binding.marketcapIcon.setBackgroundResource(R.drawable.ic_baseline_horizontal_rule_24);
                            binding.MarketCapChange.setTextColor(Color.WHITE);
                        }
                        binding.MarketCapChange.setText(cryptoMarketDataModel.getMarketCap_change());


                        //set market Vol on UI
                        binding.CryptoVolume.setText(cryptoMarketDataModel.getVol_24h());
                        //get VolumeChange without %
                        String[] str2 = cryptoMarketDataModel.getVol_change().split("%");
                        if (Float.parseFloat(str2[0]) > 0){
                            binding.VolumeIcon.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24);
                            binding.VolumeChange.setTextColor(Color.GREEN);
                        }else if (Float.parseFloat(str2[0]) < 0){
                            binding.VolumeIcon.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24);
                            binding.VolumeChange.setTextColor(Color.RED);
                        }else {
                            binding.VolumeIcon.setBackgroundResource(R.drawable.ic_baseline_horizontal_rule_24);
                            binding.VolumeChange.setTextColor(Color.WHITE);
                        }
                        binding.VolumeChange.setText(cryptoMarketDataModel.getVol_change());

                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
    }

    private void setupToolbar(View view) {
        NavController navController = Navigation.findNavController(view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.marketFragment).setOpenableLayout(mainActivity.drawerLayout).build();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        collapsingToolbarLayout = view.findViewById(R.id.collapsing_market_tb);
        NavigationUI.setupWithNavController(collapsingToolbarLayout, toolbar, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.marketFragment) {
                    collapsingToolbarLayout.setTitleEnabled(false);
                    toolbar.setNavigationIcon(R.drawable.ic_baseline_sort_24);
                    toolbar.setTitle("Market");
                    toolbar.setTitleTextColor(Color.WHITE);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}