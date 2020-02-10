package com.example.ambulance;

public class Locate {
    public String user_id,Pick_Latitude,Pick_Longitude,Dest_Latitude,Dest_Longitude;
    public Locate(String UID,String Latitude_P,String Longitude_P,String Latitude_D,String Longitude_D)
        {
            this.user_id=UID;
            this.Pick_Latitude = Latitude_P;
            this.Pick_Longitude = Longitude_P;
            this.Dest_Latitude=Latitude_D;
            this.Dest_Longitude=Longitude_D;
        }
    }
