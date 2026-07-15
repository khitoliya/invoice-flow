package com.dollyplastic.invoiceapp.ui.screens.masters.party

import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Party
import java.util.UUID


object PartySeedData {

    val parties = listOf(
        Party(
            partyId = java.util.UUID.randomUUID().toString(),
            tradeName = "A-ONE Industries",
            nickName = "A-ONE",
            gstin = "06AWPPK3628K2Z3",
            addressLine1 = "Plot No. 31",
            addressLine2 = "Sector-8, IMT Manesar",
            city = "Manesar",
            state = "Haryana",
            stateCode = "06",
            pincode = "122052"
        ),
        Party(
            partyId = java.util.UUID.randomUUID().toString(),
            tradeName = "RANJAN PACKAGING",
            nickName = "Ranjan Delhi",
            gstin = "07AKAPJ8738L1ZR",
            addressLine1 = "E-22, DSIIDC Industrial Area, Sector-1",
            addressLine2 = "",
            city = "Bawana",
            state = "Delhi",
            stateCode = "07",
            pincode = "110039"
        ),


                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Ranjan Packaging",
                        nickName = "Ranjan Bhiwadi",
                        gstin = "08AKAPJ8738L1ZP",
                        addressLine1 = "H-1/814, RIICO Industrial Area",
                        addressLine2 = "Chopanki",
                        city = "Bhiwadi",
                        state = "Rajasthan",
                        stateCode = "08",
                        pincode = "301019"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "SUNDA Industries",
                        nickName = "SUNDA",
                        gstin = "08BCBPD1172J1ZW",
                        addressLine1 = "H-1/330, RIICO Industrial Area, Chopanki",
                        addressLine2 = "",
                        city = "Alwar",
                        state = "Rajasthan",
                        stateCode = "08",
                        pincode = "301019"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Goyal Polymers",
                        nickName = "Goyal",
                        gstin = "08BSAPA2442R1ZO",
                        addressLine1 = "H1-770, RIICO Industrial Area, Khushkera",
                        addressLine2 = "",
                        city = "Alwar",
                        state = "Rajasthan",
                        stateCode = "08",
                        pincode = "301707"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Rachit Enterprises",
                        nickName = "Rachit",
                        gstin = "07BBFPS0371H1ZN",
                        addressLine1 = "T-11, Gali No. 10",
                        addressLine2 = "Anand Parbat Industrial Area",
                        city = "New Delhi",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110005"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Anurag Polychem",
                        nickName = "Anurag Polychem",
                        gstin = "07DHNPK3220B1ZR",
                        addressLine1 = "O-156, Sector-3, DSIIDC",
                        addressLine2 = "",
                        city = "Bawana",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110039"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Shakti Plastic",
                        nickName = "Shakti Plastic",
                        gstin = "07CFCPK8077C1ZG",
                        addressLine1 = "C-51, Rajdhani Building, Lawrence Road",
                        addressLine2 = "",
                        city = "New Delhi",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110035"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "RK Bobbins",
                        nickName = "RK Bobbins",
                        gstin = "07ALTPC7995G1ZF",
                        addressLine1 = "HN-04, Ground Floor",
                        addressLine2 = "Shani Bazaar Wali Gali",
                        city = "Johripur",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110094"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "MA POLYPLAST",
                        nickName = "MA PolyPlast",
                        gstin = "07ARVPA5457B2ZR",
                        addressLine1 = "House No. 476, Transport Nagar",
                        addressLine2 = "",
                        city = "North West Delhi",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110042"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "VMK POLYCHEM PVT LTD",
                        nickName = "VMK Polychem",
                        gstin = "08AAFCI8626Q1ZS",
                        addressLine1 = "G-1/229, RIICO Industrial Area, Kaharani",
                        addressLine2 = "",
                        city = "Bhiwadi",
                        state = "Rajasthan",
                        stateCode = "08",
                        pincode = "301019"
                    ),

                    // -------- Remaining parties --------

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "T.J. PLASTICS",
                        nickName = "TJ Plastic",
                        gstin = "07BAQPG3736G1ZL",
                        addressLine1 = "Shop No. 241",
                        addressLine2 = "Phase-I, Shahjada Bagh",
                        city = "Inderlok",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110035"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "DRISHTI TRADERS",
                        nickName = "Drishti Traders",
                        gstin = "09AHAPT9127E1Z6",
                        addressLine1 = "Shop No. 2, G.D.A Market",
                        addressLine2 = "Railway Station Road Shahibabad",
                        city = "Ghaziabad",
                        state = "Uttar Pradesh",
                        stateCode = "09",
                        pincode = "201005"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "GIRDHAR POLYMERS",
                        nickName = "Girdhar Polymers",
                        gstin = "07FZNPS0088H1ZS",
                        addressLine1 = "C-7, Old No. 341/3-B/1-A/13",
                        addressLine2 = "Kanti Nagar",
                        city = "Delhi",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110031"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "MAHI ENTERPRISES",
                        nickName = "Mahi Enterprise",
                        gstin = "07BQJPK2724F2ZY",
                        addressLine1 = "F-6/9, Krishna Nagar",
                        addressLine2 = "",
                        city = "Delhi",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110051"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "GR POLYFLAKES",
                        nickName = "GR Polyflakes",
                        gstin = "06GRAPK7119H1ZS",
                        addressLine1 = "Ground Floor, 22-7, Bajgera",
                        addressLine2 = "Near Bajgera",
                        city = "Gurugram",
                        state = "Haryana",
                        stateCode = "06",
                        pincode = "122001"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "HARSH PLASTIC",
                        nickName = "Harsh Plastic",
                        gstin = "07AQXPK8444M1ZU",
                        addressLine1 = "H. No. 223/2, Gali No. 7",
                        addressLine2 = "",
                        city = "Samaypur",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110042"
                    ),



                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Prakash Plastic",
                        nickName = "Prakash Plastic",
                        gstin = "07BFYPP3200F1ZD",
                        addressLine1 = "C-35, Ground Floor",
                        addressLine2 = "Chandan Vihar, Nihal Vihar",
                        city = "Nangloi",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110041"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "RANJEET PLASTICS",
                        nickName = "Ranjeet Plastic",
                        gstin = "07BPZPK7122J1ZA",
                        addressLine1 = "G/F, Plot No. 8-B/1, Kh. No. 83/4",
                        addressLine2 = "Gali No. 1, Udyog Nagar",
                        city = "Mundka",
                        state = "Delhi",
                        stateCode = "07",
                        pincode = "110041"
                    ),

                    Party(
                        partyId = UUID.randomUUID().toString(),
                        tradeName = "Chandramoli Polymer",
                        nickName = "Chandramoli Polymer",
                        gstin = "08AJJPG2218C1ZP",
                        addressLine1 = "Plot No. F-37A",
                        addressLine2 = "Khushkeda RIICO Industrial Area",
                        city = "Bhiwadi",
                        state = "Rajasthan",
                        stateCode = "08",
                        pincode = "301707"
                    )


    )

    val moreParties = listOf(

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "TAMANNA INDUSTRIES",
            nickName = "Tamanna Industries",
            gstin = "08AAHFT7055K1ZO",
            addressLine1 = "F-2273, RIICO Ramchanrapura Industrial Area",
            addressLine2 = "Sitapur, TONK ROAD",
            city = "Jaipur",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "302022"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "SUNNY PLASTICS",
            nickName = "Sunny Plastics",
            gstin = "07AUIPD7093C1ZQ",
            addressLine1 = "B-44 DDA PVC Market",
            addressLine2 = "Tikri Kalan",
            city = "New Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110041"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "R.M TRADING COMPANY",
            nickName = "RM Trading",
            gstin = "07BBHPR9260A2ZL",
            addressLine1 = "B-338 KH No-71/6",
            addressLine2 = "Ambika Enclave, Nihal Vihar",
            city = "Nangloi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110041"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Dolly Plastic",
            nickName = "Dolly Plastic Delhi",
            gstin = "07ARDPK8616J1ZK",
            addressLine1 = "Mohalla Barsan, H. No. 1221",
            addressLine2 = "Shri Jagdev Singh Tillu Marg",
            city = "New Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110041"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "CRDS Eastern Commtech Private Limited",
            nickName = "CRDS Eastern",
            gstin = "07AAICC1525F1Z0",
            addressLine1 = "B-19, B-Block, Anoop Nagar",
            addressLine2 = "Uttam Nagar",
            city = "New Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110059"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Chandra Industries",
            nickName = "Chandra Industries",
            gstin = "08AASPY1298C1Z3",
            addressLine1 = "Plot No. F/5(E), Phase-1",
            addressLine2 = "RIICO Industrial Area",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301019"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "PBS Chemicals",
            nickName = "PBS Chemicals",
            gstin = "08AADPY6648M2ZS",
            addressLine1 = "G1-161 RIICO Industrial Area",
            addressLine2 = "",
            city = "Behror",
            state = "Rajasthan",
            stateCode = "08",
            pincode = ""
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "PRITI ENTERPRISES",
            nickName = "Priti Enterprises",
            gstin = "08CNVPS0551A1ZV",
            addressLine1 = "H1-330 RIICO Industrial Area",
            addressLine2 = "Chopanki",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301019"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Bhagwati Polymers",
            nickName = "Bhagwati Polymers",
            gstin = "07AFHPG9996B1Z5",
            addressLine1 = "B-36, F.F, Sector-5",
            addressLine2 = "DSIIDC Bawana",
            city = "New Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110039"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "AHINSA POLYMERS",
            nickName = "Ahinsa Polymers",
            gstin = "07AAEPJ1358G1ZX",
            addressLine1 = "3025-A, Bahadurgarh Garh Road",
            addressLine2 = "Sadar Bazar",
            city = "Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110006"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "SHREE KRISHNA PLASTIC",
            nickName = "Shree Krishna Plastic",
            gstin = "07BBTPS8330N1ZP",
            addressLine1 = "Q-10/147",
            addressLine2 = "Mangol Puri",
            city = "Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110083"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "MANOJ AUTO INDUSTRIES",
            nickName = "Manoj Auto",
            gstin = "08AAZPS5906Q1ZE",
            addressLine1 = "H-1/860, Phase-III",
            addressLine2 = "Industrial Area",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301019"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "CRYSTAL POLYMER INDUSTRIES",
            nickName = "Crystal Polymer",
            gstin = "08ADJPN3447Q1ZT",
            addressLine1 = "H1-788, RIICO Industrial Area",
            addressLine2 = "Chopanki",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301019"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Prince Enterprises",
            nickName = "Prince Enterprises",
            gstin = "09AAOPG6795N1ZO",
            addressLine1 = "499, Udyog Kendra-II",
            addressLine2 = "Ecotech-III",
            city = "Greater Noida",
            state = "Uttar Pradesh",
            stateCode = "09",
            pincode = "201306"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "SANJAY PLASTIC",
            nickName = "Sanjay Plastic",
            gstin = "07AAZPK0918L1Z4",
            addressLine1 = "H-239, Sec-1",
            addressLine2 = "DSIIDC Industrial Area, Bawana",
            city = "Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110039"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Sunrise Polymer",
            nickName = "Sunrise Polymer",
            gstin = "08ALIPG2898R1Z5",
            addressLine1 = "Plot No. G1-613 B & C",
            addressLine2 = "RIICO Industrial Area, Khushkhera",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301707"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "ISHA CHEMICALS",
            nickName = "Isha Chemicals",
            gstin = "07EAVPG0741K1ZD",
            addressLine1 = "O-156, Sec-3",
            addressLine2 = "DSIIDC Bawana",
            city = "Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110039"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "D.K. INDUSTRIES",
            nickName = "DK Industries",
            gstin = "08CKBPK5553K1ZW",
            addressLine1 = "G1-191A RIICO Industrial Area",
            addressLine2 = "Khushkhera, Khairthal Tijara",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301707"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "SHRI NAND LAL GROUP",
            nickName = "Shri Nand Lal Group",
            gstin = "06AASPL2257Q1ZY",
            addressLine1 = "Village Patoda",
            addressLine2 = "Patoda Jhajjar",
            city = "Jhajjar",
            state = "Haryana",
            stateCode = "06",
            pincode = "124108"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "KHUSHBU ENTERPRISES",
            nickName = "Khushbu Enterprises",
            gstin = "06AIUPA9552B1Z9",
            addressLine1 = "Plot No. 35, Jeevan Nagar",
            addressLine2 = "Part-II, Gounchhi Ballabgarh",
            city = "Ballabgarh",
            state = "Haryana",
            stateCode = "06",
            pincode = "121004"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "TELCO PLASTIC",
            nickName = "Telco Plastic",
            gstin = "08AAMFT8236F1ZT",
            addressLine1 = "G1-369, RIICO Industrial Area",
            addressLine2 = "Khushkhera",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301017"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "VIKAS SPOOL PRIVATE LIMITED",
            nickName = "Vikas Spool",
            gstin = "06AACCV0185A1ZT",
            addressLine1 = "Kh. No. 84//6/1/2, 6/2, 13/2",
            addressLine2 = "Patauda Karola Road, Patauda",
            city = "Jhajjar",
            state = "Haryana",
            stateCode = "06",
            pincode = "124108"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Madhu Industries",
            nickName = "Madhu Industries",
            gstin = "07AFYPH9647B1ZZ",
            addressLine1 = "C-116, Sector-3",
            addressLine2 = "DSIDC Bawana Industrial Area",
            city = "Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110039"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "MS POLYMERS",
            nickName = "MS Polymers",
            gstin = "08CPSPM4048D1ZM",
            addressLine1 = "G-1/369 RIICO Industrial Area",
            addressLine2 = "Khushkhera",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301707"
        ),

        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "SHRI SWASTIK POLYMER",
            nickName = "Shri Swastik Polymer",
            gstin = "07CLEPA5915P1ZV",
            addressLine1 = "B-1/175, Vishnu Garden",
            addressLine2 = "West Delhi",
            city = "New Delhi",
            state = "Delhi",
            stateCode = "07",
            pincode = "110018"
        )
    )

    val lastParty = listOf(
        Party(
            partyId = UUID.randomUUID().toString(),
            tradeName = "Dolly Plastic",
            nickName = "Dolly Plastic Rajasthan",
            gstin = "08ARDPK8616J1ZI",
            addressLine1 = "H-1,207 CHOPANKI INDUSTRIAL AREA",
            addressLine2 = "TEHSIL TIJARA",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301019"
        )
    )

    val firms= listOf(
        Firm(
            firmId = UUID.randomUUID().toString(),
            tradeName = "Dolly Plastic",
            nickName = "Dolly Plastic Rajasthan",
            gstin = "08ARDPK8616J1ZI",
            addressLine1 = "H-1,207 CHOPANKI INDUSTRIAL AREA",
            addressLine2 = "TEHSIL TIJARA",
            city = "Bhiwadi",
            state = "Rajasthan",
            stateCode = "08",
            pincode = "301019",
            bankName = "Kotak Mahindra Bank",
            accountNumber = "3812667485",
            ifscCode = "KKBK0000275",
            branchName = "Bhiwadi"
        )
    )

}
