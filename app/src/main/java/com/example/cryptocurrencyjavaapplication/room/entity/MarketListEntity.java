package com.example.cryptocurrencyjavaapplication.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.cryptocurrencyjavaapplication.models.cryptolistmodel.AllMarketModel;

@Entity(tableName = "AllMarket")
public class MarketListEntity {

    @PrimaryKey
    public int uid;

    public int getUid() {
        return uid;
    }

    public AllMarketModel getAllMarketModel() {
        return allMarketModel;
    }

    @ColumnInfo(name = "AllMarket")
    public AllMarketModel allMarketModel;


    public MarketListEntity(AllMarketModel allMarketModel) {
        this.allMarketModel = allMarketModel;
    }
}
